package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.ChooseTechnicalRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.FilterRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ChooseTechnicalResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DashboardOrderSummaryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.OrderDashboardResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.OrderDetailResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.OrderSummaryResponse;

public interface RepairOrderService {
    DashboardOrderSummaryResponse findSunSummaryOrder(long serviceCenterId);

    OrderDashboardResponse handleOrderDashboard(long serviceCenterId,
            FilterRequest request, Long userId);

    ChooseTechnicalResponse handleChooseTechnical(long repairOrderId, ChooseTechnicalRequest request);

    OrderDetailResponse handleGetDetailOrder(long serviceCenterId, long orderId);

    int handleCalculateResponseScore(long serviceCenterId);

    int handleCalculatePerformanceMetrics(long serviceCenterId);

    int handleCalculateOverdueRepairs(long serviceCenterId);

    int hanldeCalculateCompleteToday(long serviceCenterId);

    double handleCalculateAvgDays(long serviceCenterId);

    int handleCalculateResolutionRate(long serviceCenterId);

    OrderSummaryResponse handleCalculateResolutionRateDifferent(long serviceCenterId);
}
