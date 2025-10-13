package com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartInventoryResponse {
    private Long id;
    private Long partId;
    private String partName;
    private String partCategory;
    private Long serviceCenterId;
    private String serviceCenterName;
    private String serviceCenterAddress;
    private int quantity;
    private String unit;
}
