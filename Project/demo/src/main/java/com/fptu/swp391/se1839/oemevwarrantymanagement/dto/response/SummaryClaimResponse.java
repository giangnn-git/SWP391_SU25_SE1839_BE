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
public class SummaryClaimResponse {
    private SummaryItemResponse total;
    private SummaryItemResponse pending;
    private SummaryItemResponse approved;
    private SummaryItemResponse cost;
    private boolean status;
}
