package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.ChangeStatusRepairDetailRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.GetAllRepairDetailResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.RepairDetailResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartInventory;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairDetail;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairOrder;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairStep;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.VehiclePart;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartInventoryRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairDetailRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairOrderRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairStepRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.VehiclePartRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.RepairDetailService;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RepairDetailServiceImpl implements RepairDetailService {
        final RepairDetailRepository repairDetailRepository;
        final VehiclePartRepository vehiclePartRepository;
        final RepairStepRepository repairStepRepository;
        final RepairOrderRepository repairOrderRepository;

        RepairDetailResponse buildRepairDetailResponse(RepairDetail rd) {
                long quantity = rd.getRepairOrder().getWarrantyClaim().getPartClaims().stream()
                                .filter(pc -> pc.getPart().getId().equals(rd.getPart().getId()))
                                .mapToLong(PartClaim::getQuantity)
                                .sum();

                RepairDetailResponse.RepairDetailResponseBuilder builder = RepairDetailResponse.builder()
                                .id(rd.getId())
                                .partName(rd.getPart().getName())
                                .category(rd.getPart().getPartCategory())
                                .oldSerialNumber(rd.getVehiclePart().getOldSerialNumber())
                                .quantity(quantity)
                                .productYear(rd.getVehiclePart().getVehicle().getProductYear())
                                .modelName(rd.getVehiclePart().getVehicle().getModel().getName())
                                .vin(rd.getVehiclePart().getVehicle().getVin())
                                .licensePlate(rd.getVehiclePart().getVehicle().getLicensePlate());

                // --- Kiểm tra step Assembly ---
                boolean hasAssemblyStep = rd.getRepairOrder().getSteps().stream()
                                .anyMatch(s -> "Assembly".equalsIgnoreCase(s.getTitle()));

                // Nếu trạng thái là REPLACED và có step Assembly, thêm các field đặc biệt
                if (rd.getStatus() == RepairDetail.DetailStatus.REPLACED && hasAssemblyStep) {
                        builder.installationDate(rd.getVehiclePart().getInstallationDate())
                                        .replacementDescription(rd.getDescription())
                                        .technicianName(rd.getRepairOrder().getTechnical().getName())
                                        .newSerialNumber(rd.getVehiclePart().getNewSerialNumber()); // nếu có field
                                                                                                    // riêng
                }

                return builder.build();
        }

        public GetAllRepairDetailResponse handleGetRepairDetail(long repairOrderId) {
                List<RepairDetail> details = repairDetailRepository.findByRepairOrderId(repairOrderId)
                                .stream()
                                .filter(rd -> rd.getStatus() != RepairDetail.DetailStatus.REJECTED) // loại bỏ REJECTED
                                .toList();

                List<RepairDetailResponse> responses = details.stream()
                                .map(rd -> buildRepairDetailResponse(rd))
                                .toList();

                return new GetAllRepairDetailResponse(responses);
        }

        @Transactional
        public void handleChangeDetailStatus(ChangeStatusRepairDetailRequest request, long repairDetailId) {
                // 1. Lấy RepairDetail
                RepairDetail rd = repairDetailRepository.findById(repairDetailId)
                                .orElseThrow(() -> new NoSuchElementException(
                                                "RepairDetail not found: " + repairDetailId));

                // Cập nhật status của RepairDetail
                rd.setStatus(RepairDetail.DetailStatus.valueOf(request.getStatus()));
                repairDetailRepository.save(rd);

                // 2. Lấy RepairOrder
                RepairOrder ro = rd.getRepairOrder();

                // 3. Cập nhật step "Repair/Replace Part"
                updateRepairReplacePartStepStatus(ro);

                // 4. Đồng bộ trạng thái RepairOrder
                updateRepairOrderStatus(ro);
        }

        @Transactional
        private void updateRepairReplacePartStepStatus(RepairOrder ro) {
                ro.getSteps().stream()
                                .filter(step -> "Repair/Replace Part".equalsIgnoreCase(step.getTitle()))
                                .findFirst()
                                .ifPresent(step -> {
                                        // Lấy tất cả RepairDetail từ DB
                                        List<RepairDetail> details = repairDetailRepository
                                                        .findByRepairOrderId(ro.getId());

                                        if (details.isEmpty()) {
                                                // Chưa có detail nào -> PENDING
                                                step.setStatus(RepairStep.StepStatus.PENDING);
                                        } else {
                                                // Kiểm tra tất cả detail đã xử lý (USED hoặc REPLACED)
                                                boolean allDone = details.stream()
                                                                .allMatch(d -> d.getStatus() == RepairDetail.DetailStatus.USED
                                                                                || d.getStatus() == RepairDetail.DetailStatus.REPLACED);

                                                if (allDone) {
                                                        // Nếu tất cả detail đã hoàn thành -> step COMPLETED
                                                        step.setStatus(RepairStep.StepStatus.COMPLETED);

                                                        // Set removalDate cho các VehiclePart bị REPLACED mà chưa set
                                                        details.stream()
                                                                        .filter(d -> d.getStatus() == RepairDetail.DetailStatus.REPLACED)
                                                                        .forEach(d -> {
                                                                                VehiclePart oldVP = d.getVehiclePart();
                                                                                if (oldVP != null && oldVP
                                                                                                .getRemovalDate() == null) {
                                                                                        oldVP.setRemovalDate(null); // nếu
                                                                                                                    // không
                                                                                                                    // dùng
                                                                                                                    // thời
                                                                                                                    // gian
                                                                                        vehiclePartRepository
                                                                                                        .save(oldVP);
                                                                                }
                                                                        });
                                                } else {
                                                        // Nếu còn detail chưa done -> step PENDING
                                                        step.setStatus(RepairStep.StepStatus.PENDING);
                                                }
                                        }

                                        repairStepRepository.save(step);
                                });
        }

        @Transactional
        private void updateRepairOrderStatus(RepairOrder ro) {
                List<RepairStep> steps = new ArrayList<>(ro.getSteps());
                List<RepairDetail> details = repairDetailRepository.findByRepairOrderId(ro.getId());

                long totalSteps = steps.size();
                long completedSteps = steps.stream()
                                .filter(s -> s.getStatus() == RepairStep.StepStatus.COMPLETED)
                                .count();

                long totalDetails = details.size();
                long usedOrReplaced = details.stream()
                                .filter(d -> d.getStatus() == RepairDetail.DetailStatus.USED
                                                || d.getStatus() == RepairDetail.DetailStatus.REPLACED)
                                .count();

                int percent = 0;
                if (totalSteps + totalDetails > 0) {
                        percent = (int) Math
                                        .ceil((completedSteps + usedOrReplaced) * 100.0 / (totalSteps + totalDetails));
                }

                RepairOrder.OrderStatus newStatus;

                if (percent == 100) {
                        if (!ro.getSupervisorApproved()) {
                                newStatus = RepairOrder.OrderStatus.PENDING_SUPERVISOR;
                        } else {
                                newStatus = RepairOrder.OrderStatus.COMPLETED;
                        }
                } else if (steps.stream().anyMatch(s -> s.getStatus() == RepairStep.StepStatus.PENDING)
                                || details.stream().anyMatch(d -> d.getStatus() == RepairDetail.DetailStatus.PENDING)) {
                        newStatus = RepairOrder.OrderStatus.IN_PROGRESS;
                } else if (completedSteps == 0 && usedOrReplaced == 0) {
                        newStatus = RepairOrder.OrderStatus.WAITING;
                } else {
                        newStatus = RepairOrder.OrderStatus.PENDING;
                }

                ro.setStatus(newStatus);
                repairOrderRepository.save(ro);
        }

}
