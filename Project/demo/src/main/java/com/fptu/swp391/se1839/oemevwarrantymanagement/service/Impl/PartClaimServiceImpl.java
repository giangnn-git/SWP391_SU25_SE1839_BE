package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.AllPartClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.PartClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ClaimsByComponentResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.WarrantyClaimStatusResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.claimsByCategoryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Part;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairDetail;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairOrder;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Vehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.VehiclePart;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartClaimRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairDetailRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.VehiclePartRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.WarrantyClaimRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.PartClaimService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PartClaimServiceImpl implements PartClaimService {
    final PartClaimRepository partClaimRepository;
    final WarrantyClaimRepository warrantyClaimRepository;
    final VehiclePartRepository vehiclePartRepository;
    final RepairDetailRepository repairDetailRepository;
    final PartRepository partRepository;

    public List<claimsByCategoryResponse> calculateClaimsByCategory(Long serviceCenterId) {
        Long totalClaims = partClaimRepository.countByWarrantyClaimServiceCenterId(serviceCenterId);
        if (totalClaims == 0)
            totalClaims = 1L;

        List<Object[]> categoryData = partClaimRepository.countFailuresByCategory(serviceCenterId);

        List<claimsByCategoryResponse> claimsByCategory = new ArrayList<>();
        for (Object[] row : categoryData) {
            String category = (String) row[0];
            Long count = (Long) row[1];
            double percentage = (count * 100.0) / totalClaims;

            claimsByCategory.add(
                    claimsByCategoryResponse.builder()
                            .category(category)
                            .percentage(String.format("%.1f%%", percentage))
                            .build());
        }
        return claimsByCategory;
    }

    private void createVehiclePartAndRepairDetail(WarrantyClaim wc, Part part, PartClaimRequest request) {
        long timestamp = System.currentTimeMillis();
        String serialNumber = "SC" + part.getId() + "-" + timestamp;

        // VehiclePart
        VehiclePart vp = VehiclePart.builder()
                .part(part)
                .vehicle(wc.getVehicle())
                .warrantyClaim(wc)
                .serialNumber(serialNumber)
                .installationDate(null)
                .removalDate(null)
                .build();
        vehiclePartRepository.save(vp);

        // PartClaim
        PartClaim partClaim = PartClaim.builder()
                .warrantyClaim(wc)
                .part(part)
                .quantity(request.getQuantity())
                .status(PartClaim.ClaimStatus.PENDING)
                .build();
        partClaimRepository.save(partClaim);

        // RepairDetail
        RepairOrder ro = wc.getRepairOrder();
        if (ro != null) {
            RepairDetail rd = RepairDetail.builder()
                    .repairOrder(ro)
                    .part(part)
                    .vehiclePart(vp)
                    .description(part.getName())
                    .status(RepairDetail.DetailStatus.PENDING)
                    .build();
            repairDetailRepository.save(rd);
        }

        wc.getVehicleParts().add(vp);
    }

    private boolean isPartDuplicated(Vehicle vehicle, Part part) {
        return vehiclePartRepository.findByVehicleVinAndPartId(vehicle.getVin(), part.getId()).isPresent();
    }

    public String handleCreatePartClaim(AllPartClaimRequest request, long claimId) {
        WarrantyClaim wc = warrantyClaimRepository.findById(claimId)
                .orElseThrow(() -> new NoSuchElementException("Warranty claim doesn't exist"));

        List<String> duplicatedParts = new ArrayList<>();

        for (PartClaimRequest pcr : request.getParts()) {
            Part part = partRepository.findById(pcr.getId())
                    .orElseThrow(() -> new NoSuchElementException("Part not found with id " + pcr.getId()));

            // Check duplicate
            if (isPartDuplicated(wc.getVehicle(), part)) {
                duplicatedParts.add(part.getName());
                continue;
            }

            // Create VehiclePart, PartClaim, RepairDetail
            createVehiclePartAndRepairDetail(wc, part, pcr);
        }

        if (!duplicatedParts.isEmpty()) {
            return "Some parts already exist: " + String.join(", ", duplicatedParts);
        }

        return "Part claim created successfully";
    }

    public List<ClaimsByComponentResponse> calculateClaimsByComponent(Long serviceCenterId) {
        Long totalClaims = partClaimRepository.countByWarrantyClaimServiceCenterId(serviceCenterId);
        if (totalClaims == 0)
            totalClaims = 1L; // tránh chia 0

        List<Object[]> componentData = partClaimRepository.countFailuresByComponent(serviceCenterId);

        List<ClaimsByComponentResponse> result = new ArrayList<>();
        for (Object[] row : componentData) {
            Part part = (Part) row[0];
            Long count = (Long) row[1];
            double percentage = (count * 100.0) / totalClaims;

            result.add(ClaimsByComponentResponse.builder()
                    .component(part.getName())
                    .count(count)
                    .percentage(String.format("%.1f%%", percentage))
                    .build());
        }

        return result;
    }
}
