package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;


import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.oauth2.jwt.Jwt;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import com.fptu.swp391.se1839.oemevwarrantymanagement.domain.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.domain.User;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.SummaryClaimsResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.UserService;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.WarrantyClaimService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://orthopterous-unwieldable-kristal.ngrok-free.dev")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarrantyClaimController {

    private final WarrantyClaimService warrantyClaimService;
    private final UserService userService;

    @GetMapping("/claims/count")
    public ResponseEntity<ApiResponse<SummaryClaimsResponse>> summaryClaim(@AuthenticationPrincipal Jwt jwt) {
        // Lấy email từ claim "sub"
        String email = jwt.getSubject();

        // Lấy user từ database
        User user = this.userService.handdleFindByEmailOrPhone(email);

        if (user == null) {
            throw new NoSuchElementException("User doesn't exist");
        }

        // Lấy serviceCenterId
        Long serviceCenterId = user.getServiceCenter() != null ? user.getServiceCenter().getId() : null;

        // Đếm số claim
        var claimCount = this.warrantyClaimService.handleSummaryClaims(serviceCenterId);

        // Trả về response
        var result = new ApiResponse<>(HttpStatus.OK, "Claim count fetched successfully", claimCount,
                null);
        return ResponseEntity.ok(result);
    }
}

