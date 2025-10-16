package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.PartListRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.GetAllPartResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.PartCategoryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.PartListResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.PartService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
public class PartController {
    private final PartService partService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<PartCategoryResponse>> category() {
        PartCategoryResponse categoryList = this.partService.handleListCategory();
        var result = ApiResponse.<PartCategoryResponse>builder()
                .status(HttpStatus.OK.toString())
                .message("Get list category successfully")
                .data(categoryList)
                .build();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/parts/{category}")
    public ResponseEntity<ApiResponse<PartListResponse>> partFlCategory(
            @PathVariable("category") PartListRequest request) {
        PartListResponse partListResponse = this.partService.handlePartList(request);
        var result = ApiResponse.<PartListResponse>builder()
                .status(HttpStatus.OK.toString())
                .message("Get part list successfully")
                .data(partListResponse)
                .build();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/parts")
    public ResponseEntity<ApiResponse<GetAllPartResponse>> getAllPart() {
        GetAllPartResponse partListResponse = this.partService.handleGetPartList();
        var result = ApiResponse.<GetAllPartResponse>builder()
                .status(HttpStatus.OK.toString())
                .message("Get part list successfully")
                .data(partListResponse)
                .build();
        return ResponseEntity.ok(result);
    }
}
