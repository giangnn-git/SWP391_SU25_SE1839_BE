package com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartCategoryGroupResponse {
    private Map<String, String> partMap;
}

