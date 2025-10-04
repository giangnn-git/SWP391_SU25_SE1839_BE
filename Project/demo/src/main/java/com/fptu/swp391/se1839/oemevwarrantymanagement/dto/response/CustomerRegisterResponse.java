package com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegisterResponse {
    private Long id;
    private String name;
    private String phoneNumber;
    private String email;
    private String address;
}
