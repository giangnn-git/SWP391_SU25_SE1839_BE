package com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePolicyRequest {
    private String name;

    private int durationPeriod;

    private int mileageLimit;

    private String description;
}
