package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DashboardResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DashboardClaimSummaryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DashboardOrderSummaryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.RepairOrderService;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.WarrantyClaimService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
public class DashboardController {
    private final RepairOrderService repairOrderService;
    private final WarrantyClaimService warrantyClaimService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboardSummary(@AuthenticationPrincipal Jwt jwt) {

        Long serviceCenterId = Long.parseLong(jwt.getClaim("serviceCenterId").toString());

        DashboardOrderSummaryResponse orderSummary = repairOrderService.findSunSummaryOrder(serviceCenterId);
        DashboardClaimSummaryResponse claimSummary = warrantyClaimService.handleSummaryClaims(serviceCenterId);

        Map<String, Object> data = new HashMap<>();
        data.put("orders", orderSummary);
        data.put("claims", claimSummary);

        DashboardResponse dashboardResponse = DashboardResponse.builder()
                .dashboarMap(data)
                .build();
        var result = ApiResponse.<DashboardResponse>builder()
                .status(HttpStatus.OK.toString())
                .message("Dashboard summary fetched successfully")
                .data(dashboardResponse)
                .build();

        return ResponseEntity.ok(result);
    }
}
