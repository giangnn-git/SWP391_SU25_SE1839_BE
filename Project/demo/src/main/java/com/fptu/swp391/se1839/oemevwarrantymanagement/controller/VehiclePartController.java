package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.ScanSerialNumberRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ScanSerialNumberResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.VehiclePartService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
public class VehiclePartController {
    final VehiclePartService vehiclePartService;

    @PostMapping("/repair-detail/vehicle-part/{id}")
    public ResponseEntity<ApiResponse<ScanSerialNumberResponse>> createNewSerialNumber(
            @RequestBody ScanSerialNumberRequest request, @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") Long repairDetailId) {

        ScanSerialNumberResponse scan = vehiclePartService.handleCreateNewSerialNumber(request, repairDetailId);

        var result = ApiResponse.<ScanSerialNumberResponse>builder()
                .status(HttpStatus.OK.toString())
                .message("Update new serial number successfully")
                .data(scan)
                .build();
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/repair-detail/vehicle-part/{id}")
    public ResponseEntity<ApiResponse<ScanSerialNumberResponse>> updateNewSerialNumber(
            @RequestBody ScanSerialNumberRequest request, @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") Long repairDetailId) {

        ScanSerialNumberResponse scan = vehiclePartService.handleCreateNewSerialNumber(request, repairDetailId);

        var result = ApiResponse.<ScanSerialNumberResponse>builder()
                .status(HttpStatus.OK.toString())
                .message("Update new serial number successfully")
                .data(scan)
                .build();
        return ResponseEntity.ok(result);
    }
}
