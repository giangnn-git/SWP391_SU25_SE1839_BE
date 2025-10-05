package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import java.text.ParseException;

import org.springframework.stereotype.Service;

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
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.User;
import com.nimbusds.jose.JOSEException;

@Service
public interface UserService {
    LoginResponse authenticate(LoginRequest request);

    OTPResponse handleVerifyOTP(OtpRequest request);

    IntrospectResponse inprospect(IntrospectRequest request) throws JOSEException, ParseException;

    User handdleFindByEmailOrPhone(String iuput);

    void handleLogout(LogoutRequest request) throws JOSEException, ParseException;

    UserResponse createUser(UserCreateRequest request);

    OTPResponse handleRefeshToken(RefeshTokenRequest request) throws JOSEException, ParseException;

    String generaToken(User user);
}
