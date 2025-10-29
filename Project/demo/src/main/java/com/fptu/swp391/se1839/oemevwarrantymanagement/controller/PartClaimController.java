package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.AllPartClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.PartClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.WarrantyClaimStatusRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.WarrantyClaimStatusResponse;
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
                .message("Change status claim successfully")
                .data(partClaim)
                .build();
        return ResponseEntity.ok(result);
    }
}
