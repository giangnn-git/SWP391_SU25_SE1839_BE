package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DashboardOrderSummaryResponse;

@Service
public interface RepairOrderService {
    DashboardOrderSummaryResponse findSunSummaryOrder(long serviceCenterId);
}
