package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;


import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.domain.ServiceCenter;
import com.fptu.swp391.se1839.oemevwarrantymanagement.domain.User;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.IntrospectRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.LoginRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.UserCreateRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.UserDeleteRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.UserUpdateRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.IntrospectResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.LoginResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.UserResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.ServiceCenterRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.UserRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.UserService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ServiceCenterRepository serviceCenterRepository;

    private final PasswordEncoder passwordEncoder;
    @Override
    public User handlleFindByEmailOrPhone(String input) {
        return this.userRepository.findByEmailOrPhoneNumber(input, input)
                .orElseThrow(() -> new NoSuchElementException("Email or Phone isn't correct"));
    }

    @Value("${SIGN_KEY}")
    private String SIGN_KEY;

    // Xác thực người dùng và trả JWT
    public LoginResponse authenticate(LoginRequest request) {
        User user = userRepository.findByEmailOrPhoneNumber(request.getUser(), request.getUser())
                .orElseThrow(() -> new NoSuchElementException("Email or Phone isn't correct"));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) {
            throw new IllegalArgumentException("Password isn't correct");
        }

        String token = generaToken(request.getUser());
        return LoginResponse.builder()
                .token(token)
                .status(true)
                .build();
    }

    public IntrospectResponse inprospect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        JWSVerifier jwsVerifier = new MACVerifier(SIGN_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expityDate = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verrfied = signedJWT.verify(jwsVerifier);

        return IntrospectResponse.builder()
                .status(verrfied && expityDate.after(new Date()))
                .build();
    }

    // Create new user.

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is existed");
        }
        user.setPhoneNumber(request.getPhoneNumber());
        if (userRepository.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
            throw new IllegalArgumentException("Phone number is existed");
        }        
        user.setName(request.getName());       
        user.setRole(User.Role.valueOf(request.getRole().toUpperCase()));
        user.setStatus(User.Status.ACTIVE);
        ServiceCenter sc = serviceCenterRepository.findById(request.getServiceCenterId())
            .orElseThrow(() -> new RuntimeException("ServiceCenter not found"));
    user.setServiceCenter(sc);

        // encoder
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User saved = userRepository.save(user);

        return new UserResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getName(),
                saved.getPhoneNumber(),
                saved.getRole(),
                saved.getStatus(),
                saved.getServiceCenter().getId()

        );
    }

    private String generaToken(String user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user)
                .issuer("devteria.com")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        try {
            jwsObject.sign(new MACSigner(SIGN_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token: ", e);
            throw new RuntimeException(e);
        }
    }
    @Override
    public UserResponse deleteUser(UserDeleteRequest request) {
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        user.setStatus(User.Status.INACTIVE);
        User saved = userRepository.save(user);
        return new UserResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getName(),
                saved.getPhoneNumber(),
                saved.getRole(),
                saved.getStatus(),
                saved.getServiceCenter().getId()
        );
    }


    @Override
    public UserResponse updateUser(UserUpdateRequest request) {
        User user = userRepository.findById(request.getId())
        .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (userRepository.findByEmail(request.getEmail()).isPresent() && !user.getEmail().equals(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use by another user");
        }
        user.setEmail(request.getEmail());
        user.setName(request.getName());       
        
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            user.setRole(User.Role.valueOf(request.getRole().toUpperCase()));
        }
        if (request.getStatus() != null) {
            user.setStatus(User.Status.valueOf(request.getStatus().name().toUpperCase()));
        }
        ServiceCenter sc = serviceCenterRepository.findById(request.getServiceCenterId())
            .orElseThrow(() -> new RuntimeException("ServiceCenter not found"));
        user.setServiceCenter(sc);

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Update phone number if provided
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        User saved = userRepository.save(user);

        return new UserResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getName(),
                saved.getPhoneNumber(),
                saved.getRole(),
                saved.getStatus(),
                saved.getServiceCenter().getId()
        );
    }    
    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> userRes = new ArrayList<>();
        for (User user : users) {
            userRes.add(new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        user.getPhoneNumber(),
                        user.getRole(),
                        user.getStatus(),
                        user.getServiceCenter().getId())
                        );
        }
        return userRes;
    }
}
