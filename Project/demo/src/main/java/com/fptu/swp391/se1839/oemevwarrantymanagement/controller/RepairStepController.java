package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CreateRepairStepRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ChangeStatusRepairStepResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.CreateRepairStepResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.RepairStepService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
public class RepairStepController {

        final RepairStepService repairStepService;

        @PostMapping("/repairSteps/{id}")
        public ResponseEntity<ApiResponse<CreateRepairStepResponse>> createStep(
                        @PathVariable Long repairOrderId, @RequestBody CreateRepairStepRequest requets) {
                CreateRepairStepResponse crsr = this.repairStepService.handleCreateRepairStep(requets, repairOrderId);
                var result = ApiResponse.<CreateRepairStepResponse>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Choose techinician successfully")
                                .data(crsr)
                                .build();
                return ResponseEntity.ok(result);
        }

        @PatchMapping("/repairSteps/{id}")
        public ResponseEntity<ApiResponse<ChangeStatusRepairStepResponse>> changeStatus(
                        @PathVariable("id") Long repairStepId, @RequestBody String status) {
                ChangeStatusRepairStepResponse changeStatusRepairStep = this.repairStepService
                                .changeStepStatus(repairStepId, status);
                var result = ApiResponse.<ChangeStatusRepairStepResponse>builder()
                                .status(HttpStatus.OK.toString())
                                .message("Get Repair Order successfully")
                                .data(changeStatusRepairStep)
                                .build();
                return ResponseEntity.ok(result);
        }
}
