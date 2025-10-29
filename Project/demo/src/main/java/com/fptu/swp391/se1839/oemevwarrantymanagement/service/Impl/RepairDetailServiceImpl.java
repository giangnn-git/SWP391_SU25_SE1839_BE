package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.annotation.Activity;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.ChangeStatusRepairDetailRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ChangeStatusRepairDetailResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.GetAllRepairDetailResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.RepairDetailResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairDetail;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairOrder;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairStep;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Part;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Vehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.VehiclePart;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartInventoryRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairDetailRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairOrderRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.VehiclePartRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.RepairDetailService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RepairDetailServiceImpl implements RepairDetailService {
        final RepairDetailRepository repairDetailRepository;
        final VehiclePartRepository vehiclePartRepository;
        final RepairOrderRepository repairOrderRepository;
        final PartInventoryRepository partInventoryRepository;

        RepairDetailResponse buildRepairDetailResponse(RepairDetail rd, long serviceCenterId) {
                long quantity = rd.getRepairOrder().getWarrantyClaim().getPartClaims().stream()
                                .filter(pc -> pc.getPart().getId().equals(rd.getPart().getId()))
                                .mapToLong(PartClaim::getQuantity)
                                .sum();

                return RepairDetailResponse.builder()
                                .id(rd.getId())
                                .partName(rd.getPart().getName())
                                .category(rd.getPart().getPartCategory())
                                .serailNumber(rd.getVehiclePart().getSerialNumber())
                                .quantity(quantity)
                                .prodcutYear(rd.getVehiclePart().getVehicle().getProductYear())
                                .modelName(rd.getVehiclePart().getVehicle().getModel().getName())
                                .vin(rd.getVehiclePart().getVehicle().getVin())
                                .licensePlate(rd.getVehiclePart().getVehicle().getLicensePlate())
                                .build();
        }

        public GetAllRepairDetailResponse handleGetRepairDetail(long serviceCenterId, long repairOrderId) {
                List<RepairDetail> details = repairDetailRepository.findByRepairOrderId(repairOrderId);
                List<RepairDetailResponse> responses = details.stream()
                                .map(rd -> buildRepairDetailResponse(rd, serviceCenterId))
                                .toList();
                return new GetAllRepairDetailResponse(responses);
        }

        @Activity(title = "Part Replacement", status = "COMPLETED", detail = "Replaced part '{partName}' with serial '{serialNumber}' for claim {claimId}, vehicle VIN: {vehicleVin}")
        @Override
        public ChangeStatusRepairDetailResponse handleChangeStatus(
                        ChangeStatusRepairDetailRequest request,
                        Long repairDetailId) {

                RepairDetail rd = repairDetailRepository.findById(repairDetailId)
                                .orElseThrow(() -> new NoSuchElementException(
                                                "RepairDetail not found with id " + repairDetailId));

                Part part = rd.getPart();
                RepairOrder ro = rd.getRepairOrder();
                WarrantyClaim claim = ro.getWarrantyClaim();
                Vehicle vehicle = claim.getVehicle();

                vehiclePartRepository.findActiveVehiclePart(vehicle, part).ifPresent(oldVp -> {
                        oldVp.setRemovalDate(LocalDate.now());
                        vehiclePartRepository.save(oldVp);
                });

                String serialNumber = "SC-" + vehicle.getVin() + "-" + part.getId() + "-" + System.currentTimeMillis();
                VehiclePart newVp = VehiclePart.builder()
                                .part(part)
                                .vehicle(vehicle)
                                .warrantyClaim(claim)
                                .serialNumber(serialNumber)
                                .installationDate(LocalDate.now())
                                .removalDate(null)
                                .build();
                vehiclePartRepository.save(newVp);

                for (PartClaim pc : claim.getPartClaims()) {
                        partInventoryRepository.findByPart_Id(pc.getId()).ifPresent(pi -> {
                                pi.setQuantity((int) (pi.getQuantity() - pc.getQuantity()));
                                partInventoryRepository.save(pi);
                        });
                }

                rd.setStatus(RepairDetail.DetailStatus.REPLACED);
                rd.setVehiclePart(newVp);
                repairDetailRepository.save(rd);

                List<RepairStep> steps = new ArrayList<>(ro.getSteps());
                List<RepairDetail> details = repairDetailRepository.findByRepairOrderId(ro.getId());

                long totalSteps = steps.size();
                long completedSteps = steps.stream()
                                .filter(s -> s.getStatus() == RepairStep.StepStatus.COMPLETED)
                                .count();

                long totalDetails = details.size();
                long replacedDetails = details.stream()
                                .filter(d -> d.getStatus() == RepairDetail.DetailStatus.REPLACED)
                                .count();

                int percent = 0;
                if (totalSteps + totalDetails > 0) {
                        percent = (int) Math
                                        .ceil((completedSteps + replacedDetails) * 100.0 / (totalSteps + totalDetails));
                }

                if (percent == 100) {
                        ro.setStatus(RepairOrder.OrderStatus.COMPLETED);
                        if (ro.getEndDate() == null) {
                                ro.setEndDate(LocalDateTime.now());
                        }
                } else if (steps.stream().anyMatch(s -> s.getStatus() == RepairStep.StepStatus.IN_PROGRESS)) {
                        ro.setStatus(RepairOrder.OrderStatus.IN_PROGRESS);
                } else if (completedSteps == 0 && replacedDetails == 0) {
                        ro.setStatus(RepairOrder.OrderStatus.WAITING);
                } else {
                        ro.setStatus(RepairOrder.OrderStatus.PENDING);
                }

                repairOrderRepository.save(ro);

                return ChangeStatusRepairDetailResponse.builder()
                                .date(LocalDate.now())
                                .technicianName(ro.getTechnical().getName())
                                .description(request.getDescription())
                                .serailNumber(serialNumber)
                                .build();
        }

}
