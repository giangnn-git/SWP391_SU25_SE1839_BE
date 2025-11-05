package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

  List<User> findByRole(User.Role role);

  @Query("""
      SELECT u FROM User u
      WHERE (:serviceCenterId IS NULL OR u.serviceCenter.id = :serviceCenterId)
        AND u.workStatus IN :statuses
        AND u.role = :role
      """)
  List<User> findByWorkStatusInAndServiceCenterIdAndRole(
      @Param("statuses") List<User.WorkStatus> statuses,
      @Param("serviceCenterId") Long serviceCenterId,
      @Param("role") User.Role role);

  @Query("""
      SELECT u FROM User u
      WHERE u.workStatus IN :statuses
        AND u.role = :role
      """)
  List<User> findByWorkStatusInAndRole(
      @Param("statuses") List<User.WorkStatus> statuses,
      @Param("role") User.Role role);

  @Query("""
      SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
      FROM User u
      WHERE u.id = :techId AND u.date = :day
      """)
  boolean existsByTechnicianIdAndDate(@Param("techId") long techId, @Param("day") LocalDate day);

  User findByName(String techName);
}