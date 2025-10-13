package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.GetAllVehicleResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.VehicleService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping("/vehicles")
    public ResponseEntity<ApiResponse<GetAllVehicleResponse>> getAllVehicles() {
        GetAllVehicleResponse vehicles = vehicleService.getAllVehicles();
        var result = ApiResponse.<GetAllVehicleResponse>builder()
                .status(HttpStatus.OK.toString())
                .message("Get all vehicles successfully")
                .data(vehicles)
                .build();
        return ResponseEntity.ok(result);
    }

}
