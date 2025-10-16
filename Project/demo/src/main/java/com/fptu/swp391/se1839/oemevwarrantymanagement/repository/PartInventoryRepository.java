package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartInventory;

public interface PartInventoryRepository extends JpaRepository<PartInventory, Long> {
    List<PartInventory> findByServiceCenter_Id(Long serviceCenterId);

    List<PartInventory> findByPart_Id(Long partId);
}
