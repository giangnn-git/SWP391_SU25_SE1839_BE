package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CreateClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.FilterRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ClaimDashboardResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.CreateClaimResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.WarrantyClaimService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarrantyClaimController {

        private final WarrantyClaimService warrantyClaimService;

        @PostMapping(value = "/claims", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiResponse<CreateClaimResponse>> createClaim(
                        @RequestPart("claim") CreateClaimRequest request, // trực tiếp map JSON vào object
                        @RequestPart(value = "attachments", required = false) MultipartFile[] attachments,
                        @AuthenticationPrincipal Jwt jwt) throws IOException {

                Long serviceCenterId = Long.parseLong(jwt.getClaim("serviceCenterId").toString());

                var warrantyClaim = this.warrantyClaimService.handleCreateClaim(request,
                                serviceCenterId, attachments);
                var result = ApiResponse.<CreateClaimResponse>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Create claim successfully")
                                .data(warrantyClaim)
                                .build();
                return ResponseEntity.ok(result);
        }

        @GetMapping("/claims")
        public ResponseEntity<ApiResponse<ClaimDashboardResponse>> claimSummary(@AuthenticationPrincipal Jwt jwt,
                        @RequestBody FilterRequest request) {
                Long serviceCenterId = Long.parseLong(jwt.getClaim("serviceCenterId").toString());

                ClaimDashboardResponse cdr = this.warrantyClaimService.handleClaimDashboard(serviceCenterId, request);
                var result = ApiResponse.<ClaimDashboardResponse>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Get calim summary successfully")
                                .data(cdr)
                                .build();
                return ResponseEntity.ok(result);
        }

}
