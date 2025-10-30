package com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VehicleInfoResponse {
    private String vin;
    private String modelName;
    private String licensePlate;
    private LocalDate purchaseDate;
    private List<String> campaignNames;
}
