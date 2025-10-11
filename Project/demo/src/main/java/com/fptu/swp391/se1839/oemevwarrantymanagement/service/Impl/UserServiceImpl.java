package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.Utilities.PasswordGeneration;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.ChangePasswordRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.EmailDetailsRequest;
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
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.InvalidToken;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ServiceCenter;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.User;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.InvalidTokenRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.ServiceCenterRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.UserRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.EmailService;
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
    private final PasswordEncoder passwordEncoder;
    private final InvalidTokenRepository invalidTokenRepository;
    private final ServiceCenterRepository serviceCenterRepository;
    private final OtpServiceImpl otpService;
    private final PasswordGeneration passwordGeneration;
    private final EmailService emailService;
    @Value("${app.frontend.login-url}")
    private String loginUrl;

    @Override
    public User handleFindByEmailOrPhone(String input) {
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
        // otpService.sendOtp(user);
        // return LoginResponse.builder()
        // .message("OTP sent to your email/phone. Please verify.")
        // .build();
        String token = generaToken(user);
        return LoginResponse.builder()
                .token(token)
                .status(true)
                .id(user.getId())
                .name(user.getName())
                .requiresPasswordChange(user.isRequiresPasswordChange())
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
                .claim("id", user.getId())
                .claim("role", user.getRole().toString())
                .claim("serviceCenterId", user.getServiceCenter().getId())
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
        ServiceCenter sc = serviceCenterRepository.findById(request.getServiceCenterId())
                .orElseThrow(() -> new NoSuchElementException("ServiceCenter not found"));
        user.setServiceCenter(sc);
        String temporaryPassword = passwordGeneration.generateSimplePassword();
        user.setRequiresPasswordChange(true);
        // encoder
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setRequiresPasswordChange(true);
        User saved = userRepository.save(user);
        sendAccountCreationEmail(
                saved.getName(),
                saved.getEmail(),
                temporaryPassword,
                saved.getRole(),
                loginUrl);

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
    public UserResponse deleteUser(Long id, Long ownId) {
        if (id.equals(ownId)) {
            throw new IllegalArgumentException("You cannot deactivate your own account.");
        }
        User user = userRepository.findById(id)
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
                saved.getServiceCenter().getId());
    }

    @Override
    public UserResponse restoreUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (user.getStatus().equals(User.Status.INACTIVE)) {
            user.setStatus(User.Status.ACTIVE);
        } else {
            throw new IllegalArgumentException("User is already active");
        }
        User saved = userRepository.save(user);
        return new UserResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getName(),
                saved.getPhoneNumber(),
                saved.getRole(),
                saved.getStatus(),
                saved.getServiceCenter().getId());
    }

    @Override
    public UserResponse updateUser(UserUpdateRequest request, Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        user.setId(id);

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
                .orElseThrow(() -> new NoSuchElementException("ServiceCenter not found"));
        user.setServiceCenter(sc);

        // Update phone number if provided
        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()
                && !user.getPhoneNumber().equals(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number is already in use by another user");
        }
        user.setPhoneNumber(request.getPhoneNumber());
        User saved = userRepository.save(user);
        return new UserResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getName(),
                saved.getPhoneNumber(),
                saved.getRole(),
                saved.getStatus(),
                saved.getServiceCenter().getId());
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
                    user.getServiceCenter().getId()));
        }
        return userRes;
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + id));

        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setName(user.getName());
        userResponse.setPhoneNumber(user.getPhoneNumber());
        userResponse.setRole(user.getRole());
        userResponse.setStatus(user.getStatus());
        userResponse.setServiceCenterId(user.getServiceCenter().getId());
        return userResponse;
    }

    @Override
    public List<UserResponse> searchUsers(UserSearchRequest request) {
        List<User> users = userRepository
                .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase(
                        request.getKeyword(), request.getKeyword(), request.getKeyword());

        List<UserResponse> userRes = new ArrayList<>();
        for (User user : users) {
            userRes.add(new UserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getPhoneNumber(),
                    user.getRole(),
                    user.getStatus(),
                    user.getServiceCenter().getId()));
        }
        return userRes;
    }

    private void sendAccountCreationEmail(
            String name,
            String recipientEmail,
            String password,
            User.Role role,
            String loginUrl) {

        String subject = "Account Access Notification";

        String htmlBody = String.format(
                "<html>" +
                        "<body style='font-family: Arial, sans-serif; line-height: 1.6;'>" +
                        "<p>Dear %s,</p>" +
                        "<p>We are pleased to inform you that your account has been successfully created. Please find your login details below:</p>"
                        +
                        "<hr/>" +
                        "<p><strong>User (Email):</strong> %s</p>" +
                        "<p><strong>Password:</strong> %s</p>" +
                        "<p><strong>Role:</strong> %s</p>" +
                        "<hr/>" +
                        "<p>Please log in at: <a href='%s'>%s</a></p>" +
                        "<p>For your security, please <strong>change your password immediately</strong> after your first login.</p>"
                        +
                        "<p>Best regards,<br>" +
                        "OEM EV Warranty</p>" +
                        "</body>" +
                        "</html>",
                name,
                recipientEmail,
                password,
                role.toString(),
                loginUrl, loginUrl);

        EmailDetailsRequest details = new EmailDetailsRequest();
        details.setRecipient(recipientEmail);
        details.setSubject(subject);
        details.setMessageBody(htmlBody);

        emailService.sendHtmlMail(details);
    }

    @Override
    public UserResponse changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setRequiresPasswordChange(false);
        userRepository.save(user);

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getStatus(),
                user.getServiceCenter().getId()
        );
    }

    @Override
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NoSuchElementException("Email not found"));
        if (!user.getStatus().equals(User.Status.ACTIVE)) {
            throw new IllegalArgumentException("User is not active");
        }
        if (user.isRequiresPasswordChange()) {
            throw new IllegalArgumentException("Password has not been changed");
        }
        String newPassword = passwordGeneration.generateSimplePassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setRequiresPasswordChange(true);
        userRepository.save(user);
        
        EmailDetailsRequest details = new EmailDetailsRequest();
        details.setRecipient(user.getEmail());
        details.setSubject("Password Reset - OEM EV Warranty");
        details.setMessageBody(
                "<p>Dear " + user.getName() + ",</p>" +
                        "<p>Your password has been reset. Here is your new password:</p>" +
                        "<p><b>" + newPassword + "</b></p>" +
                        "<p>Please log in and change your password immediately.</p>" +
                        "<p>Best regards,<br>OEM EV Warranty</p>");

        emailService.sendHtmlMail(details);
        return "Password reset email sent successfully.";
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
