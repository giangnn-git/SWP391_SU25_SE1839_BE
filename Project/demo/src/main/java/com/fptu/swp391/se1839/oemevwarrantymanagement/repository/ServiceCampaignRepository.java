package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ServiceCampaign;

@Repository
public interface ServiceCampaignRepository extends JpaRepository<ServiceCampaign, Long> {
        boolean existsByCode(String code);
}
