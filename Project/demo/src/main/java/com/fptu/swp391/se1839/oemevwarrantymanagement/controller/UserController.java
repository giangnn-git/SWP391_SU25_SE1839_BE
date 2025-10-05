package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import java.text.ParseException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.IntrospectRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.LoginRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.LogoutRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.OtpRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.RefeshTokenRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.UserCreateRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.IntrospectResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.LoginResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.OTPResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.UserResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ApiResponse;
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

        @PostMapping("/login")
        public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
                LoginResponse user = this.employeeService.authenticate(request);
                var result = ApiResponse.<LoginResponse>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Input user and password correct")
                                .data(user)
                                .build();
                return ResponseEntity.ok(result);

        }

        @PostMapping("/verify-otp")
        public ResponseEntity<ApiResponse<OTPResponse>> verifyOtp(@RequestBody OtpRequest request) {
                OTPResponse verify = this.employeeService.handleVerifyOTP(request);
                var result = ApiResponse.<OTPResponse>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Invalid OTP")
                                .data(verify)
                                .build();
                return ResponseEntity.ok(result);
        }

        @PostMapping("/introspect")
        public ResponseEntity<ApiResponse<IntrospectResponse>> checkToken(@RequestBody IntrospectRequest request)
                        throws JOSEException, ParseException {
                IntrospectResponse valid = this.employeeService.inprospect(request);
                var result = ApiResponse.<IntrospectResponse>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Token introspected")
                                .data(valid)
                                .build();
                return ResponseEntity.ok(result);
        }

        @PostMapping("/logout")
        public ResponseEntity<ApiResponse<Void>> logout(@RequestBody LogoutRequest request)
                        throws JOSEException, ParseException {
                this.employeeService.handleLogout(request);
                var result = ApiResponse.<Void>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Logout successfully")
                                .data(null)
                                .build();
                return ResponseEntity.ok(result);

        }

        @PostMapping("/refesh")
        public ResponseEntity<ApiResponse<OTPResponse>> refesh(@RequestBody RefeshTokenRequest request)
                        throws JOSEException, ParseException {
                OTPResponse user = this.employeeService.handleRefeshToken(request);
                var result = ApiResponse.<OTPResponse>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Refesh token successfully")
                                .data(user)
                                .build();
                return ResponseEntity.ok(result);

        }

        @PostMapping("/users")
        @PreAuthorize("hasAuthority('admin')")
        public ResponseEntity<ApiResponse<UserResponse>> createUser(
                        @Valid @RequestBody UserCreateRequest request) {

                UserResponse createdUser = employeeService.createUser(request);
                var result = ApiResponse.<UserResponse>builder()
                                .status(HttpStatus.CREATED.toString())
                                .message("User created successfully")
                                .data(createdUser)
                                .build();
                return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }
}
