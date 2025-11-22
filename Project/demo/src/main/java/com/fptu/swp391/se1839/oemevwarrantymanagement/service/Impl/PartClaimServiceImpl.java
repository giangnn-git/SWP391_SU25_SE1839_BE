package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.ChangeStatusPartClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.PartClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ChangeStatusPartClaimResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ClaimsByComponentResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.GetPartClaimResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.claimsByCategoryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Part;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartInventory;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartPolicy;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartPriceHistory;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairDetail;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairManual;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairOrder;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairStep;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.User;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyPolicy;
import com.fptu.swp391.se1839.oemevwarrantymanagement.event.EntityUpdatedEvent;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartClaimRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartInventoryRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairDetailRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairManualRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairOrderRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairStepRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.UserRepository;
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
    final RepairDetailRepository repairDetailRepository;
    final RepairStepRepository repairStepRepository;
    final RepairOrderRepository repairOrderRepository;
    final ApplicationEventPublisher eventPublisher;
    final UserRepository userRepository;
    final PartInventoryRepository partInventoryRepository;
    final RepairManualRepository repairManualRepository;

    public List<claimsByCategoryResponse> calculateClaimsByCategory(Long serviceCenterId) {
        boolean hasSpecificCenter = serviceCenterId != null && serviceCenterId > 0;

        // Tổng số claim
        Long totalClaims = hasSpecificCenter
                ? partClaimRepository.countByWarrantyClaimServiceCenterId(serviceCenterId)
                : partClaimRepository.count(); // tổng cho tất cả trung tâm
        if (totalClaims == 0)
            totalClaims = 1L; // tránh chia cho 0

        // Lấy dữ liệu theo category
        List<Object[]> categoryData = hasSpecificCenter
                ? partClaimRepository.countFailuresByCategory(serviceCenterId)
                : partClaimRepository.countAllFailuresByCategory(); // cần implement query tổng cho tất cả trung tâm

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

    public List<ClaimsByComponentResponse> calculateClaimsByComponent(Long serviceCenterId) {
        Long totalClaims = partClaimRepository.countByWarrantyClaimServiceCenterId(
                serviceCenterId != null && serviceCenterId > 0 ? serviceCenterId : null);
        if (totalClaims == 0)
            totalClaims = 1L;

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
                    .policyName(policy != null ? policy.getName() : "N/A")
                    .description(part.getDescription())
                    .durationPeriod(policy != null ? policy.getDurationPeriod() : 0)
                    .effect(partPolicy != null ? partPolicy.getStartDate() : null)
                    .coverage(coverage)
                    .conditional(conditions)
                    .build();

            responses.add(response);
        }

        return responses;
    }

    @Transactional
    public ChangeStatusPartClaimResponse handleChangeStatusPartClaim(ChangeStatusPartClaimRequest request,
            long partClaimId,
            long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (user.getRole() != User.Role.EVM_STAFF) {
            return ChangeStatusPartClaimResponse.builder()
                    .message("❌ Access denied: only EVM_STAFF can change part claim status.")
                    .build();
        }

        try {
            if (request == null || request.getStatus() == null) {
                return ChangeStatusPartClaimResponse.builder()
                        .message("Invalid request: status is missing.")
                        .build();
            }

            // Find PartClaim
            PartClaim partClaim = partClaimRepository.findById(partClaimId)
                    .orElseThrow(() -> new NoSuchElementException("PartClaim not found with ID: " + partClaimId));

            // Update status
            PartClaim.ClaimStatus newStatus = PartClaim.ClaimStatus.valueOf(request.getStatus().toUpperCase());
            partClaim.setStatus(newStatus);
            partClaimRepository.save(partClaim);
            eventPublisher.publishEvent(new EntityUpdatedEvent<>(this, partClaim));

            // Get WarrantyClaim & RepairOrder
            WarrantyClaim claim = partClaim.getWarrantyClaim();
            RepairOrder repairOrder = claim.getRepairOrder();

            // Handle REJECTED PartClaim
            if (newStatus == PartClaim.ClaimStatus.REJECTED && repairOrder != null) {

                // Update related steps
                List<RepairStep> relatedSteps = repairOrder.getSteps().stream()
                        .collect(Collectors.toList());
                relatedSteps.forEach(step -> step.setStatus(RepairStep.StepStatus.REJECTED));
                repairStepRepository.saveAll(relatedSteps);

                // Update related details
                List<RepairDetail> relatedDetails = repairDetailRepository.findByRepairOrderId(repairOrder.getId())
                        .stream()
                        .filter(d -> d.getPart().getId().equals(partClaim.getPart().getId()))
                        .collect(Collectors.toList());
                relatedDetails.forEach(d -> d.setStatus(RepairDetail.DetailStatus.REJECTED));
                repairDetailRepository.saveAll(relatedDetails);
            }

            List<PartClaim> allPartClaims = partClaimRepository.findByWarrantyClaimId(claim.getId());

            boolean allDecided = allPartClaims.stream()
                    .allMatch(pc -> pc.getStatus() != PartClaim.ClaimStatus.PENDING);

            if (allDecided) {

                boolean anyApproved = allPartClaims.stream()
                        .anyMatch(pc -> pc.getStatus() == PartClaim.ClaimStatus.APPROVED);

                if (anyApproved) {
                    claim.setStatus(WarrantyClaim.ClaimStatus.APPROVED);
                    warrantyClaimRepository.save(claim);
                    eventPublisher.publishEvent(new EntityUpdatedEvent<>(this, claim));

                    if (user.getRole() == User.Role.EVM_STAFF || user.getRole() == User.Role.ADMIN) {
                        if (repairOrder == null) {
                            repairOrder = new RepairOrder();
                            repairOrder.setWarrantyClaim(claim);
                            repairOrderRepository.save(repairOrder);
                            claim.setRepairOrder(repairOrder);
                            warrantyClaimRepository.save(claim);
                        }

                        boolean inspectionExists = repairStepRepository
                                .existsByRepairOrderIdAndTitle(repairOrder.getId(), "Inspection");
                        if (!inspectionExists) {
                            RepairStep inspectionStep = RepairStep.builder()
                                    .title("Inspection")
                                    .estimatedHours(suggestHours("Inspection"))
                                    .status(RepairStep.StepStatus.PENDING)
                                    .repairOrder(repairOrder)
                                    .build();
                            repairStepRepository.save(inspectionStep);
                        }

                        // Create RepairDetail for APPROVED parts
                        for (PartClaim pc : allPartClaims) {
                            if (pc.getStatus() == PartClaim.ClaimStatus.APPROVED) {

                                long existingDetails = repairDetailRepository.countByRepairOrderIdAndPartId(
                                        repairOrder.getId(), pc.getPart().getId());

                                long quantityToCreate = pc.getQuantity() - existingDetails;

                                for (long i = 1; i <= quantityToCreate; i++) {
                                    RepairDetail detail = RepairDetail.builder()
                                            .part(pc.getPart())
                                            .description(pc.getPart().getName() + "-" + (existingDetails + i))
                                            .repairOrder(repairOrder)
                                            .build();
                                    repairDetailRepository.save(detail);
                                }
                            }
                        }
                    }

                } else {
                    // All REJECTED
                    claim.setStatus(WarrantyClaim.ClaimStatus.REJECTED);
                    warrantyClaimRepository.save(claim);
                    eventPublisher.publishEvent(new EntityUpdatedEvent<>(this, claim));
                }
            }

            // Get active warranty policy
            Part part = partClaim.getPart();
            LocalDate today = LocalDate.now();

            PartPolicy activePolicy = part.getPartPolicies().stream()
                    .filter(p -> p.getStatus() == PartPolicy.Status.ACTIVE)
                    .filter(p -> p.getStartDate() == null || !p.getStartDate().isAfter(today))
                    .filter(p -> p.getEndDate() == null || !p.getEndDate().isBefore(today))
                    .sorted((a, b) -> b.getEndDate().compareTo(a.getEndDate()))
                    .findFirst()
                    .orElse(null);

            WarrantyPolicy warranty = activePolicy != null ? activePolicy.getWarrantyPolicy() : null;

            // Return result
            return ChangeStatusPartClaimResponse.builder()
                    .message(String.format(
                            "Part claim status updated successfully!\n" +
                                    "Part: %s\n" +
                                    "New Status: %s\n" +
                                    "Warranty Policy: %s\n" +
                                    "Claim ID: %d\n" +
                                    "PartClaim ID: %d",
                            part.getName(),
                            partClaim.getStatus().name(),
                            warranty != null ? warranty.getName() : "No active warranty policy found",
                            claim.getId(),
                            partClaimId))
                    .repairOrderId(repairOrder != null ? repairOrder.getId() : null)
                    .build();

        } catch (Exception e) {
            return ChangeStatusPartClaimResponse.builder()
                    .message("❌ Failed to update part claim status: " + e.getMessage())
                    .build();
        }
    }

    private double suggestHours(String title) {
        List<RepairStep> steps = repairStepRepository.findByTitleIgnoreCaseAndStatus(
                title, RepairStep.StepStatus.COMPLETED);

        if (steps.isEmpty()) {
            return 0.5;
        }

        double total = steps.stream()
                .filter(s -> s.getActualHours() != null)
                .mapToDouble(RepairStep::getActualHours)
                .sum();

        long count = steps.stream()
                .filter(s -> s.getActualHours() != null)
                .count();

        double value = (count > 0) ? total / count : 0.5;

        return Math.round(value * 10.0) / 10.0; // làm tròn 1 số thập phân
    }

    @Transactional
    public String handleUpdatePartQuantities(long claimId, List<PartClaimRequest> updates, long userId) {

        WarrantyClaim claim = warrantyClaimRepository.findById(claimId)
                .orElseThrow(() -> new NoSuchElementException("Warranty claim not found: " + claimId));

        List<PartClaim> partClaims = partClaimRepository.findByWarrantyClaimId(claimId);

        Map<Long, PartClaimRequest> updateMap = updates.stream()
                .collect(Collectors.toMap(PartClaimRequest::getId, Function.identity()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User with id: " + userId + " not found!"));

        boolean updated = false;

        for (PartClaim pc : partClaims) {
            Long partId = pc.getPart().getId();
            PartClaimRequest update = updateMap.get(partId);

            if (update == null)
                continue;

            PartInventory pi = partInventoryRepository
                    .findByPartIdAndServiceCenterId(partId, user.getServiceCenter().getId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Part with id: " + partId + " not found in inventory!"));

            long stockQty = pi.getQuantity();
            long requestQty = update.getQuantity();

            RepairManual rm = repairManualRepository
                    .findFirstByPartIdAndModel(partId, pc.getWarrantyClaim().getVehicle().getModel().getName())
                    .orElse(null);

            long recommendedQuantity = rm != null ? rm.getMinQuantity() : 0;

            if (stockQty <= 0) {
                return "Part with ID " + partId + " is out of stock. Please add more to the inventory.";
            }

            if (requestQty > stockQty) {
                return "Part with ID " + partId
                        + " exceeds available stock (" + stockQty + "). Please reduce the requested quantity.";
            }

            if (requestQty > recommendedQuantity) {
                return "Part with ID " + partId
                        + " exceeds recommended quantity (" + recommendedQuantity
                        + "). Please reduce the requested quantity.";
            }

            pc.setQuantity(requestQty);
            updated = true;
        }

        if (!updated) {
            return "No matching parts found in this warranty claim to update.";
        }

        partClaimRepository.saveAll(partClaims);

        return "Part quantities updated successfully.";
    }

    public String handleSubmitPartClaims(long claimId, long userId, List<PartClaimRequest> requests) {
        for (PartClaimRequest pc : requests) {
            PartClaim partClaim = new PartClaim();
            WarrantyClaim claim = warrantyClaimRepository.findById(claimId)
                    .orElseThrow(() -> new NoSuchElementException("Warranty claim not found: " + claimId));
            partClaim.setWarrantyClaim(claim);

            Part part = partInventoryRepository.findById(pc.getId())
                    .orElseThrow(() -> new NoSuchElementException("Part not found with ID: " + pc.getId()))
                    .getPart();

            partClaim.setPart(part);

            partClaim.setQuantity(pc.getQuantity());

            partClaimRepository.save(partClaim);
        }
        return "Part claims submitted successfully.";
    }
}