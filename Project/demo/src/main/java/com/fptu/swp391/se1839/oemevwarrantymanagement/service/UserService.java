package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import java.text.ParseException;
import java.util.List;

import org.springframework.stereotype.Service;

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
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.User;
import com.nimbusds.jose.JOSEException;

@Service
public interface UserService {
    LoginResponse authenticate(LoginRequest request);

    OTPResponse handleVerifyOTP(OtpRequest request);

    IntrospectResponse inprospect(IntrospectRequest request) throws JOSEException, ParseException;

    void handleLogout(LogoutRequest request) throws JOSEException, ParseException;

    OTPResponse handleRefeshToken(RefeshTokenRequest request) throws JOSEException, ParseException;

    String generaToken(User user);

    User handleFindByEmailOrPhone(String iuput);

    UserResponse createUser(UserCreateRequest request);

    UserResponse updateUser(UserUpdateRequest request, Long id);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse deleteUser(Long id, Long ownId);

    List<UserResponse> searchUsers(UserSearchRequest request);

    UserResponse restoreUser(Long id);

    UserResponse changePassword(Long id, ChangePasswordRequest request);

    String forgotPassword(ForgotPasswordRequest request);
}
