package com.fptu.swp391.se1839.oemevwarrantymanagement.entity;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;
    private String errorCode;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
