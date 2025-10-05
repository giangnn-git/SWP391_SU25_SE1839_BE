package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CreateClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.CreateClaimResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DashboardClaimSummaryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.SummaryClaimResponse;

@Service
public interface WarrantyClaimService {

    DashboardClaimSummaryResponse handleSummaryClaims(Long serviceCenterId);

    // WarrantyClaim
    CreateClaimResponse handleCreateClaim(CreateClaimRequest request, long serviceCenterId, MultipartFile[] attachments)
            throws IOException;

    SummaryClaimResponse handleSummaryClaim(long serviceCenterId);
}
