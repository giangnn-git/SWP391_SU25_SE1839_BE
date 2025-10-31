package com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelPolicyResponse {
    private String modelName;
    private String policyName;
    private int durationMonths;
    private int mileageLimit;
    private LocalDate purchaseDate;
    private LocalDate warrantyEndDate;
}
