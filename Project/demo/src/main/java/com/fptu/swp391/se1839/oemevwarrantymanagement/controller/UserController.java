package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import java.text.ParseException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.ChangePasswordRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.ForgotPasswordRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.IntrospectRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.LoginRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.LogoutRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.OtpRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.RefeshTokenRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.UserCreateRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.UserSearchRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.UserUpdateRequest;
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

        @PostMapping("/token")
        public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
                LoginResponse user = this.employeeService.authenticate(request);
                var result = ApiResponse.<LoginResponse>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Input user and password correct")
                                .data(user)
                                .build();
                return ResponseEntity.ok(result);

        }

        // @PostMapping("/login")
        // public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody
        // LoginRequest request) {
        // LoginResponse user = this.employeeService.authenticate(request);
        // var result = ApiResponse.<LoginResponse>builder()
        // .status(HttpStatus.OK.toString())
        // .message("Input user and password correct")
        // .data(user)
        // .build();
        // return ResponseEntity.ok(result);

        // }

        // @PostMapping("/verify-otp")
        // public ResponseEntity<ApiResponse<OTPResponse>> verifyOtp(@RequestBody
        // OtpRequest request) {
        // OTPResponse verify = this.employeeService.handleVerifyOTP(request);
        // var result = ApiResponse.<OTPResponse>builder()
        // .status(HttpStatus.OK.toString())
        // .message("Invalid OTP")
        // .data(verify)
        // .build();
        // return ResponseEntity.ok(result);
        // }

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

        @PostMapping("/user")
        @PreAuthorize("hasAuthority('admin')")
        public ResponseEntity<ApiResponse<UserResponse>> createUser(
                        @Valid @RequestBody UserCreateRequest request) {

                UserResponse createdUser = employeeService.createUser(request);
                var result = ApiResponse.<UserResponse>builder()
                                .status(HttpStatus.CREATED.toString())
                                .message("User created successfully")
                                .data(createdUser)
                                .build();
                return ResponseEntity.ok(result);
        }

        @PutMapping("/users/{userID}")
        @PreAuthorize("hasAuthority('admin')")
        public ResponseEntity<ApiResponse<UserResponse>> updateUser(
                        @PathVariable Long userID,
                        @Valid @RequestBody UserUpdateRequest request) {

                UserResponse updateUser = employeeService.updateUser(request, userID);
                var result = ApiResponse.<UserResponse>builder()
                                .status(HttpStatus.OK.toString())
                                .message("User updated successfully")
                                .data(updateUser)
                                .build();
                return ResponseEntity.ok(result);
        }

        @GetMapping("/users")
        @PreAuthorize("hasAuthority('admin')")
        public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
                List<UserResponse> users = employeeService.getAllUsers();
                var result = ApiResponse.<List<UserResponse>>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Get all users successfully")
                                .data(users)
                                .build();
                return ResponseEntity.ok(result);
        }

        @PatchMapping("/users/inactive/{userID}")
        @PreAuthorize("hasAuthority('admin')")
        public ResponseEntity<ApiResponse<UserResponse>> deleteUsers(
                        @PathVariable Long userID) {
                UserResponse user = employeeService.deleteUser(userID);
                var result = ApiResponse.<UserResponse>builder()
                                .status(HttpStatus.OK.toString())
                                .message("User deleted successfully")
                                .data(user)
                                .build();
                return ResponseEntity.ok(result);
        }

        @GetMapping("/users/{userID}")
        @PreAuthorize("hasAuthority('admin')")
        public ResponseEntity<ApiResponse<UserResponse>> getUserByID(@PathVariable Long userID) {
                UserResponse user = employeeService.getUserById(userID);
                var result = ApiResponse.<UserResponse>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Get user " + userID + " successfully")
                                .data(user)
                                .build();
                return ResponseEntity.ok(result);
        }

        @GetMapping("/users/search")
        @PreAuthorize("hasAuthority('admin')")
        public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(
                        @Valid @RequestBody UserSearchRequest request) {
                List<UserResponse> userList = employeeService.searchUsers(request);
                var result = ApiResponse.<List<UserResponse>>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Users found")
                                .data(userList)
                                .build();
                return ResponseEntity.ok(result);
        }

        @PatchMapping("/users/active/{userID}")
        @PreAuthorize("hasAuthority('admin')")
        public ResponseEntity<ApiResponse<UserResponse>> restoreUser(@PathVariable Long userID) {
                UserResponse user = employeeService.restoreUser(userID);
                var result = ApiResponse.<UserResponse>builder()
                                .status(HttpStatus.OK.toString())
                                .message("restore " + userID + " successfully")
                                .data(user)
                                .build();
                return ResponseEntity.ok(result);
        }

        @PostMapping("/{userID}/change-password")
        @PreAuthorize("hasAnyAuthority('admin','technician','manager','staff')")
        public ResponseEntity<ApiResponse<UserResponse>> changePassword(
                        @AuthenticationPrincipal Jwt jwt,
                        @PathVariable Long userID,
                        @Valid @RequestBody ChangePasswordRequest request) {
                String email = jwt.getSubject();
                UserResponse updated = employeeService.changePassword(userID, request);
                var result = ApiResponse.<UserResponse>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Password changed successfully")
                                .data(updated)
                                .build();
                return ResponseEntity.ok(result);
        }

        @PostMapping("/forgot-password")
        public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
                String message = employeeService.forgotPassword(request);
                var result = ApiResponse.<String>builder()
                                .status(HttpStatus.OK.toString())
                                .message("successfull")
                                .data(message)
                                .build();
                return ResponseEntity.ok(result);
        }
}
