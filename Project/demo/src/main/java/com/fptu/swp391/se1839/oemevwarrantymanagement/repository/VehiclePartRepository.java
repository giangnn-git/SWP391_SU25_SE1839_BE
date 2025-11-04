package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Part;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Vehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.VehiclePart;

@Repository
public interface VehiclePartRepository extends JpaRepository<VehiclePart, String> {
    @Query("SELECT vp FROM VehiclePart vp WHERE vp.vehicle = :vehicle AND vp.part = :part AND vp.removalDate IS NULL")
    Optional<VehiclePart> findActiveVehiclePart(@Param("vehicle") Vehicle vehicle, @Param("part") Part part);

    Optional<VehiclePart> findByVehicleVinAndPartIdAndWarrantyClaimId(String vin, Long partId, Long claimId);

    Optional<VehiclePart> findByVehicleVinAndPartId(String vin, Long partId);
}
