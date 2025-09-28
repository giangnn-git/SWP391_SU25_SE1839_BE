package vn.hoidanit.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hoidanit.jobhunter.domain.Vehicle;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {

    Vehicle findByVin(String vin);
}
