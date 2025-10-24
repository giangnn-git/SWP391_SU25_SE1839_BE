package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.ChooseTechnicalRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.FilterRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ChooseTechnicalResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DashboardOrderSummaryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.FilterOrderResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.OrderDashboardResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.SummaryItemResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.SummaryOrderResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Model;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairOrder;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairOrder.OrderStatus;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairStep;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ServiceCenter;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.User;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Vehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.ModelRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairOrderRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairStepRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.ServiceCenterRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.UserRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.VehicleRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.WarrantyClaimRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.RepairOrderService;

import jakarta.persistence.PersistenceException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RepairOrderServiceImpl implements RepairOrderService {

    final RepairOrderRepository repairOrderRepository;
    final VehicleRepository vehicleRepository;
    final ServiceCenterRepository serviceCenterRepository;
    final WarrantyClaimRepository warrantyClaimRepository;
    final ModelRepository modelRepository;
    final RepairStepRepository repairStepRepository;
    final UserRepository userRepository;

    Vehicle getVehicleByVin(String vin) {
        Vehicle vehicle = this.vehicleRepository.findByVin(vin)
                .orElseThrow(() -> new NoSuchElementException("Vehicle not found with vin " + vin));
        return vehicle;
    }

    WarrantyClaim getWarrantyClaimId(long id) {
        return this.warrantyClaimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Warranty claim not found with id " + id));
    }

    ServiceCenter getServiceCenterById(long id) {
        return this.serviceCenterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Service center not found with id " + id));
    }

    SummaryItemResponse calculateSummary(long current, long previous) {
        long percentage;
        String message;

        if (previous == 0) {
            percentage = current > 0 ? 100 : 0;
            message = current > 0 ? "Increase" : "No Change";
        } else if (current > previous) {
            percentage = (current - previous) * 100 / previous;
            message = "Increase";
        } else {
            percentage = (previous - current) * 100 / previous;
            message = "Decrease";
        }

        return new SummaryItemResponse(percentage, message);
    }

    SummaryItemResponse calculateWeekSummary(long serviceCenterId) {
        LocalDate currentStart = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate currentEnd = LocalDate.now().with(DayOfWeek.SUNDAY);

        long currentWeek = repairOrderRepository.countRepairFlWeek(serviceCenterId, currentStart, currentEnd);

        LocalDate prevStart = currentStart.minusWeeks(1);
        LocalDate prevEnd = currentEnd.minusWeeks(1);

        long previousWeek = repairOrderRepository.countRepairFlWeek(serviceCenterId, prevStart, prevEnd);

        return calculateSummary(currentWeek, previousWeek);
    }

    SummaryItemResponse calculateMonthSummary(long serviceCenterId) {
        LocalDate startMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        long currentMonth = repairOrderRepository.countRepairFlStatusAndMonth(
                serviceCenterId, RepairOrder.OrderStatus.COMPLETED, startMonth, endMonth);

        LocalDate prevStart = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate prevEnd = LocalDate.now().minusMonths(1)
                .withDayOfMonth(LocalDate.now().minusMonths(1).lengthOfMonth());

        long previousMonth = repairOrderRepository.countRepairFlStatusAndMonth(
                serviceCenterId, RepairOrder.OrderStatus.COMPLETED, prevStart, prevEnd);

        return calculateSummary(currentMonth, previousMonth);
    }

    @Override
    public DashboardOrderSummaryResponse findSunSummaryOrder(long serviceCenterId) {
        boolean status = true;
        long countOrderOneWeek = 0;
        SummaryItemResponse weekResult = new SummaryItemResponse(0, "No Data");
        SummaryItemResponse monthResult = new SummaryItemResponse(0, "No Data");

        try {
            countOrderOneWeek = this.repairOrderRepository.countRepairFlStatus(
                    serviceCenterId, RepairOrder.OrderStatus.IN_PROGRESS);

            weekResult = calculateWeekSummary(serviceCenterId);
            monthResult = calculateMonthSummary(serviceCenterId);

        } catch (DataAccessException | PersistenceException e) {
            log.error("Error while calculating dashboard summary", e);
            status = false;
        }

        return DashboardOrderSummaryResponse.builder()
                .countOrderInOneWeek(countOrderOneWeek)
                .differenceOneWeek(weekResult.getPercentage())
                .completeOneMonth(monthResult.getPercentage())
                .differenceOneMonth(monthResult.getPercentage())
                .status(status)
                .build();
    }

    long getOrderByStatus(long serviceCenterId, RepairOrder.OrderStatus status) {
        return repairOrderRepository.countByServiceCenterIdAndStatus(serviceCenterId, status);
    }

    double countAvgComplete(List<RepairOrder> roList) {
        List<RepairOrder> repairOrders = new ArrayList<>();
        for (int i = 0; i < roList.size(); i++) {
            if (roList.get(i).getStatus() == RepairOrder.OrderStatus.COMPLETED) {
                repairOrders.add(roList.get(i));
            }
        }

        long totalDays = 0;
        int count = 0;

        for (RepairOrder ro : repairOrders) {
            if (ro.getEndDate() != null && ro.getStartDate() != null) {
                long daysBetween = Duration.between(ro.getStartDate(), ro.getEndDate()).toDays();
                totalDays += daysBetween;
                count++;
            }
        }

        return count > 0 ? (double) totalDays / count : 0;
    }

    public double calculateProgress(Long repairOrderId) {
        List<RepairStep> steps = repairStepRepository.findByRepairOrderId(repairOrderId);

        if (steps.isEmpty())
            return 0;

        long total = steps.size();
        long completed = steps.stream()
                .filter(d -> d.getStatus() == RepairStep.StepStatus.COMPLETED
                        || d.getStatus() == RepairStep.StepStatus.CANCELLED)
                .count();

        return (completed * 100.0) / total;
    }

    SummaryOrderResponse handleSummary(long serviceCenterId, List<RepairOrder> roList) {
        long countInProcess = getOrderByStatus(serviceCenterId, RepairOrder.OrderStatus.IN_PROGRESS);
        long countInWaiting = getOrderByStatus(serviceCenterId, RepairOrder.OrderStatus.WAITING);
        long countInComplete = getOrderByStatus(serviceCenterId, RepairOrder.OrderStatus.COMPLETED);
        double avgDays = countAvgComplete(roList);

        return SummaryOrderResponse.builder()
                .active(new SummaryItemResponse(countInProcess, "In-process order"))
                .complete(new SummaryItemResponse(countInComplete, "Complete order"))
                .waitting(new SummaryItemResponse(countInWaiting, "Waitting order"))
                .avgComplete(new SummaryItemResponse((long) avgDays, "Avg complete order"))
                .status(true)
                .build();
    }

    List<FilterOrderResponse> handleFilterOrder(List<RepairOrder> roList) {
        List<FilterOrderResponse> forList = new ArrayList<>();

        for (RepairOrder ro : roList) {
            double progress = calculateProgress(ro.getId());

            if (progress == 0) {
                ro.setStatus(RepairOrder.OrderStatus.WAITING);
            } else if (progress < 100) {
                ro.setStatus(RepairOrder.OrderStatus.IN_PROGRESS);
            } else {
                ro.setStatus(RepairOrder.OrderStatus.COMPLETED);
                ro.setEndDate(LocalDateTime.now());
            }
            repairOrderRepository.save(ro);

            String technicalName = ro.getTechnical() != null ? ro.getTechnical().getName() : "Unknown";

            WarrantyClaim claim = getWarrantyClaimId(ro.getWarrantyClaim().getId());
            Vehicle vehicle = getVehicleByVin(claim.getVehicle().getVin());
            Model model = modelRepository.findById(vehicle.getModel().getId())
                    .orElseThrow(() -> new RuntimeException("Model not found"));

            FilterOrderResponse response = FilterOrderResponse.builder()
                    .repairOrderId(ro.getId())
                    .percentInProcess(progress)
                    .techinal(technicalName)
                    .prodcutYear(vehicle.getProductYear())
                    .vin(vehicle.getVin())
                    .licensePlate(vehicle.getLicensePlate())
                    .modelName(model.getName())
                    .status(true)
                    .orderDate(ro.getStartDate()) // sử dụng startDate để sắp xếp
                    .build();

            forList.add(response);
        }

        forList.sort(Comparator.comparing(FilterOrderResponse::getOrderDate).reversed());

        return forList;
    }

    @Override
    public OrderDashboardResponse handleOrderDashboard(long serviceCenterId, FilterRequest request, Long userId) {
        List<RepairOrder> roList = new ArrayList<>();
        List<FilterOrderResponse> responseList = new ArrayList<>();
        SummaryOrderResponse sor = new SummaryOrderResponse();
        ServiceCenter sc = getServiceCenterById(serviceCenterId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        RepairOrder.OrderStatus statusEnum = null;
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            statusEnum = RepairOrder.OrderStatus.valueOf(request.getStatus().toUpperCase());
        }

        if (request.getKeyword() == null && request.getStatus() == null) {
            if (user.getRole() == User.Role.TECHNICIAN) {
                roList = repairOrderRepository.findByServiceCenterIdAndUserId(sc.getId(), userId);
            } else {
                roList = repairOrderRepository.findByServiceCenterId(sc.getId());
            }
        } else if (request.getKeyword() == null && request.getStatus() != null) {
            if (user.getRole() == User.Role.TECHNICIAN) {
                roList = repairOrderRepository.findByServiceCenterIdAndStatusAndUserId(sc.getId(), statusEnum, userId);
            } else {
                roList = repairOrderRepository.findByServiceCenterIdAndStatus(sc.getId(), statusEnum);
            }
        } else if (request.getKeyword() != null && request.getStatus() == null) {
            if (user.getRole() == User.Role.TECHNICIAN) {
                roList = repairOrderRepository.findByServiceCenterIdAndVehicleVinAndUserId(sc.getId(),
                        request.getKeyword(), userId);
            } else {
                roList = repairOrderRepository.findByServiceCenterIdAndVehicleVin(sc.getId(), request.getKeyword());
            }
            if (roList == null || roList.isEmpty()) {
                if (user.getRole() == User.Role.TECHNICIAN) {
                    roList = repairOrderRepository.findByCustomerNameAndUserId(sc.getId(),
                            request.getKeyword(), userId);
                } else {
                    roList = repairOrderRepository.findByCustomerName(sc.getId(), request.getKeyword());
                }
            }
        } else {
            if (user.getRole() == User.Role.TECHNICIAN) {
                roList = repairOrderRepository.findByServiceCenterIdAndVehicleVinAndStatusAndUserId(sc.getId(),
                        request.getKeyword(), statusEnum, userId);
            } else {
                roList = repairOrderRepository.findByServiceCenterIdAndVehicleVinAndStatus(sc.getId(),
                        request.getKeyword(),
                        statusEnum);
            }
            if (roList == null || roList.isEmpty()) {
                if (user.getRole() == User.Role.TECHNICIAN) {
                    roList = repairOrderRepository.findByServiceCenterIdAndCustomerNameAndStatusAndUserId(sc.getId(),
                            request.getKeyword(), statusEnum, userId);
                } else {
                    roList = repairOrderRepository.findByServiceCenterIdAndCustomerNameAndStatus(sc.getId(),
                            request.getKeyword(), statusEnum);
                }
            }
        }

        responseList = handleFilterOrder(roList);
        sor = handleSummary(sc.getId(), roList);

        return OrderDashboardResponse.builder()
                .fors(responseList)
                .sor(sor)
                .build();
    }

    public ChooseTechnicalResponse handleChooseTechinical(long repairOrderId, ChooseTechnicalRequest request) {
        System.out.println(">>> Debug: finding repair order id=" + repairOrderId);
        var repairOrder = repairOrderRepository.findById(repairOrderId)
                .orElseThrow(() -> new NoSuchElementException("This repair order is not exist " + repairOrderId));

        System.out.println(">>> Found repair order id=" + repairOrder.getId());

        User technical = userRepository.findByName(request.getTechnicalName());
        if (technical == null) {
            throw new NoSuchElementException("Technician not found: " + request.getTechnicalName());
        }

        repairOrder.setTechnical(technical);
        repairOrder.setEstimated(request.getEstimated());
        repairOrder.setStartDate(request.getStartDate());
        repairOrder.setEndDate(request.getEndDate());
        repairOrder.setStatus(OrderStatus.PENDING);
        repairOrderRepository.save(repairOrder);

        return ChooseTechnicalResponse.builder()
                .message("Choose successfully")
                .status(true)
                .build();
    }

    public FilterOrderResponse handleGetDetailOrder(long orderId) {
        RepairOrder ro = this.repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Not find repair order"));

        String technicalName = (ro.getTechnical() != null) ? ro.getTechnical().getName() : null;

        return FilterOrderResponse.builder()
                .repairOrderId(orderId)
                .prodcutYear(ro.getWarrantyClaim().getVehicle().getProductYear())
                .modelName(ro.getWarrantyClaim().getVehicle().getModel().getName())
                .vin(ro.getWarrantyClaim().getVehicle().getVin())
                .licensePlate(ro.getWarrantyClaim().getVehicle().getLicensePlate())
                .techinal(technicalName)
                .percentInProcess(calculateProgress(orderId))
                .build();
    }

}