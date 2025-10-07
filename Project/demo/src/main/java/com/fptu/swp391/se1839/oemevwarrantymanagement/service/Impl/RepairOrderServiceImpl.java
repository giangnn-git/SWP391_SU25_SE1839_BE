package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.FilterRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DashboardOrderSummaryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.FilterOrderResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.OrderDashboardResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.SummaryItemResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.SummaryOrderResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Model;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairDetail;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairOrder;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ServiceCenter;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Vehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.ModelRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairDetailRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairOrderReposity;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.ServiceCenterRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.VehicleRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.WarrantyClaimRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.RepairOrderService;

import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepairOrderServiceImpl implements RepairOrderService {

    private final RepairOrderReposity repairOrderReposity;
    private final VehicleRepository vehicleRepository;
    private final ServiceCenterRepository serviceCenterRepository;
    private final WarrantyClaimRepository warrantyClaimRepository;
    private final ModelRepository modelRepository;
    private final RepairDetailRepository repairDetailRepository;

    private Vehicle getVehicleByVin(String vin) {
        Vehicle vehicle = this.vehicleRepository.findByVin(vin)
                .orElseThrow(() -> new NoSuchElementException("Vehicle not found with vin " + vin));
        return vehicle;
    }

    private WarrantyClaim getWarrantyClaimId(long id) {
        return this.warrantyClaimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Warranty claim not found with id " + id));
    }

    private ServiceCenter getServiceCenterById(long id) {
        return this.serviceCenterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Service center not found with id " + id));
    }

    private SummaryItemResponse calculateSummary(long current, long previous) {
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

    private SummaryItemResponse calculateWeekSummary(long serviceCenterId) {
        LocalDate currentStart = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate currentEnd = LocalDate.now().with(DayOfWeek.SUNDAY);

        long currentWeek = repairOrderReposity.countRepairFlWeek(serviceCenterId, currentStart, currentEnd);

        LocalDate prevStart = currentStart.minusWeeks(1);
        LocalDate prevEnd = currentEnd.minusWeeks(1);

        long previousWeek = repairOrderReposity.countRepairFlWeek(serviceCenterId, prevStart, prevEnd);

        return calculateSummary(currentWeek, previousWeek);
    }

    private SummaryItemResponse calculateMonthSummary(long serviceCenterId) {
        LocalDate startMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        long currentMonth = repairOrderReposity.countRepairFlStatusAndMonth(
                serviceCenterId, RepairOrder.OrderStatus.COMPLETED, startMonth, endMonth);

        LocalDate prevStart = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate prevEnd = LocalDate.now().minusMonths(1)
                .withDayOfMonth(LocalDate.now().minusMonths(1).lengthOfMonth());

        long previousMonth = repairOrderReposity.countRepairFlStatusAndMonth(
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
            countOrderOneWeek = this.repairOrderReposity.countRepairFlStatus(
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

    private long getOrderByStatus(long serviceCenterId, RepairOrder.OrderStatus status) {
        return repairOrderReposity.countByServiceCenterIdAndStatus(serviceCenterId, status);
    }

    private double countAvgComplete(List<RepairOrder> roList) {
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
        List<RepairDetail> details = repairDetailRepository.findByRepairOrderId(repairOrderId);

        if (details.isEmpty())
            return 0;

        long total = details.size();
        long completed = details.stream()
                .filter(d -> d.getStatus() == RepairDetail.DetailStatus.REPLACED
                        || d.getStatus() == RepairDetail.DetailStatus.REJECTED)
                .count();

        return (completed * 100.0) / total;
    }

    private SummaryOrderResponse handleSummary(long serviceCenterId, List<RepairOrder> roList) {
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

    private List<FilterOrderResponse> handleFilterOrder(List<RepairOrder> roList) {
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
            repairOrderReposity.save(ro);

            String technicalName = ro.getTechnical() != null ? ro.getTechnical().getName() : "Unknown";

            WarrantyClaim claim = getWarrantyClaimId(ro.getWarrantyClaim().getId());
            Vehicle vehicle = getVehicleByVin(claim.getVehicle().getVin());
            Model model = modelRepository.findById(vehicle.getModel().getId())
                    .orElseThrow(() -> new RuntimeException("Model not found"));

            FilterOrderResponse response = FilterOrderResponse.builder()
                    .percentInProcess(progress)
                    .techinal(technicalName)
                    .prodcutYear(vehicle.getProductYear())
                    .vin(vehicle.getVin())
                    .modelName(model.getName())
                    .status(true)
                    .build();

            forList.add(response);
        }
        return forList;
    }

    @Override
    public OrderDashboardResponse handleOrderDashboard(long serviceCenterId,
            FilterRequest request) {
        List<RepairOrder> roList = new ArrayList<>();
        List<FilterOrderResponse> responseList = new ArrayList<>();
        SummaryOrderResponse sor = new SummaryOrderResponse();
        ServiceCenter sc = getServiceCenterById(serviceCenterId);
        RepairOrder.OrderStatus statusEnum = RepairOrder.OrderStatus
                .valueOf(request.getStatus().toUpperCase());

        if (request.getKeyword() == null && request.getStatus() == null) {
            roList = this.repairOrderReposity.findByServiceCenterId(sc.getId());
            responseList = handleFilterOrder(roList);
            sor = handleSummary(sc.getId(), roList);
        } else if (request.getKeyword() == null && request.getStatus() != null) {
            roList = this.repairOrderReposity.findByServiceCenterIdAndStatus(sc.getId(),
                    statusEnum);
            responseList = handleFilterOrder(roList);
            sor = handleSummary(sc.getId(), roList);
        } else if (request.getKeyword() != null && request.getStatus() == null) {
            roList = this.repairOrderReposity.findByServiceCenterIdAndVehicleVin(sc.getId(),
                    request.getKeyword());
            if (roList == null) {
                roList = this.repairOrderReposity.findByCustomerName(sc.getId(),
                        request.getKeyword());
            }
            responseList = handleFilterOrder(roList);
            sor = handleSummary(sc.getId(), roList);
        } else {
            roList = this.repairOrderReposity.findByServiceCenterIdAndVehicleVinAndStatus(sc.getId(),
                    request.getKeyword(), statusEnum);
            if (roList == null) {
                roList = this.repairOrderReposity.findByServiceCenterIdAndCustomerNameAndStatus(
                        sc.getId(),
                        request.getKeyword(), statusEnum);
            }
            responseList = handleFilterOrder(roList);
            sor = handleSummary(sc.getId(), roList);
        }
        return OrderDashboardResponse.builder()
                .fors(responseList)
                .sor(sor)
                .build();
    }
}