package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyClaim;

@Repository
public interface WarrancyClaimRepository extends JpaRepository<WarrantyClaim, Long> {
    Long countByServiceCenterId(Long serviceCenterId);

    Long countByServiceCenterIdAndPriority(Long serviceCenterId, WarrantyClaim.ClaimPriority status);

    Long countByServiceCenterIdAndStatus(Long ServiceCenterId, WarrantyClaim.ClaimStatus status);

    List<WarrantyClaim> findByVehicleVinAndClaimDate(String vin, LocalDate date);
}
