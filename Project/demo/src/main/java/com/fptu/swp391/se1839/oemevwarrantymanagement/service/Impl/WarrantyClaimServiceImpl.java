package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fptu.swp391.se1839.oemevwarrantymanagement.domain.ServiceCenter;
import com.fptu.swp391.se1839.oemevwarrantymanagement.domain.WarrantyClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.SummaryClaimsResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.WarrancyClaimRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.WarrantyClaimService;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarrantyClaimServiceImpl implements WarrantyClaimService {
    private final WarrancyClaimRepository warrancyClaimRepository;

    public SummaryClaimsResponse handleSummaryClaims(Long serviceCenterId) {
        long count = this.warrancyClaimRepository.countByServiceCenterId(serviceCenterId);
        long emegencyCount = this.warrancyClaimRepository.countByServiceCenterIdAndPriority(serviceCenterId,
                WarrantyClaim.ClaimPriority.URGENT);

        return SummaryClaimsResponse.builder()
                .count(count)
                .emegency(emegencyCount)
                .build();
    }

    @Override
    public WarrantyClaim handleCreateClaim(ServiceCenter serviceCenter, WarrantyClaim input) {
        return null;
    }
}
