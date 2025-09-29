package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.domain.ServiceCenter;
import com.fptu.swp391.se1839.oemevwarrantymanagement.domain.WarrantyClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.SummaryClaimsResponse;

@Service
public interface WarrantyClaimService {

    WarrantyClaim handleCreateClaim(ServiceCenter serviceCenter, WarrantyClaim input);

    SummaryClaimsResponse handleSummaryClaims(Long serviceCenterId);
}
