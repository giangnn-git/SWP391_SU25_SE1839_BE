package vn.hoidanit.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hoidanit.jobhunter.domain.WarrantyClaim;

@Repository
public interface WarrancyClaimRepository extends JpaRepository<WarrantyClaim, Long> {

}
