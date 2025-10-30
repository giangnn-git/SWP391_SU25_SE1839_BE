package com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerSummaryResponse {
    private Long id;
    private String name;
    private String phoneNumber;
    private String email;
    private String address;
    private int vehicleCount;
}
