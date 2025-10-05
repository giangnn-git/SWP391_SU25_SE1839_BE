package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DashboardOrderSummaryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.SummaryItemResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairOrder;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairOrderReposity;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.RepairOrderService;

import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepairOrderServiceImpl implements RepairOrderService {

    private final RepairOrderReposity repairOrderReposity;

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
}