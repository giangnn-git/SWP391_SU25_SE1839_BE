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
public class FilterOrderResponse {
    private int prodcutYear;
    private String modelName;
    private String vin;
    private String techinal;
    private double percentInProcess;
    private boolean status;
}
