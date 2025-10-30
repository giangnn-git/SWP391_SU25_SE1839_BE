package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.ChooseTechnicalRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.FilterRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ChooseTechnicalResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.OrderDashboardResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.OrderDetailResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.RepairOrderService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
public class RepairOrderController {
    final RepairOrderService repairOrderService;

    @GetMapping("/repairOrders")
    public ResponseEntity<ApiResponse<OrderDashboardResponse>> getRepairOrder(@AuthenticationPrincipal Jwt jwt,
            @RequestBody(required = false) FilterRequest request) {
        Long serviceCenterId = Long.parseLong(jwt.getClaim("serviceCenterId").toString());
        if (request == null) {
            request = new FilterRequest();
        }
        Long userId = Long.parseLong(jwt.getClaim("userId").toString());
        OrderDashboardResponse odr = this.repairOrderService.handleOrderDashboard(serviceCenterId, request, userId);
        var result = ApiResponse.<OrderDashboardResponse>builder()
                .status(HttpStatus.OK.toString())
                .message("Get inf dashboard successfully")
                .data(odr)
                .build();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/repairOrders/{id}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getRepairOrderDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") long orderId) {
        Long serviceCenterId = Long.parseLong(jwt.getClaim("serviceCenterId").toString());
        OrderDetailResponse orderDetail = this.repairOrderService.handleGetDetailOrder(serviceCenterId, orderId);
        var result = ApiResponse.<OrderDetailResponse>builder()
                .status(HttpStatus.OK.toString())
                .message("Get inf dashboard successfully")
                .data(orderDetail)
                .build();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/repairOrders/{id}")
    public ResponseEntity<ApiResponse<ChooseTechnicalResponse>> chooseTechinician(
            @PathVariable("id") Long repairOrderId, @RequestBody ChooseTechnicalRequest requets) {
        ChooseTechnicalResponse ctr = this.repairOrderService.handleChooseTechnical(repairOrderId, requets);
        var result = ApiResponse.<ChooseTechnicalResponse>builder()
                .status(HttpStatus.OK.toString())
                .message("Choose techinician successfully")
                .data(ctr)
                .build();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/send-complete-email")
    public ResponseEntity<ApiResponse<String>> sendRepairCompletedEmail(@PathVariable Long id) {
        String success= repairOrderService.sendRepairCompletedEmail(id);
        var result = ApiResponse.<String>builder()
                .status(HttpStatus.OK.toString())
                .message("Choose techinician successfully")
                .data(success)
                .build();
        return ResponseEntity.ok(result);
    }
}
