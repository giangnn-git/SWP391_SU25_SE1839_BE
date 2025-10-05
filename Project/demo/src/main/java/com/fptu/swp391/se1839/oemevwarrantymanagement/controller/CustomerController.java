package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CustomerRegisterRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Customer;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.CustomerService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/customers")
    public ResponseEntity<ApiResponse<?>> registerCustomer(@RequestBody CustomerRegisterRequest req) {
        Customer customer = customerService.registerCustomer(req);
        var result = ApiResponse.builder()
                .status(HttpStatus.OK.toString())
                .message("Create customer successfully")
                .data(customer)
                .build();
        return ResponseEntity.ok(result);
    }
}
