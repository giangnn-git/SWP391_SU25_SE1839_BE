package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.AllPartClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.ChangeStatusPartClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.PartClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ClaimsByComponentResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.GetPartClaimResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.claimsByCategoryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Part;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartPolicy;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartPriceHistory;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairDetail;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairOrder;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairStep;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Vehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.VehiclePart;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyPolicy;
import com.fptu.swp391.se1839.oemevwarrantymanagement.event.EntityCreatedEvent;
import com.fptu.swp391.se1839.oemevwarrantymanagement.event.EntityUpdatedEvent;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartClaimRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairDetailRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairOrderRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairStepRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.VehiclePartRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.WarrantyClaimRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.PartClaimService;

import jakarta.transaction.Transactional;
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
    final RepairStepRepository repairStepRepository;
    final RepairOrderRepository repairOrderRepository;
    final ApplicationEventPublisher eventPublisher;

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

    @Transactional
    private void createVehiclePartAndRepairDetail(WarrantyClaim warrantyClaim, Part part, PartClaimRequest request) {
        String vin = warrantyClaim.getVehicle().getVin();
        Long serviceCenterId = warrantyClaim.getServiceCenter().getId();
        Long partId = part.getId();

        List<WarrantyClaim> activeClaims = warrantyClaimRepository.findActiveClaimsForPart(vin, serviceCenterId,
                partId);

        activeClaims = activeClaims.stream()
                .filter(c -> !c.getId().equals(warrantyClaim.getId()))
                .toList();

        if (!activeClaims.isEmpty()) {
            String claimCodes = activeClaims.stream()
                    .map(c -> String.valueOf(c.getId()))
                    .collect(Collectors.joining(", "));

            throw new RuntimeException("Part '" + part.getName()
                    + "' already exists in active claim(s): [" + claimCodes + "]");
        }

        // --- PartClaim ---
        PartClaim partClaim = PartClaim.builder()
                .warrantyClaim(warrantyClaim)
                .part(part)
                .quantity(request.getQuantity())
                .status(PartClaim.ClaimStatus.PENDING)
                .build();
        partClaimRepository.save(partClaim);
        eventPublisher.publishEvent(new EntityCreatedEvent<>(this, partClaim));

        // --- RepairDetail và Steps ---
        RepairOrder repairOrder = warrantyClaim.getRepairOrder();
        if (repairOrder != null) {
            Vehicle vehicle = warrantyClaim.getVehicle();
            VehiclePart vehiclePart = null;

            if (vehicle != null) {
                vehiclePart = vehiclePartRepository
                        .findByVehicleVinAndPartIdAndWarrantyClaimId(
                                vehicle.getVin(), part.getId(), warrantyClaim.getId())
                        .orElse(null);
            }

            RepairDetail repairDetail = RepairDetail.builder()
                    .repairOrder(repairOrder)
                    .part(part)
                    .vehiclePart(vehiclePart)
                    .description(part.getName())
                    .status(RepairDetail.DetailStatus.PENDING)
                    .build();

            repairDetailRepository.save(repairDetail);
            repairOrder.getRepairDetails().add(repairDetail);

            List<RepairStep> steps = new ArrayList<>();

            if (request.getQuantity() > 0) {
                List<String> partSteps = List.of(
                        "Check part " + part.getName(),
                        "Remove damaged part " + part.getName(),
                        "Install new part " + part.getName());
                List<Double> estimatedHours = List.of(0.3, 0.7, 0.5);

                for (int i = 0; i < partSteps.size(); i++) {
                    steps.add(RepairStep.builder()
                            .title(partSteps.get(i))
                            .estimatedHours(estimatedHours.get(i))
                            .status(RepairStep.StepStatus.PENDING)
                            .repairOrder(repairOrder)
                            .build());
                }
            }

            List<String> generalSteps = List.of("Operation Check", "Repair Completion");
            List<String> existingTitles = repairOrder.getSteps().stream()
                    .map(RepairStep::getTitle)
                    .toList();

            for (String title : generalSteps) {
                if (!existingTitles.contains(title)) {
                    steps.add(RepairStep.builder()
                            .title(title)
                            .estimatedHours(title.equals("Operation Check") ? 0.4 : 0.2)
                            .status(RepairStep.StepStatus.PENDING)
                            .repairOrder(repairOrder)
                            .build());
                }
            }

            repairStepRepository.saveAll(steps);
            repairOrder.getSteps().addAll(steps);
        }
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

    String getCoverageDescription(WarrantyPolicy policy) {
        if (policy.getType() == WarrantyPolicy.PolicyType.PROMOTION) {
            return "Discounted warranty plan valid for " + policy.getDurationPeriod() + " months on selected parts.";
        } else {
            return "Covers standard components such as basic battery pack, single motor, and charger.";
        }
    }

    String getConditionDescription(WarrantyPolicy policy) {
        if (policy.getType() == WarrantyPolicy.PolicyType.PROMOTION) {
            return "Warranty void if misuse, modification, or poor maintenance occurs.";
        } else {
            return "Warranty applies only to manufacturer defects; not valid for damage due to impact or overheating.";
        }
    }

    public List<GetPartClaimResponse> handleGetPartClaim(Long claimId) {
        List<PartClaim> partClaims = partClaimRepository.findByWarrantyClaimId(claimId);
        List<GetPartClaimResponse> responses = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (PartClaim partClaim : partClaims) {
            Part part = partClaim.getPart();

            // Lọc policy còn hiệu lực và đang ACTIVE
            List<PartPolicy> validPolicies = part.getPartPolicies().stream()
                    .filter(p -> p.getStatus() == PartPolicy.Status.ACTIVE)
                    .filter(p -> (p.getStartDate() == null || !p.getStartDate().isAfter(today)))
                    .filter(p -> (p.getEndDate() == null || !p.getEndDate().isBefore(today)))
                    .collect(Collectors.toList());

            PartPolicy partPolicy = validPolicies.stream()
                    .sorted((a, b) -> b.getEndDate().compareTo(a.getEndDate()))
                    .findFirst()
                    .orElse(null);

            WarrantyPolicy policy = (partPolicy != null) ? partPolicy.getWarrantyPolicy() : null;

            // 🔹 Lấy giá hiện tại từ PartPriceHistory
            Double currentPrice = part.getPartPriceHistories().stream()
                    .filter(h -> (h.getStartDate() == null || !h.getStartDate().isAfter(today)))
                    .filter(h -> (h.getEndDate() == null || !h.getEndDate().isBefore(today)))
                    .sorted((a, b) -> b.getStartDate().compareTo(a.getStartDate()))
                    .map(PartPriceHistory::getPrice)
                    .findFirst()
                    .orElse(0.0);

            String coverage = (policy != null)
                    ? getCoverageDescription(policy)
                    : "No warranty coverage information available.";

            String conditions = (policy != null)
                    ? getConditionDescription(policy)
                    : "No warranty conditions specified.";

            GetPartClaimResponse response = GetPartClaimResponse.builder()
                    .partClaimId(partClaim.getId())
                    .partClaimName(part.getName())
                    .status(partClaim.getStatus().name())
                    .estimatedCost(currentPrice)
                    .campaignName(policy != null ? policy.getName() : "N/A")
                    .description(part.getDescription())
                    .durationPeriord(policy != null ? policy.getDurationPeriod() : 0)
                    .effect(partPolicy != null ? partPolicy.getStartDate() : null)
                    .coverage(coverage)
                    .conditional(conditions)
                    .build();

            responses.add(response);
        }

        return responses;
    }

    public String handleChangeStatusPartClaim(ChangeStatusPartClaimRequest request, long claimId, long partClaimId) {
        try {
            if (request == null || request.getStatus() == null) {
                return "Invalid request: status is missing.";
            }

            // 1️⃣ Find PartClaim by ID
            PartClaim partClaim = partClaimRepository.findById(partClaimId)
                    .orElseThrow(() -> new NoSuchElementException("PartClaim not found with ID: " + partClaimId));

            // 2️⃣ Update status
            PartClaim.ClaimStatus newStatus = PartClaim.ClaimStatus.valueOf(request.getStatus().toUpperCase());
            partClaim.setStatus(newStatus);
            partClaimRepository.save(partClaim);

            eventPublisher.publishEvent(new EntityUpdatedEvent<>(this, partClaim));

            if (newStatus == PartClaim.ClaimStatus.REJECTED) {
                WarrantyClaim claim = partClaim.getWarrantyClaim();
                RepairOrder ro = claim.getRepairOrder();
                if (ro != null) {
                    Long partId = partClaim.getPart().getId();
                    String partName = partClaim.getPart().getName();

                    // 🔹 Update RepairDetail
                    List<RepairDetail> details = repairDetailRepository
                            .findByRepairOrderIdAndPartId(ro.getId(), partId);
                    for (RepairDetail rd : details) {
                        rd.setStatus(RepairDetail.DetailStatus.REJECTED);
                    }
                    repairDetailRepository.saveAll(details);

                    // 🔹 Update RepairStep liên quan đến part
                    List<RepairStep> relatedSteps = ro.getSteps().stream()
                            .filter(step -> step.getTitle().toLowerCase().contains(partName.toLowerCase()))
                            .collect(Collectors.toList());

                    for (RepairStep step : relatedSteps) {
                        step.setStatus(RepairStep.StepStatus.REJECTED);
                    }
                    repairStepRepository.saveAll(relatedSteps);
                }

                // 🔹 Kiểm tra nếu toàn bộ PartClaim của claim đều bị từ chối
                Long warrantyClaimId = claim.getId();
                List<PartClaim> allPartClaims = partClaimRepository.findByWarrantyClaimId(warrantyClaimId);

                boolean allRejected = allPartClaims.stream()
                        .allMatch(pc -> pc.getStatus() == PartClaim.ClaimStatus.REJECTED);

                if (allRejected) {
                    // 🔸 Cập nhật claim
                    claim.setStatus(WarrantyClaim.ClaimStatus.REJECTED);
                    warrantyClaimRepository.save(claim);
                    eventPublisher.publishEvent(new EntityUpdatedEvent<>(this, claim));

                    // 🔸 Cập nhật luôn repair order
                    if (ro != null) {
                        ro.setStatus(RepairOrder.OrderStatus.CANCELLED);
                        repairOrderRepository.save(ro);
                        eventPublisher.publishEvent(new EntityUpdatedEvent<>(this, ro));

                        // Optionally: reject tất cả detail/step còn lại
                        ro.getRepairDetails().forEach(rd -> rd.setStatus(RepairDetail.DetailStatus.REJECTED));
                        repairDetailRepository.saveAll(ro.getRepairDetails());
                        eventPublisher.publishEvent(new EntityUpdatedEvent<>(this, ro.getRepairDetails()));

                        ro.getSteps().forEach(step -> step.setStatus(RepairStep.StepStatus.CANCELLED));
                        repairStepRepository.saveAll(ro.getSteps());
                        eventPublisher.publishEvent(new EntityUpdatedEvent<>(this, ro.getSteps()));

                    }
                }
            }

            // 3️⃣ Get current part
            Part part = partClaim.getPart();
            LocalDate today = LocalDate.now();

            // 4️⃣ Find active & valid policy
            PartPolicy activePolicy = part.getPartPolicies().stream()
                    .filter(p -> p.getStatus() == PartPolicy.Status.ACTIVE)
                    .filter(p -> (p.getStartDate() == null || !p.getStartDate().isAfter(today)))
                    .filter(p -> (p.getEndDate() == null || !p.getEndDate().isBefore(today)))
                    .sorted((a, b) -> b.getEndDate().compareTo(a.getEndDate()))
                    .findFirst()
                    .orElse(null);

            WarrantyPolicy warranty = (activePolicy != null) ? activePolicy.getWarrantyPolicy() : null;

            // 5️⃣ Return message
            return String.format(
                    "✅ Part claim status updated successfully!\n" +
                            "Part: %s\n" +
                            "New Status: %s\n" +
                            "Warranty Policy: %s\n" +
                            "Claim ID: %d\n" +
                            "PartClaim ID: %d",
                    part.getName(),
                    partClaim.getStatus().name(),
                    warranty != null ? warranty.getName() : "No active warranty policy found",
                    claimId,
                    partClaimId);

        } catch (Exception e) {
            return "❌ Failed to update part claim status: " + e.getMessage();
        }
    }

}
