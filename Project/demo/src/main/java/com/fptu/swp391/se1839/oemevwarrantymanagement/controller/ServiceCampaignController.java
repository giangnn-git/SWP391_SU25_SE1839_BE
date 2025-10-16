package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CreateCampaignRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.FilterRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.GetAllCampaignResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ServiceCampaignDetailResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ServiceCampaignResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.CampaignService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
public class ServiceCampaignController {
        final CampaignService serviceCampaignService;

        @GetMapping("/campaigns")
        @PreAuthorize("hasAnyAuthority('ADMIN','EVM_STAFF')")
        public ResponseEntity<ApiResponse<GetAllCampaignResponse>> getAllCampaigns(
                        @AuthenticationPrincipal Jwt jwt) {

                GetAllCampaignResponse campaigns = serviceCampaignService.handleGetAllCampaigns();
                var result = ApiResponse.<GetAllCampaignResponse>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Get all campaigns successfully")
                                .data(campaigns)
                                .build();

                return ResponseEntity.ok(result);
        }

        @GetMapping("/campaigns/{id}")
        @PreAuthorize("hasAnyAuthority('ADMIN','EVM_STAFF')")
        public ResponseEntity<ApiResponse<ServiceCampaignDetailResponse>> getCampaignDetail(
                        @PathVariable Long id,
                        @AuthenticationPrincipal Jwt jwt) {

                ServiceCampaignDetailResponse detail = serviceCampaignService.handleGetCampaignById(id);
                var result = ApiResponse.<ServiceCampaignDetailResponse>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Get campaign detail successfully")
                                .data(detail)
                                .build();

                return ResponseEntity.ok(result);
        }

        @PostMapping("/campaigns")
        @PreAuthorize("hasAuthority('ADMIN')")
        public ResponseEntity<ApiResponse<ServiceCampaignResponse>> createCampaign(
                        @Validated @RequestBody CreateCampaignRequest request,
                        @AuthenticationPrincipal Jwt jwt) {

                ServiceCampaignResponse response = serviceCampaignService.handleCreateCampaign(request);
                var result = ApiResponse.<ServiceCampaignResponse>builder()
                                .status(HttpStatus.CREATED.toString())
                                .message("Campaign created successfully")
                                .data(response)
                                .build();

                return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }

        @DeleteMapping("/campaigns/{id}")
        @PreAuthorize("hasAuthority('ADMIN')")
        public ResponseEntity<ApiResponse<Void>> deleteCampaign(
                        @PathVariable Long id,
                        @AuthenticationPrincipal Jwt jwt) {

                serviceCampaignService.handleDeleteCampaign(id);
                var result = ApiResponse.<Void>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Campaign deleted successfully")
                                .build();

                return ResponseEntity.ok(result);
        }

}
