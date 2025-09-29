package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import java.text.ParseException;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.domain.User;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.IntrospectRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.LoginRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.IntrospectResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.LoginResponse;
import com.nimbusds.jose.JOSEException;

@Service
public interface UserService {
    LoginResponse authenticate(LoginRequest request);

    IntrospectResponse inprospect(IntrospectRequest request) throws JOSEException, ParseException;

    User handdleFindByEmailOrPhone(String iuput);
}
