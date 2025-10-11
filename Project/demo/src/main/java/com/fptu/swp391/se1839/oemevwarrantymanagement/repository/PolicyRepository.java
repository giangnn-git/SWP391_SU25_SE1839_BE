package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyPolicy;
@Repository
public interface PolicyRepository extends JpaRepository<WarrantyPolicy, Long> {    
    boolean existsByName(String name);
    WarrantyPolicy findByName(String name);
}
