package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartClaim;

@Repository
public interface PartClaimRepository extends JpaRepository<PartClaim, Long> {

        Long countByWarrantyClaimServiceCenterId(Long serviceCenterId);

        List<PartClaim> findByWarrantyClaimId(Long claimId);

        @Query("SELECT p.partCategory, COUNT(pc) " +
                        "FROM PartClaim pc " +
                        "JOIN pc.warrantyClaim wc " +
                        "JOIN pc.part p " +
                        "WHERE wc.serviceCenter.id = :serviceCenterId " +
                        "GROUP BY p.partCategory")
        List<Object[]> countFailuresByCategory(@Param("serviceCenterId") Long serviceCenterId);

        @Query("SELECT p, COUNT(pc) " +
                        "FROM PartClaim pc " +
                        "JOIN pc.warrantyClaim wc " +
                        "JOIN pc.part p " +
                        "WHERE wc.serviceCenter.id = :serviceCenterId " +
                        "GROUP BY p")
        List<Object[]> countFailuresByComponent(@Param("serviceCenterId") Long serviceCenterId);

        @Query("SELECT pc FROM PartClaim pc " +
                        "JOIN pc.warrantyClaim wc " +
                        "JOIN pc.part p " +
                        "WHERE wc.serviceCenter.id = :serviceCenterId " +
                        "AND p.partCategory = :component")
        List<PartClaim> findByServiceCenterAndComponent(@Param("serviceCenterId") Long serviceCenterId,
                        @Param("component") String component);
}
