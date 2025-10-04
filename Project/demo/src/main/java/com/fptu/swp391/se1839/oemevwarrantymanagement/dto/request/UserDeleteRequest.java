package com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class UserDeleteRequest {
    @Id
    private Long id;
}
