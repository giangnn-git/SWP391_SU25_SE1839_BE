package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairOrder;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RepairOrderReposity extends JpaRepository<RepairOrder, Long> {

    @Query("Select Count(r.Id)" +
            "From RepairOrder r JOIN r.technical t " +
            "where t.serviceCenter.id = :ServiceCenterId " +
            "and r.startDate between :StartDate and :EndDate")
    long countRepairFlWeek(@Param("ServiceCenterId") long serviceCenterId,
            @Param("StartDate") LocalDate startDate, @Param("EndDate") LocalDate endDate);

    @Query("Select Count(r.Id)" +
            "From RepairOrder r JOIN r.technical t " +
            "where t.serviceCenter.Id = :ServiceCenterId " +
            "and r.status = :Status")
    long countRepairFlStatus(@Param("ServiceCenterId") long serviceCenterId,
            @Param("Status") RepairOrder.OrderStatus status);

    @Query("Select Count(r.Id)" +
            "From RepairOrder r JOIN r.technical t " +
            "where t.serviceCenter.Id = :ServiceCenterId " +
            "and r.startDate between :StartDate and :EndDate " +
            "and r.status = :Status")
    long countRepairFlStatusAndMonth(@Param("ServiceCenterId") long serviceCenterId,
            @Param("Status") RepairOrder.OrderStatus status, @Param("StartDate") LocalDate startDate,
            @Param("EndDate") LocalDate endDate);

    @Query("""
                    SELECT ro FROM RepairOrder ro
            Join ro.warrantyClaim wc
            Where wc.serviceCenter.id = :serviceCenterId
            And ro.status = :status
            """)
    List<RepairOrder> findByServiceCenterIdAndStatus(@Param("serviceCenterId") Long serviceCenterId,
            @Param("status") RepairOrder.OrderStatus status);

    @Query("""
                SELECT count(ro.id) FROM RepairOrder ro
                JOIN ro.warrantyClaim wc
                WHERE wc.serviceCenter.id = :serviceCenterId
                AND ro.status = :status
            """)
    long countByServiceCenterIdAndStatus(
            @Param("serviceCenterId") Long serviceCenterId,
            @Param("status") RepairOrder.OrderStatus status);

    @Query("""
                SELECT ro FROM RepairOrder ro
                JOIN ro.warrantyClaim wc
                WHERE wc.serviceCenter.id = :serviceCenterId
            """)
    List<RepairOrder> findByServiceCenterId(@Param("serviceCenterId") Long serviceCenterId);

    @Query("""
                SELECT ro FROM RepairOrder ro
                JOIN ro.warrantyClaim wc
                WHERE wc.serviceCenter.id = :serviceCenterId
                And wc.vehicle.vin = :vehicleVin
            """)
    List<RepairOrder> findByServiceCenterIdAndVehicleVin(@Param("serviceCenterId") Long serviceCenterId,
            @Param("vehicleVin") String vin);

    @Query("""
                SELECT ro FROM RepairOrder ro
                JOIN ro.warrantyClaim wc
                JOIN wc.vehicle v
                JOIN v.customer c
                WHERE wc.serviceCenter.id = :serviceCenterId
                AND LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<RepairOrder> findByCustomerName(@Param("serviceCenterId") Long serviceCenterId,
            @Param(":keyword") String name);

    @Query("""
                SELECT ro FROM RepairOrder ro
                JOIN ro.warrantyClaim wc
                WHERE wc.serviceCenter.id = :serviceCenterId
                And wc.vehicle.vin = :vehicleVin
                And ro.status = :status
            """)
    List<RepairOrder> findByServiceCenterIdAndVehicleVinAndStatus(@Param("serviceCenterId") Long serviceCenterId,
            @Param("vehicleVin") String vin, @Param("status") RepairOrder.OrderStatus status);

    @Query("""
                SELECT ro FROM RepairOrder ro
                JOIN ro.warrantyClaim wc
                JOIN wc.vehicle v
                JOIN v.customer c
                WHERE wc.serviceCenter.id = :serviceCenterId
                AND LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                And ro.status = :status
            """)
    List<RepairOrder> findByServiceCenterIdAndCustomerNameAndStatus(@Param("serviceCenterId") Long serviceCenterId,
            @Param(":keyword") String name, @Param("status") RepairOrder.OrderStatus status);
}
