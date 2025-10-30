package com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TogglePolicyStatusRequest {
    private Long replacementPolicyId; 
}

