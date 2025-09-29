package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fptu.swp391.se1839.oemevwarrantymanagement.domain.WarrantyClaim;

@Repository
public interface WarrancyClaimRepository extends JpaRepository<WarrantyClaim, Long> {
    Long countByServiceCenterId(Long serviceCenterId);

    Long countByServiceCenterIdAndPriority(Long serviceCenterId, WarrantyClaim.ClaimPriority status);
}
