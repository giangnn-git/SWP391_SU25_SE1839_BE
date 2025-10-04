package com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request;

import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class UserUpdateRequest {
    @Id
    private Long id;
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String name;

    private String password;
    @Pattern(regexp = "\\d{10}", message = "Phone number must contain exactly 10 digits")
    private String phoneNumber;
    private String role;
    private Long serviceCenterId;
    private Status status;
    public enum Status {
        ACTIVE, INACTIVE, SUSPENDED
    }
}
