package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.ChooseTechnicalRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.FilterRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ChooseTechnicalResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DashboardOrderSummaryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.FilterOrderResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.OrderDashboardResponse;

public interface RepairOrderService {
    DashboardOrderSummaryResponse findSunSummaryOrder(long serviceCenterId);

    OrderDashboardResponse handleOrderDashboard(long serviceCenterId,
            FilterRequest request, Long userId);

    ChooseTechnicalResponse handleChooseTechinical(long repairOrderId, ChooseTechnicalRequest request);

    FilterOrderResponse handleGetDetailOrder(long orderId);
}
