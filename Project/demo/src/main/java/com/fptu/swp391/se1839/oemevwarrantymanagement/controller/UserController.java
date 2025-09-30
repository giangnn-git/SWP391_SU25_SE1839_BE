package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import java.text.ParseException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.domain.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.IntrospectRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.LoginRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.IntrospectResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.LoginResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.UserService;
import com.nimbusds.jose.JOSEException;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
public class UserController {

    private final UserService employeeService;


    @PostMapping("/token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse user = this.employeeService.authenticate(request);
        var result = new ApiResponse<>(HttpStatus.OK, "Login successfully", user, null);
        return ResponseEntity.ok(result);

    }

    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<IntrospectResponse>> checkToken(@RequestBody IntrospectRequest request)
            throws JOSEException, ParseException {
        IntrospectResponse valid = this.employeeService.inprospect(request);

        var result = new ApiResponse<>(HttpStatus.OK, "Token introspected", valid, null);
        return ResponseEntity.ok(result);
    }

}

