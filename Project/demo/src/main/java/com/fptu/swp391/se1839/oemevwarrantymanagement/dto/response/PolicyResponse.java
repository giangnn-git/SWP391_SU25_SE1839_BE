package com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response;

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
public class PolicyResponse {
    private Long id;
    private String name;
    private int durationPeriod;
    private int mileageLimit;
    private String description;
    private String code;
    private String type;
}
