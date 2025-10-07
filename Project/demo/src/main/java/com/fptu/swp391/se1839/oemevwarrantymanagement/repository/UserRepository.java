package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmail(String email);

    boolean existsById(Long id);

    List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase(
            String name, String email, String phoneNumber);
}
