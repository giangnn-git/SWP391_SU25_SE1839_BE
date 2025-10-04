package com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response;

import com.fptu.swp391.se1839.oemevwarrantymanagement.domain.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private User.Role role;
    private User.Status status;
    private Long serviceCenterId;
}


