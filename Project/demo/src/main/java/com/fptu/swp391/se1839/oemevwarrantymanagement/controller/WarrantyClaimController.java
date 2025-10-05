package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import java.io.IOException;
import java.util.NoSuchElementException;

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
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.FilterClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.CreateClaimResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.FilterClaimResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.SummaryClaimResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.User;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.UserService;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.WarrantyClaimService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarrantyClaimController {

    private final WarrantyClaimService warrantyClaimService;
    private final UserService userService;

    @PostMapping(value = "/claims", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CreateClaimResponse>> createClaim(
            @RequestPart("claim") CreateClaimRequest request, // trực tiếp map JSON vào object
            @RequestPart(value = "attachments", required = false) MultipartFile[] attachments,
            @AuthenticationPrincipal Jwt jwt) throws IOException {

        String account = jwt.getSubject();
        User user = this.userService.handdleFindByEmailOrPhone(account);

        if (user == null) {
            throw new NoSuchElementException("User doesn't exist");
        }

        Long serviceCenterId = user.getServiceCenter() != null ? user.getServiceCenter().getId() : null;
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
    public ResponseEntity<ApiResponse<SummaryClaimResponse>> claimSummary(@AuthenticationPrincipal Jwt jwt) {
        String account = jwt.getSubject();
        User user = this.userService.handdleFindByEmailOrPhone(account);

        if (user == null) {
            throw new NoSuchElementException("User doesn't exist");
        }

        Long serviceCenterId = user.getServiceCenter() != null ? user.getServiceCenter().getId() : null;
        SummaryClaimResponse summaryClaimResponse = this.warrantyClaimService.handleSummaryClaim(serviceCenterId);
        var result = ApiResponse.<SummaryClaimResponse>builder()
                .status(HttpStatus.OK.toString())
                .message("Get calim summary successfully")
                .data(summaryClaimResponse)
                .build();
        return ResponseEntity.ok(result);
    }

    // @GetMapping("/claims/filter")
    // public ResponseEntity<ApiResponse<FilterClaimResponse>>
    // claimSummary(@AuthenticationPrincipal Jwt jwt,
    // @RequestBody FilterClaimRequest request) {
    // String account = jwt.getSubject();
    // User user = this.userService.handdleFindByEmailOrPhone(account);

    // if (user == null) {
    // throw new NoSuchElementException("User doesn't exist");
    // }

    // Long serviceCenterId = user.getServiceCenter() != null ?
    // user.getServiceCenter().getId() : null;
    // SummaryClaimResponse summaryClaimResponse =
    // this.warrantyClaimService.handleSummaryClaim(serviceCenterId);
    // var result = ApiResponse.<SummaryClaimResponse>builder()
    // .status(HttpStatus.OK.toString())
    // .message("Get calim summary successfully")
    // .data(summaryClaimResponse)
    // .build();
    // return ResponseEntity.ok(result);
    // }

}
