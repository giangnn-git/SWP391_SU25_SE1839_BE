package com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.User;

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
public class UserResponse {
    Long id;
    String email;
    String name;
    String phoneNumber;
    User.Role role;
    User.Status status;
    Long serviceCenterId;
}
