package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import java.util.List;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.RepairOrderDurationStatsResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ServiceCenterPerformanceResponse;

public interface ReportService {
    List<ServiceCenterPerformanceResponse> getServiceCenterPerformance();
    RepairOrderDurationStatsResponse getCompletedOrderDurationStats();
}
