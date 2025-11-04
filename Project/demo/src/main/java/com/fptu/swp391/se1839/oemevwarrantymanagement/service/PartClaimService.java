package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import java.util.List;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.AllPartClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.ChangeStatusPartClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ClaimsByComponentResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.claimsByCategoryResponse;

public interface PartClaimService {
    List<claimsByCategoryResponse> calculateClaimsByCategory(Long serviceCenterId);

    List<ClaimsByComponentResponse> calculateClaimsByComponent(Long serviceCenterId);

    String handleCreatePartClaim(AllPartClaimRequest request, long claimId);

    String handleChangeStatusPartClaim(ChangeStatusPartClaimRequest request, long claimId, long partClaimId,
            long userId);

}
