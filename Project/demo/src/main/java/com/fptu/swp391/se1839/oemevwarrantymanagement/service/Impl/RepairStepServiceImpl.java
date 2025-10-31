package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.ChangeStatusRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ChangeStatusRepairStepResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.GetRepairStepResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Part;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairDetail;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairOrder;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairStep;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Vehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.VehiclePart;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.event.EntityUpdatedEvent;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartInventoryRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairDetailRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairOrderRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairStepRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.VehiclePartRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.WarrantyClaimRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.RepairStepService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RepairStepServiceImpl implements RepairStepService {
    final RepairStepRepository repairStepRepository;
    final RepairOrderRepository repairOrderReposity;
    final PartRepository partRepository;
    final VehiclePartRepository vehiclePartRepository;
    final PartInventoryRepository partInventoryRepository;
    final RepairDetailRepository repairDetailRepository;
    final WarrantyClaimRepository warrantyClaimRepository;
    final ApplicationEventPublisher applicationEventPublisher;

    // --- Tính % hoàn thành tất cả step ---
    int calculatePercent(long repairOrderId) {
        List<RepairStep> steps = repairStepRepository.findByRepairOrderId(repairOrderId);
        if (steps.isEmpty())
            return 0;
        long completedCount = steps.stream()
                .filter(s -> s.getStatus() == RepairStep.StepStatus.COMPLETED)
                .count();
        return (int) Math.ceil(((double) completedCount / steps.size()) * 100);
    }

    // --- Lấy các trạng thái tiếp theo có thể chọn ---
    Set<String> getNextStepStatuses(RepairStep step) {
        Set<String> nextStatuses = new LinkedHashSet<>();
        switch (step.getStatus()) {
            case PENDING -> nextStatuses.addAll(List.of("IN_PROGRESS", "CANCELLED"));
            case WAITING -> nextStatuses.addAll(List.of("IN_PROGRESS", "CANCELLED"));
            case IN_PROGRESS -> nextStatuses.addAll(List.of("COMPLETED", "CANCELLED"));
            default -> {
            } // COMPLETED hoặc CANCELLED -> không đổi
        }
        return nextStatuses;
    }

    // --- Lấy danh sách RepairStep ---
    public List<GetRepairStepResponse> handleGetRepairStep(long repairOrderId) {
        RepairOrder repairOrder = repairOrderReposity.findById(repairOrderId)
                .orElseThrow(() -> new NoSuchElementException("Repair order does not exist: " + repairOrderId));

        List<GetRepairStepResponse> responses = new ArrayList<>();

        if (repairOrder.getTechnical() != null) {
            List<RepairStep> steps = repairStepRepository.findByRepairOrderId(repairOrder.getId())
                    .stream()
                    .filter(step -> step.getStatus() != RepairStep.StepStatus.REJECTED)
                    .toList();

            // Separate the last 2 steps
            List<RepairStep> finalSteps = steps.stream()
                    .filter(step -> step.getTitle().contains("Operation Check")
                            || step.getTitle().contains("Repair Completion"))
                    .toList();

            // Other steps (sorted by id)
            List<RepairStep> otherSteps = steps.stream()
                    .filter(step -> !finalSteps.contains(step))
                    .sorted(Comparator.comparing(RepairStep::getId))
                    .toList();

            int stepNumber = 1;

            // Add the other steps
            for (RepairStep step : otherSteps) {
                responses.add(GetRepairStepResponse.builder()
                        .stepId(step.getId())
                        .title("Step " + stepNumber + ": " + step.getTitle())
                        .estimatedHour(step.getEstimatedHours() != null ? step.getEstimatedHours() : 0.0)
                        .actualHour(step.getActualHours() != null ? step.getActualHours() : 0.0)
                        .status(step.getStatus().name())
                        .nextStatuses(getNextStepStatuses(step))
                        .build());
                stepNumber++;
            }

            // Add the last 2 steps
            for (RepairStep step : finalSteps) {
                responses.add(GetRepairStepResponse.builder()
                        .stepId(step.getId())
                        .title("Step " + stepNumber + ": " + step.getTitle())
                        .estimatedHour(step.getEstimatedHours() != null ? step.getEstimatedHours() : 0.0)
                        .actualHour(step.getActualHours() != null ? step.getActualHours() : 0.0)
                        .status(step.getStatus().name())
                        .nextStatuses(getNextStepStatuses(step))
                        .build());
                stepNumber++;
            }
        }

        return responses;
    }

    @Override
    public ChangeStatusRepairStepResponse changeStepStatus(long repairStepId, ChangeStatusRequest newStatusStr) {
        RepairStep rs = getRepairStep(repairStepId);
        RepairOrder order = rs.getRepairOrder();
        LocalDateTime now = LocalDateTime.now();

        RepairStep.StepStatus newStatus = parseStepStatus(newStatusStr);

        validateStepStatus(rs, newStatus);

        handleStepTime(rs, newStatus, now);

        if (newStatus == RepairStep.StepStatus.COMPLETED || newStatus == RepairStep.StepStatus.CANCELLED) {
            handlePartIfNeeded(rs, newStatus);
        }

        rs.setStatus(newStatus);
        repairStepRepository.save(rs);

        applicationEventPublisher.publishEvent(new EntityUpdatedEvent<>(this, rs));

        syncRepairOrderStatus(order, now);

        int percent = calculateCompletionPercent(order);

        return ChangeStatusRepairStepResponse.builder()
                .id(rs.getId())
                .status(rs.getStatus().name())
                .hoursWorked(rs.getActualHours() != null ? rs.getActualHours() : 0.0)
                .percent(percent)
                .build();
    }

    // --- Hàm hỗ trợ ---
    private RepairStep getRepairStep(long repairStepId) {
        return repairStepRepository.findById(repairStepId)
                .orElseThrow(() -> new NoSuchElementException("Repair step does not exist: " + repairStepId));
    }

    private RepairStep.StepStatus parseStepStatus(ChangeStatusRequest newStatusStr) {
        try {
            return RepairStep.StepStatus.valueOf(newStatusStr.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + newStatusStr);
        }
    }

    private void validateStepStatus(RepairStep rs, RepairStep.StepStatus newStatus) {
        Set<String> allowed = getNextStepStatuses(rs);
        if (!allowed.contains(newStatus.name())) {
            throw new IllegalArgumentException("Cannot change status from " + rs.getStatus() + " to " + newStatus);
        }
    }

    private void handleStepTime(RepairStep rs, RepairStep.StepStatus newStatus, LocalDateTime now) {
        if (newStatus == RepairStep.StepStatus.IN_PROGRESS && rs.getStartTime() == null) {
            rs.setStartTime(now);
        } else if ((newStatus == RepairStep.StepStatus.COMPLETED || newStatus == RepairStep.StepStatus.CANCELLED)
                && rs.getStartTime() == null) {
            rs.setStartTime(now);
            rs.setEndTime(now);
            LocalDateTime start = rs.getStartTime();
            LocalDateTime end = rs.getEndTime();
            if (start != null && end != null) {
                double hoursWorked = Duration.between(start, end).toMinutes() / 60.0;
                rs.setActualHours(Math.round(hoursWorked * 100.0) / 100.0);
            } else {
                rs.setActualHours(0.0);
            }
        }
    }

    private void handlePartIfNeeded(RepairStep rs, RepairStep.StepStatus newStatus) {
        String titleLower = rs.getTitle().toLowerCase();
        if (titleLower.startsWith("remove damaged part") || titleLower.startsWith("install new part")) {
            RepairOrder order = rs.getRepairOrder();
            WarrantyClaim claim = order.getWarrantyClaim();
            Vehicle vehicle = claim.getVehicle();
            String partName = titleLower.contains("remove")
                    ? titleLower.substring("remove damaged part ".length()).trim()
                    : titleLower.substring("install new part ".length()).trim();

            Part part = partRepository.findByName(partName)
                    .orElseThrow(() -> new NoSuchElementException("Part not found: " + partName));

            if (newStatus == RepairStep.StepStatus.COMPLETED) {
                processCompletedPart(vehicle, claim, part);
            } else if (newStatus == RepairStep.StepStatus.CANCELLED) {
                markDetailUsed(rs.getRepairOrder(), part);
            }
        }
    }

    private void processCompletedPart(Vehicle vehicle, WarrantyClaim claim, Part part) {
        vehiclePartRepository.findActiveVehiclePart(vehicle, part).ifPresent(vp -> {
            vp.setRemovalDate(LocalDate.now());
            vehiclePartRepository.save(vp);
        });

        String serialNumber = "SC-" + vehicle.getVin() + "-" + part.getId() + "-" + System.currentTimeMillis();

        VehiclePart vp = vehiclePartRepository.findByVehicleVinAndPartIdAndWarrantyClaimId(
                vehicle.getVin(), part.getId(), claim.getId())
                .orElseThrow(() -> new NoSuchElementException(
                        "VehiclePart not found for vehicle " + vehicle.getVin() +
                                ", part " + part.getId() + ", claim " + claim.getId()));

        vp.setNewSerialNumber(serialNumber);
        vehiclePartRepository.save(vp);

        VehiclePart newVp = VehiclePart.builder()
                .part(part)
                .vehicle(vehicle)
                .warrantyClaim(claim)
                .oldSerialNumber(serialNumber)
                .installationDate(LocalDate.now())
                .build();
        vehiclePartRepository.save(newVp);

        claim.getPartClaims().stream()
                .filter(pc -> pc.getPart().getId().equals(part.getId()))
                .forEach(pc -> partInventoryRepository.findByPart_Id(pc.getPart().getId())
                        .ifPresent(pi -> {
                            pi.setQuantity(Math.max(pi.getQuantity() - (int) pc.getQuantity(), 0));
                            partInventoryRepository.save(pi);
                        }));

        repairDetailRepository.findByRepairOrderId(claim.getRepairOrder().getId()).stream()
                .filter(d -> d.getPart().getId().equals(part.getId()))
                .forEach(d -> {
                    d.setStatus(RepairDetail.DetailStatus.REPLACED);
                    d.setVehiclePart(newVp);
                    repairDetailRepository.save(d);
                });
    }

    private void markDetailUsed(RepairOrder order, Part part) {
        repairDetailRepository.findByRepairOrderId(order.getId()).stream()
                .filter(d -> d.getPart().getId().equals(part.getId()))
                .forEach(d -> {
                    d.setStatus(RepairDetail.DetailStatus.USED);
                    repairDetailRepository.save(d);
                });
    }

    private void syncRepairOrderStatus(RepairOrder order, LocalDateTime now) {
        List<RepairStep> stepsOfOrder = repairStepRepository.findByRepairOrderId(order.getId());

        long totalSteps = stepsOfOrder.size();
        long completed = stepsOfOrder.stream().filter(s -> s.getStatus() == RepairStep.StepStatus.COMPLETED).count();
        long inProgress = stepsOfOrder.stream().filter(s -> s.getStatus() == RepairStep.StepStatus.IN_PROGRESS).count();
        long waiting = stepsOfOrder.stream().filter(s -> s.getStatus() == RepairStep.StepStatus.WAITING
                || s.getStatus() == RepairStep.StepStatus.PENDING).count();
        long cancelled = stepsOfOrder.stream().filter(s -> s.getStatus() == RepairStep.StepStatus.CANCELLED).count();

        if (completed == totalSteps) {
            order.setStatus(RepairOrder.OrderStatus.COMPLETED);
            order.setEndDate(now);
            double totalHours = stepsOfOrder.stream()
                    .mapToDouble(s -> s.getActualHours() != null ? s.getActualHours() : 0.0)
                    .sum();
            order.setEndTime((int) Math.round(totalHours));
            if (order.getStartDate() == null && !stepsOfOrder.isEmpty()) {
                order.setStartDate(
                        stepsOfOrder.get(0).getStartTime() != null ? stepsOfOrder.get(0).getStartTime() : now);
            }

            applicationEventPublisher.publishEvent(new EntityUpdatedEvent<>(this, order));
        } else if (inProgress > 0) {
            order.setStatus(RepairOrder.OrderStatus.IN_PROGRESS);
        } else if (waiting == totalSteps) {
            order.setStatus(RepairOrder.OrderStatus.WAITING);
        } else if (cancelled == totalSteps) {
            order.setStatus(RepairOrder.OrderStatus.CANCELLED);
        } else {
            order.setStatus(RepairOrder.OrderStatus.PENDING);
        }

        repairOrderReposity.save(order);

        WarrantyClaim claim = order.getWarrantyClaim();
        if (claim != null) {
            boolean allDone = stepsOfOrder.stream()
                    .allMatch(s -> s.getStatus() == RepairStep.StepStatus.COMPLETED
                            || s.getStatus() == RepairStep.StepStatus.CANCELLED);
            if (allDone) {
                claim.setStatus(WarrantyClaim.ClaimStatus.COMPLETED);
                warrantyClaimRepository.save(claim);
                applicationEventPublisher.publishEvent(new EntityUpdatedEvent<>(this, claim));

            }
        }
    }

    private int calculateCompletionPercent(RepairOrder order) {
        List<RepairStep> stepsOfOrder = repairStepRepository.findByRepairOrderId(order.getId());
        long totalSteps = stepsOfOrder.size();
        long completed = stepsOfOrder.stream().filter(s -> s.getStatus() == RepairStep.StepStatus.COMPLETED).count();
        return totalSteps > 0 ? (int) ((completed * 100.0) / totalSteps) : 0;
    }

}