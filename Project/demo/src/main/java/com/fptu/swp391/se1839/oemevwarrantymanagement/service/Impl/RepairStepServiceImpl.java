package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CreateRepairStepRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ChangeStatusRepairStepResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.CreateRepairStepResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairOrder;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairStep;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairOrderRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairStepRepository;
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
            case PENDING -> nextStatuses.addAll(List.of("WAITING", "IN_PROGRESS", "CANCELLED"));
            case WAITING -> nextStatuses.addAll(List.of("IN_PROGRESS", "CANCELLED"));
            case IN_PROGRESS -> nextStatuses.addAll(List.of("WAITING", "COMPLETED", "CANCELLED"));
            default -> {
            } // COMPLETED hoặc CANCELLED -> không đổi
        }
        return nextStatuses;
    }

    // --- Tạo RepairStep ---
    public CreateRepairStepResponse handleCreateRepairStep(CreateRepairStepRequest request, long repairOrderId) {
        RepairOrder ro = repairOrderReposity.findById(repairOrderId)
                .orElseThrow(() -> new NoSuchElementException("This repair order does not exist: " + repairOrderId));

        long countRepairStep = repairStepRepository.countByRepairOrderId(repairOrderId);

        RepairStep rs = RepairStep.builder()
                .assignedTechnician(ro.getTechnical().getName())
                .estimatedHours(request.getEstimatedHours())
                .actualHours(0.0)
                .title("B" + countRepairStep + " " + request.getDescription())
                .repairOrder(ro)
                .status(RepairStep.StepStatus.PENDING) // mặc định PENDING
                .build();

        repairStepRepository.save(rs);

        return CreateRepairStepResponse.builder()
                .id(rs.getId())
                .percent(calculatePercent(repairOrderId))
                .message("Create successfully")
                .build();
    }

    @Override
    public ChangeStatusRepairStepResponse changeStepStatus(long repairStepId, String newStatusStr) {
        RepairStep rs = repairStepRepository.findById(repairStepId)
                .orElseThrow(() -> new NoSuchElementException("Repair step does not exist: " + repairStepId));

        RepairOrder order = rs.getRepairOrder();
        LocalDateTime now = LocalDateTime.now();

        RepairStep.StepStatus newStatus;
        try {
            newStatus = RepairStep.StepStatus.valueOf(newStatusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + newStatusStr);
        }

        // Kiểm tra trạng thái hợp lệ
        Set<String> allowed = getNextStepStatuses(rs);
        if (!allowed.contains(newStatus.name())) {
            throw new IllegalArgumentException("Cannot change status from " + rs.getStatus() + " to " + newStatus);
        }

        // Xử lý thời gian và actualHours
        switch (newStatus) {
            case IN_PROGRESS:
                if (rs.getStartTime() == null)
                    rs.setStartTime(now);
                break;
            case COMPLETED:
                if (rs.getStartTime() == null)
                    rs.setStartTime(now);
                rs.setEndTime(now);
                double hoursWorked = Duration.between(rs.getStartTime(), rs.getEndTime()).toMinutes() / 60.0;
                rs.setActualHours(Math.round(hoursWorked * 100.0) / 100.0);
                break;
            case WAITING:
                // giữ startTime, chưa set endTime
                break;
            default:
                // CANCELLED hoặc các trạng thái khác
                break;
        }

        // Cập nhật status step
        rs.setStatus(newStatus);
        repairStepRepository.save(rs);

        // --- Đồng bộ trạng thái RepairOrder ---
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
            double totalHours = stepsOfOrder.stream().mapToDouble(RepairStep::getActualHours).sum();
            order.setEndTime((int) Math.round(totalHours));
            if (order.getStartDate() == null && !stepsOfOrder.isEmpty()) {
                order.setStartDate(stepsOfOrder.get(0).getStartTime());
            }
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

        // Tính % hoàn thành
        int percent = (int) ((completed * 100.0) / totalSteps);

        // Trả về response
        return ChangeStatusRepairStepResponse.builder()
                .id(rs.getId())
                .status(rs.getStatus().name())
                .hoursWorked(rs.getActualHours())
                .percent(percent)
                .build();
    }

}
