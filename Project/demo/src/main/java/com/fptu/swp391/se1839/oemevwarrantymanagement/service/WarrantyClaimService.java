package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CreateClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.FilterRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.WarrantyClaimStatusRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ClaimDashboardResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ClaimDetailResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.CreateClaimResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DashboardClaimSummaryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.WarrantyClaimStatusResponse;

public interface WarrantyClaimService {

    DashboardClaimSummaryResponse handleSummaryClaims(Long serviceCenterId);

    // WarrantyClaim
    CreateClaimResponse handleCreateClaim(CreateClaimRequest request, long serviceCenterId, MultipartFile[] attachments,
            long userId)
            throws IOException;

    ClaimDashboardResponse handleClaimDashboard(long serviceCenterId, FilterRequest request, long userId);

    WarrantyClaimStatusResponse handleChangeStatus(long claimId, WarrantyClaimStatusRequest request, long userId);

    ClaimDetailResponse handleGetClaimDetail(long claimId, Long userId);

}
