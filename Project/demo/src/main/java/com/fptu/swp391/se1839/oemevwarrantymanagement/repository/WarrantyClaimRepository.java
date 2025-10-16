package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyClaim;

@Repository
public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim, Long> {
    Long countByServiceCenterId(Long serviceCenterId);

    Long countByServiceCenterIdAndPriority(Long serviceCenterId, WarrantyClaim.ClaimPriority status);

    Long countByServiceCenterIdAndStatus(Long ServiceCenterId, WarrantyClaim.ClaimStatus status);

    List<WarrantyClaim> findByVehicleVinAndClaimDate(String vin, LocalDate date);

    List<WarrantyClaim> findByServiceCenterIdAndStatus(Long serviceCenterId, WarrantyClaim.ClaimStatus status);

    List<WarrantyClaim> findByServiceCenterIdAndStatusAndUserId(Long serviceCenterId, WarrantyClaim.ClaimStatus status,
            Long userId);

    List<WarrantyClaim> findByServiceCenterId(Long serviceCenterId);

    List<WarrantyClaim> findByServiceCenterIdAndUserId(Long serviceCenterId, Long userId);

    List<WarrantyClaim> findByServiceCenterIdAndVehicleVin(Long serviceCenterId, String vin);

    List<WarrantyClaim> findByServiceCenterIdAndVehicleVinAndUserId(Long serviceCenterId, String vin, Long userId);

    List<WarrantyClaim> findByServiceCenterIdAndVehicleVinAndStatus(Long serviceCenterId, String vin,
            WarrantyClaim.ClaimStatus status);

    List<WarrantyClaim> findByServiceCenterIdAndVehicleVinAndStatusAndUserId(Long serviceCenterId, String vin,
            WarrantyClaim.ClaimStatus status, Long userId);

    @Query("""
                SELECT wc FROM WarrantyClaim wc
                JOIN wc.vehicle v
                JOIN v.customer c
                WHERE wc.serviceCenter.id = :serviceCenterId
                  AND LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<WarrantyClaim> findByCustomerName(
            @Param("serviceCenterId") Long serviceCenterId,
            @Param("keyword") String keyword);

    @Query("""
                SELECT wc FROM WarrantyClaim wc
                JOIN wc.vehicle v
                JOIN v.customer c
                WHERE wc.serviceCenter.id = :serviceCenterId
                AND wc.userId = :userId
                  AND LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<WarrantyClaim> findByCustomerNameAndUserId(
            @Param("serviceCenterId") Long serviceCenterId,
            @Param("keyword") String keyword,
            @Param("userId") Long userId);

    @Query("""
                SELECT wc FROM WarrantyClaim wc
                JOIN wc.vehicle v
                JOIN v.customer c
                WHERE wc.serviceCenter.id = :serviceCenterId
                  AND LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  AND wc.status = :status
            """)
    List<WarrantyClaim> findByServiceCenterIdAndCustomerNameAndStatus(
            @Param("serviceCenterId") Long serviceCenterId,
            @Param("keyword") String keyword,
            @Param("status") WarrantyClaim.ClaimStatus status);

    @Query("""
                SELECT wc FROM WarrantyClaim wc
                JOIN wc.vehicle v
                JOIN v.customer c
                WHERE wc.serviceCenter.id = :serviceCenterId
                AND wc.userId = :userId
                  AND LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  AND wc.status = :status
            """)
    List<WarrantyClaim> findByServiceCenterIdAndCustomerNameAndStatusAndUserId(
            @Param("serviceCenterId") Long serviceCenterId,
            @Param("keyword") String keyword,
            @Param("status") WarrantyClaim.ClaimStatus status,
            @Param("userId") Long userId);
}