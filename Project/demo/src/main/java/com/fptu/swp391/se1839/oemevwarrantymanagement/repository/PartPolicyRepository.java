package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartPolicy;

import jakarta.transaction.Transactional;

@Repository
public interface PartPolicyRepository extends JpaRepository<PartPolicy, Long> {
        @Query("""
                            SELECT p FROM PartPolicy p
                            WHERE p.warrantyPolicy.id = :policyId
                              AND p.endDate >= :today
                        """)
        List<PartPolicy> findUnexpiredPartPolicies(@Param("policyId") Long policyId, @Param("today") LocalDate today);

        @Query("""
                            SELECT COUNT(p) > 0 FROM PartPolicy p
                            WHERE p.part.id = :partId
                              AND p.warrantyPolicy.id = :policyId
                              AND ((:startDate BETWEEN p.startDate AND p.endDate)
                                OR (:endDate BETWEEN p.startDate AND p.endDate)
                                OR (p.startDate BETWEEN :startDate AND :endDate))
                        """)
        boolean existsByPartIdAndWarrantyPolicyIdAndDateRangeOverlap(
                        Long partId, Long policyId, LocalDate startDate, LocalDate endDate);

        List<PartPolicy> findByPartId(Long partId);

        @Query("SELECT p FROM PartPolicy p WHERE p.part.id = :partId AND p.warrantyPolicy.type = 'NORMAL' "
                        + "AND ((:startDate BETWEEN p.startDate AND p.endDate) OR "
                        + "(:endDate BETWEEN p.startDate AND p.endDate) OR "
                        + "(p.startDate BETWEEN :startDate AND :endDate))")
        List<PartPolicy> findOverlappingNormalPolicies(Long partId, LocalDate startDate, LocalDate endDate);

        boolean existsByWarrantyPolicyIdAndStatus(Long warrantyPolicyId, PartPolicy.Status status);

        @Query("SELECT p FROM PartPolicy p WHERE p.warrantyPolicy.id = :policyId AND p.status = 'ACTIVE'")
        List<PartPolicy> findActivePartPoliciesByPolicyId(@Param("policyId") Long policyId);

        @Modifying
        @Transactional
        @Query("UPDATE PartPolicy p SET p.status = :status WHERE p.id IN :ids")
        void updateStatusByIds(@Param("ids") List<Long> ids, @Param("status") PartPolicy.Status status);

        boolean existsByWarrantyPolicyId(Long policyId);
}
