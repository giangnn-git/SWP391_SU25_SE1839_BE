package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Part;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartInventory;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ServiceCenter;

public interface PartInventoryRepository extends JpaRepository<PartInventory, Long> {
    List<PartInventory> findByServiceCenter_Id(Long serviceCenterId);

    Optional<PartInventory> findByPart_Id(Long partId);

    int countByServiceCenterId(long serviceCenterId);

    Optional<PartInventory> findByPartAndServiceCenter(Part part, ServiceCenter serviceCenter);

    long countByServiceCenterIdAndQuantityGreaterThan(long serviceCenterId, int quantity);
}
