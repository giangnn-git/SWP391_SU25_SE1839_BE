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
public class RepairDetailResponse {
    long id;
    String partName;
    String serailNumber;
    long quantity;
    int prodcutYear;
    String modelName;
    String vin;
    String category;
}