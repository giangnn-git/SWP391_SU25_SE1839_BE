package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.domain.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CustomerRegisterRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.CustomerRegisterResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.CustomerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://orthopterous-unwieldable-kristal.ngrok-free.dev")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/customers")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<ApiResponse<CustomerRegisterResponse>> registerCustomer(
        @Valid @RequestBody CustomerRegisterRequest request) {
        CustomerRegisterResponse responseDto = customerService.registerCustomer(request);

        ApiResponse<CustomerRegisterResponse> response = new ApiResponse<>(
                HttpStatus.CREATED,
                "Customer registered successfully",
                responseDto,
                null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
