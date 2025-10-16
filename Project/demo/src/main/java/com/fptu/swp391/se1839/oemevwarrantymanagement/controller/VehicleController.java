package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.FilterRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ClaimDashboardResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.VehicleService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
public class VehicleController {
    final VehicleService vehicleService;

    @GetMapping("/claims")
        public ResponseEntity<ApiResponse<ClaimDashboardResponse>> claimSummary(
                        @AuthenticationPrincipal Jwt jwt,
                        @RequestBody(required = false) FilterRequest request) {
                Long serviceCenterId = Long.parseLong(jwt.getClaim("serviceCenterId").toString());

                if (request == null) {
                        request = new FilterRequest();
                }

                Long userId = Long.parseLong(jwt.getClaim("userId").toString());

                ClaimDashboardResponse cdr = this.warrantyClaimService.handleClaimDashboard(serviceCenterId, request,
                                userId);

                var result = ApiResponse.<ClaimDashboardResponse>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Get claim summary successfully")
               
}
}