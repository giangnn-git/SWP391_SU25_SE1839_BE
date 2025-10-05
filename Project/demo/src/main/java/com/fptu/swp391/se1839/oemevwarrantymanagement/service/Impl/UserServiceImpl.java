package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.InvalidToken;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ServiceCenter;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.User;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.UserOtp;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.InvalidTokenRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.ServiceCenterRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.UserRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InvalidTokenRepository invalidTokenRepository;
    private final ServiceCenterRepository serviceCenterRepository;
    private final OtpServiceImpl otpService;

    public User handdleFindByEmailOrPhone(String input) {
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
        otpService.sendOtp(user);
        return LoginResponse.builder()
                .message("OTP sent to your email/phone. Please verify.")
                .build();
    }

    public OTPResponse handleVerifyOTP(OtpRequest request) {
        boolean valid = otpService.verifyOtp(request.getEmailOrPhoneNumber(), request.getVerify());
        if (valid) {
            User user = userRepository.findByEmail(request.getEmailOrPhoneNumber())
                    .orElseGet(() -> userRepository.findByPhoneNumber(request.getEmailOrPhoneNumber())
                            .get());

            String token = generaToken(user);
            return OTPResponse.builder()
                    .token(token)
                    .message("Verify OTP successfully")
                    .status(true)
                    .build();
        }
        return null;
    }

    public IntrospectResponse inprospect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();

        boolean verify = true;
        try {
            verifyToken(token);
        } catch (JOSEException | ParseException e) {
            verify = false;

        }
        return IntrospectResponse.builder()
                .status(verify)
                .build();
    }

    public OTPResponse handleRefeshToken(RefeshTokenRequest request) throws JOSEException, ParseException {
        var verifyToken = verifyToken(request.getToken());

        String jwt = verifyToken.getJWTClaimsSet().getJWTID();
        Date expiDate = verifyToken.getJWTClaimsSet().getExpirationTime();

        InvalidToken invalidToken = InvalidToken.builder()
                .id(jwt)
                .expityDate(expiDate)
                .build();
        invalidTokenRepository.save(invalidToken);

        User user = userRepository.findByEmailOrPhoneNumber(verifyToken.getJWTClaimsSet().getSubject(),
                verifyToken.getJWTClaimsSet().getSubject())
                .orElseThrow(() -> new NoSuchElementException("User is invalid"));

        String token = generaToken(user);
        return OTPResponse.builder()
                .token(token)
                .status(true)
                .build();
    }

    public String generaToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail() != null ? user.getEmail() : user.getPhoneNumber())
                .issuer("devteria.com")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(8, ChronoUnit.HOURS)))
                .jwtID(UUID.randomUUID().toString())
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

    public void handleLogout(LogoutRequest request) throws JOSEException, ParseException {
        var vetifyToken = verifyToken(request.getToken());
        String jwt = vetifyToken.getJWTClaimsSet().getJWTID();
        Date expiDate = vetifyToken.getJWTClaimsSet().getExpirationTime();

        InvalidToken invalidToken = InvalidToken.builder()
                .id(jwt)
                .expityDate(expiDate)
                .build();
        invalidTokenRepository.save(invalidToken);
    }

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
        ServiceCenter sc = this.serviceCenterRepository.findById(request.getServiceCenterId())
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

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier jwsVerifier = new MACVerifier(SIGN_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expityDate = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verrfied = signedJWT.verify(jwsVerifier);
        if (!(verrfied && expityDate.after(new Date()))) {
            throw new RuntimeException("Verification failed or expired date passed");
        }
        if (invalidTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new RuntimeException("Verification failed or expired date passed");
        }
        return signedJWT;
    }

}
