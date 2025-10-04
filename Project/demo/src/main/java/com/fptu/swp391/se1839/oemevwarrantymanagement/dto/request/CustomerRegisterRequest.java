package com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request;

import lombok.Data;

@Data
public class CustomerRegisterRequest {
    private String name;
    private String phoneNumber;
    private String email;
    private String address;
    private String vin; 
}
