package com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request;

import java.util.Set;

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
public class CreateClaimRequest {
    private String description;
    private int mileage;
    private String vin;
    private String status;
    private Set<PartClaimRequest> partClaims;
}
