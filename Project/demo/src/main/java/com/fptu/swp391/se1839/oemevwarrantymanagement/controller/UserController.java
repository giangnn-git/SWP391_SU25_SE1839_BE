package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import java.text.ParseException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.domain.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.IntrospectRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.LoginRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.UserCreateRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.UserUpdateRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.IntrospectResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.LoginResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.UserResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.UserService;
import com.nimbusds.jose.JOSEException;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://orthopterous-unwieldable-kristal.ngrok-free.dev")
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

    @PostMapping("/users/create")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        
        UserResponse createdUser = employeeService.createUser(request);
        var result = new ApiResponse<>(HttpStatus.CREATED, "User created successfully", createdUser, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/users/update")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
        @Valid @RequestBody UserUpdateRequest request) {
        
        UserResponse updateUser = employeeService.updateUser(request);
        var result = new ApiResponse<>(HttpStatus.CREATED, "User updated successfully", updateUser, null);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(){
        List<UserResponse> users = employeeService.getAllUsers();
        var result = new ApiResponse<>(HttpStatus.OK, "Get all users successfully", users, null);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
    
}
