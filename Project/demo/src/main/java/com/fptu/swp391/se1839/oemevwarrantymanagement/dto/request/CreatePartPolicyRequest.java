package com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePartPolicyRequest {
    Long partId;
    Long warrantyPolicyId;
    String startDate;
    String endDate;
}
