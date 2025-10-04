package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import java.text.ParseException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.domain.User;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.IntrospectRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.LoginRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.UserCreateRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.UserDeleteRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.UserUpdateRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.IntrospectResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.LoginResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.UserResponse;
import com.nimbusds.jose.JOSEException;

@Service
public interface UserService {
    LoginResponse authenticate(LoginRequest request);

    IntrospectResponse inprospect(IntrospectRequest request) throws JOSEException, ParseException;
    User handlleFindByEmailOrPhone(String iuput);
    UserResponse createUser(UserCreateRequest request);   
    UserResponse updateUser(UserUpdateRequest request);
    List<UserResponse> getAllUsers();
    UserResponse deleteUser(UserDeleteRequest request);
}
