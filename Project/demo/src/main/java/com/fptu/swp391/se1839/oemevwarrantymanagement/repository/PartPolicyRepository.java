package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartPolicy;

@Repository
public interface PartPolicyRepository extends JpaRepository<PartPolicy, Long> {

        @Query("SELECT p FROM PartPolicy p WHERE p.warrantyPolicy.id = :policyId AND p.endDate > :today")
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
}
