package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import java.util.Optional;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Part;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Vehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.VehiclePart;

@Repository
public interface VehiclePartRepository extends JpaRepository<VehiclePart, String> {

        /**
         * 🔹 Tìm bộ phận (part) đang gắn trên xe (vehicle) — chưa bị tháo (removalDate
         * IS NULL).
         */
        @Query("""
                        SELECT vp
                        FROM VehiclePart vp
                        WHERE vp.vehicle = :vehicle
                          AND vp.part = :part
                          AND vp.removalDate IS NULL
                        """)
        Optional<VehiclePart> findActiveVehiclePart(@Param("vehicle") Vehicle vehicle, @Param("part") Part part);

        /**
         * 🔹 Tìm VehiclePart theo VIN, PartId và ClaimId.
         */
        Optional<VehiclePart> findByVehicleVinAndPartIdAndWarrantyClaimId(String vin, Long partId, Long claimId);

        /**
         * 🔹 Tìm VehiclePart theo VIN và PartId (bất kể có claim hay chưa).
         */
        Optional<VehiclePart> findByVehicleVinAndPartId(String vin, Long partId);

        /**
         * 🔹 Lấy danh sách các part đang gắn trên xe theo VIN và danh sách partId.
         * Chỉ lấy các part chưa bị tháo (removalDate IS NULL).
         */
        @Query("""
                        SELECT vp
                        FROM VehiclePart vp
                        WHERE vp.vehicle.vin = :vin
                          AND vp.part.id IN :partIds
                          AND vp.removalDate IS NULL
                        """)
        List<VehiclePart> findActiveByVehicleVinAndPartIdIn(
                        @Param("vin") String vin,
                        @Param("partIds") Collection<Long> partIds);

        @Query("SELECT vp FROM VehiclePart vp WHERE vp.part.id = :partId AND vp.vehicle.vin = :vin AND vp.removalDate IS NULL")
        Optional<VehiclePart> findActivePart(@Param("partId") Long partId, @Param("vin") String vin);
}
