package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Part;

@Repository
public interface PartRepository extends JpaRepository<Part, Long> {
    List<Part> findByPartCategory(String name);
}
