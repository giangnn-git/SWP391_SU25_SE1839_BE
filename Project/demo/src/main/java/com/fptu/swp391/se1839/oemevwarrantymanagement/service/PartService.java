package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.PartListRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.PartCategoryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.PartListResponse;

@Service
public interface PartService {
    PartCategoryResponse handleListCategory();

    PartListResponse handlePartList(PartListRequest request);
}
