package com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response;

import java.time.LocalDate;

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
public class RegisteredVehicleResponse {
    private String vin;
    private String licensePlate;
    private LocalDate purchaseDate;
    private String modelName;
    private String customerName;
    private String customerPhone;
    private Long scID;
}
