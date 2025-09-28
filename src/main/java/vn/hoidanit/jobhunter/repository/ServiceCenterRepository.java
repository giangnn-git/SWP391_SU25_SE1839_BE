package vn.hoidanit.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hoidanit.jobhunter.domain.ServiceCenter;

@Repository
public interface ServiceCenterRepository extends JpaRepository<ServiceCenter, Long> {

}
