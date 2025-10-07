package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.FilterRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DashboardOrderSummaryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.OrderDashboardResponse;

@Service
public interface RepairOrderService {
    DashboardOrderSummaryResponse findSunSummaryOrder(long serviceCenterId);

    OrderDashboardResponse handleOrderDashboard(long serviceCenterId,
            FilterRequest request);
}
