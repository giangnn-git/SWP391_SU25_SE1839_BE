package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.RepairOrderDurationStatsResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ServiceCenterPerformanceResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.ReportService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportController {

    ReportService reportService;

    @GetMapping("/performances")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<List<ServiceCenterPerformanceResponse>>> getServiceCenterPerformance(
            @AuthenticationPrincipal Jwt jwt) {

        List<ServiceCenterPerformanceResponse> data = reportService.getServiceCenterPerformance();

        ApiResponse<List<ServiceCenterPerformanceResponse>> response = ApiResponse.<List<ServiceCenterPerformanceResponse>>builder()
                .status(HttpStatus.OK.toString())
                .message("Get service center performance successfully at " + LocalDateTime.now())
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/completed-duration")
    @PreAuthorize("hasAnyAuthority('ADMIN','EVM_STAFF')")
    public ResponseEntity<ApiResponse<RepairOrderDurationStatsResponse>> getCompletedOrderDurationStats() {

        RepairOrderDurationStatsResponse data = reportService.getCompletedOrderDurationStats();

        var response = ApiResponse.<RepairOrderDurationStatsResponse>builder()
                .status(HttpStatus.OK.toString())
                .message("Completed repair orders duration stats at " + LocalDateTime.now())
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }
}
