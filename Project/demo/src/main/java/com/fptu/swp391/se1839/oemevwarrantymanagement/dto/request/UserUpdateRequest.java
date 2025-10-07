package com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String name;
    @Pattern(regexp = "\\d{10}", message = "Phone number must contain exactly 10 digits")
    private String phoneNumber;
    private String role;
    private Long serviceCenterId;
    private Status status;

    public enum Status {
        ACTIVE, INACTIVE, SUSPENDED
    }
}
