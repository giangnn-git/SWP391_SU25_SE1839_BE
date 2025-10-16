package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ClaimAttachment;

@Repository
public interface ClaimAttachmentRepository extends JpaRepository<ClaimAttachment, Long> {
    Optional<ClaimAttachment> findByName(String filename);

}
