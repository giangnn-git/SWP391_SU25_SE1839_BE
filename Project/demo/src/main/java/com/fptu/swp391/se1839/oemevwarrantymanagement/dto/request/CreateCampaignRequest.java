package com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCampaignRequest {

    @NotBlank(message = "Campaign name cannot be blank")
    private String name;

    private String description;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Produce date from is required")
    private LocalDate produceDateFrom;

    @NotNull(message = "Produce date to is required")
    private LocalDate produceDateTo;

    @NotBlank(message = "Code cannot be blank")
    @Size(min = 3, max = 10, message = "Code must be between 3 and 10 characters")
    private String code;
}
