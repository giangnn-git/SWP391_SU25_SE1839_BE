package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyClaim;

@Repository
public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim, Long> {
        Long countByServiceCenterId(Long serviceCenterId);

        int countByServiceCenterIdAndPriority(Long serviceCenterId, WarrantyClaim.ClaimPriority status);

        Long countByServiceCenterIdAndStatus(Long ServiceCenterId, WarrantyClaim.ClaimStatus status);

        List<WarrantyClaim> findByVehicleVinAndClaimDate(String vin, LocalDate date);

        List<WarrantyClaim> findByServiceCenterIdAndStatus(Long serviceCenterId, WarrantyClaim.ClaimStatus status);

        List<WarrantyClaim> findByServiceCenterIdAndStatusAndUserId(Long serviceCenterId,
                        WarrantyClaim.ClaimStatus status,
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

        @Query("""
                            SELECT COUNT(DISTINCT wc)
                            FROM WarrantyClaim wc
                            JOIN wc.partClaims pc
                            WHERE wc.serviceCenter.id = :serviceCenterId
                            AND EXISTS (
                                SELECT 1
                                FROM WarrantyClaim c
                                JOIN c.partClaims pc2
                                WHERE c.vehicle.id = wc.vehicle.id
                                  AND pc2.part.id = pc.part.id
                                  AND c.id <> wc.id
                            )
                        """)
        long countRepeatClaims(@Param("serviceCenterId") Long serviceCenterId);

        @Query("""
                            SELECT COUNT(wc)
                            FROM WarrantyClaim wc
                            WHERE wc.serviceCenter.id = :serviceCenterId
                              AND wc.claimDate BETWEEN :startDate AND :endDate
                        """)
        long countByServiceCenterIdAndClaimDateBetween(
                        @Param("serviceCenterId") Long serviceCenterId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("""
                            SELECT COUNT(wc)
                            FROM WarrantyClaim wc
                            WHERE wc.serviceCenter.id = :serviceCenterId
                              AND wc.claimDate BETWEEN :startDate AND :endDate
                              AND wc.vehicle.id IN (
                                  SELECT wc2.vehicle.id
                                  FROM WarrantyClaim wc2
                                  WHERE wc2.serviceCenter.id = :serviceCenterId
                                  GROUP BY wc2.vehicle.id
                                  HAVING COUNT(wc2.id) > 1
                              )
                        """)
        long countRepeatClaimsInRange(
                        @Param("serviceCenterId") Long serviceCenterId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT v.model, COUNT(wc) " +
                        "FROM WarrantyClaim wc JOIN wc.vehicle v " +
                        "WHERE wc.serviceCenter.id = :serviceCenterId " +
                        "GROUP BY v.model")
        List<Object[]> countFailuresByVehicleModel(@Param("serviceCenterId") Long serviceCenterId);

        @Query("SELECT p.partCategory AS category, COUNT(wc) AS count " +
                        "FROM WarrantyClaim wc " +
                        "JOIN wc.partClaims pc " +
                        "JOIN pc.part p " +
                        "WHERE wc.serviceCenter.id = :serviceCenterId " +
                        "GROUP BY p.partCategory")
        List<Object[]> countServiceCenterIdAndVehicleModel(@Param("serviceCenterId") Long serviceCenterId);

        @Query("SELECT wc.priority, COUNT(wc) " +
                        "FROM WarrantyClaim wc " +
                        "WHERE wc.serviceCenter.id = :serviceCenterId " +
                        "GROUP BY wc.priority")
        List<Object[]> countServiceCenterAndPriority(@Param("serviceCenterId") Long serviceCenterId);

        @Query("SELECT w FROM WarrantyClaim w " +
                        "WHERE w.serviceCenter.id = :serviceCenterId " +
                        "AND YEAR(w.claimDate) = :year " +
                        "AND MONTH(w.claimDate) = :month")
        List<WarrantyClaim> findByServiceCenterAndMonth(
                        @Param("serviceCenterId") long serviceCenterId,
                        @Param("year") int year,
                        @Param("month") int month);

        @Query("""
                            SELECT c FROM WarrantyClaim c
                            JOIN c.partClaims pc
                            WHERE c.vehicle.vin = :vin
                              AND c.serviceCenter.id = :serviceCenterId
                              AND pc.part.id = :partId
                              AND c.status NOT IN ('COMPLETED', 'CANCELLED', 'REJECTED')
                        """)
        List<WarrantyClaim> findActiveClaimsForPart(String vin, Long serviceCenterId, Long partId);

        long countByServiceCenterIdAndStatusIn(Long serviceCenterId, List<WarrantyClaim.ClaimStatus> statuses);

        List<WarrantyClaim> findByServiceCenterIdAndStatusIn(Long serviceCenterId,
                        List<WarrantyClaim.ClaimStatus> statuses);

}