package com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String name;

    @NotBlank
    private String password;
    private String phoneNumber;
    private String role; // "user", "admin"
    private Long serviceCenterId;
}
