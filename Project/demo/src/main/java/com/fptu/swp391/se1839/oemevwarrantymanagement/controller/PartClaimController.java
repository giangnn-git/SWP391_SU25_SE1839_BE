package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.AllPartClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.ChangeStatusPartClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.GetPartClaimResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.PartClaimService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
public class PartClaimController {

        final PartClaimService partClaimService;

        @PutMapping("/claims/partClaims/{id}")
        public ResponseEntity<ApiResponse<String>> addPartClaim(@PathVariable("id") long claimId,
                        @RequestBody AllPartClaimRequest request, @AuthenticationPrincipal Jwt jwt) {
                String partClaim = this.partClaimService.handleCreatePartClaim(request, claimId);
                var result = ApiResponse.<String>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Add part claim successfully")
                                .data(partClaim)
                                .build();
                return ResponseEntity.ok(result);
        }

        @GetMapping("/{claimId}/part-claims")
        public ResponseEntity<ApiResponse<List<GetPartClaimResponse>>> getPartClaimAndCampaign(
                        @PathVariable("id") long claimId, @AuthenticationPrincipal Jwt jwt) {
                List<GetPartClaimResponse> partClaim = this.partClaimService.handleGetPartClaim(claimId);
                var result = ApiResponse.<List<GetPartClaimResponse>>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Get part claim successfully")
                                .data(partClaim)
                                .build();
                return ResponseEntity.ok(result);
        }

        @PutMapping("/claimId/part-claims/{partClaimId}/status")
        public ResponseEntity<ApiResponse<String>> changeStatusPartClaim(@PathVariable("id") long claimId,
                        @RequestBody ChangeStatusPartClaimRequest request,
                        @PathVariable("partClaimId") long partClaimId, @AuthenticationPrincipal Jwt jwt) {
                String partClaim = this.partClaimService.handleChangeStatusPartClaim(request, claimId, partClaimId);
                var result = ApiResponse.<String>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Change status part claim successfully")
                                .data(partClaim)
                                .build();
                return ResponseEntity.ok(result);
        }
}
