package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ServiceCampaign;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Vehicle;

@Repository
public interface ServiceCampaignRepository extends JpaRepository<ServiceCampaign, Long> {
        boolean existsByCode(String code);

        @Query("SELECT DISTINCT sc FROM ServiceCampaign sc " +
                        "JOIN CampaignVehicle cv ON sc = cv.serviceCampaign " +
                        "WHERE cv.vehicle = :vehicle")
        List<ServiceCampaign> findByVehicle(@Param("vehicle") Vehicle vehicle);
}
