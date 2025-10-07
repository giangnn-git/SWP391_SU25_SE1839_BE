package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.FilterRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.OrderDashboardResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.RepairOrderService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
public class RepairOrderController {
    private final RepairOrderService repairOrderService;

    @GetMapping("/repairOrders")
    public ResponseEntity<ApiResponse<OrderDashboardResponse>> getRepairOrder(@AuthenticationPrincipal Jwt jwt,
            @RequestBody FilterRequest request) {
        Long serviceCenterId = Long.parseLong(jwt.getClaim("serviceCenterId").toString());
        OrderDashboardResponse odr = this.repairOrderService.handleOrderDashboard(serviceCenterId, request);
        var result = ApiResponse.<OrderDashboardResponse>builder()
                .status(HttpStatus.OK.toString())
                .message("Get part list successfully")
                .data(odr)
                .build();
        return ResponseEntity.ok(result);
    }

    // @PutMapping("/repairOrders/{id}")
    // public ResponseEntity<ApiResponse<ChooseTechnicalRequest>> putMethodName(
    // @PathVariable Long techId, @RequestBody ChooseTechnicalRequest requets,
    // @AuthenticationPrincipal Jwt jwt) {
    // ChooseTechnicalRequest ctr = this.repairOrderService;
    // var result = ApiResponse.<ChooseTechnicalRequest>builder()
    // .status(HttpStatus.OK.toString())
    // .message("Get part list successfully")
    // .data(ctr)
    // .build();
    // return ResponseEntity.ok(result);
    // }
}
