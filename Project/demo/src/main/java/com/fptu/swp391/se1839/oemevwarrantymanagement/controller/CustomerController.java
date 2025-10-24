package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CustomerRegisterRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.CustomerRegisterResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.CustomerService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
public class CustomerController {

    final CustomerService customerService;

    @PostMapping("/customers")
    public ResponseEntity<ApiResponse<CustomerRegisterResponse>> registerCustomer(
            @Valid @RequestBody CustomerRegisterRequest req) {
        CustomerRegisterResponse customer = customerService.registerCustomer(req);
        var result = ApiResponse.<CustomerRegisterResponse>builder()
                .status(HttpStatus.CREATED.toString())
                .message("Create customer successfully")
                .data(customer)
                .build();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/customer")
    public ResponseEntity<ApiResponse<CustomerRegisterResponse>> findRegisteredVehicleByPhone(
            @RequestParam String vin) {

        CustomerRegisterResponse customerResponse = customerService.handleFindCustomerByVin(vin);

        var result = ApiResponse.<CustomerRegisterResponse>builder()
                .status(HttpStatus.OK.toString())
                .message("Get vehicle successfully")
                .data(customerResponse)
                .build();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<ApiResponse<CustomerRegisterResponse>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRegisterRequest req) {

        var updated = customerService.updateCustomer(id, req);
        var result = ApiResponse.<CustomerRegisterResponse>builder()
                .status(HttpStatus.OK.toString())
                .message("Update customer successfully")
                .data(updated)
                .build();
        return ResponseEntity.ok(result);
    }
}
