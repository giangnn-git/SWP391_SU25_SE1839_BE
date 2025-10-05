package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.InvalidToken;

@Repository
public interface InvalidTokenRepository extends JpaRepository<InvalidToken, String> {

}
