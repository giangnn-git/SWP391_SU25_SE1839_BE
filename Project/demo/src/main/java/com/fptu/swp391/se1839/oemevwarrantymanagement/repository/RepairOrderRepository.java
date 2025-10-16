package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairOrder;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyClaim;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RepairOrderRepository extends JpaRepository<RepairOrder, Long> {

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
                                SELECT ro FROM RepairOrder ro
                        Join ro.warrantyClaim wc
                        Where wc.serviceCenter.id = :serviceCenterId
                        And ro.status = :status
                        And ro.technical.id = :userId
                        """)
        List<RepairOrder> findByServiceCenterIdAndStatusAndUserId(@Param("serviceCenterId") Long serviceCenterId,
                        @Param("status") RepairOrder.OrderStatus status, @Param("userId") Long userId);

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
                            WHERE ro.technical.id = :userId
                            And wc.serviceCenter.id = :serviceCenterId
                        """)
        List<RepairOrder> findByServiceCenterIdAndUserId(@Param("serviceCenterId") Long serviceCenterId,
                        @Param("userId") Long userId);

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
                            WHERE wc.serviceCenter.id = :serviceCenterId
                            And wc.vehicle.vin = :vehicleVin
                            And ro.technical.id = :userId
                        """)
        List<RepairOrder> findByServiceCenterIdAndVehicleVinAndUserId(@Param("serviceCenterId") Long serviceCenterId,
                        @Param("vehicleVin") String vin, @Param("userId") Long userId);

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
                            JOIN wc.vehicle v
                            JOIN v.customer c
                            WHERE wc.serviceCenter.id = :serviceCenterId
                            AND LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            AND ro.technical.id = :userId
                        """)
        List<RepairOrder> findByCustomerNameAndUserId(@Param("serviceCenterId") Long serviceCenterId,
                        @Param(":keyword") String name, @Param("userId") Long userId);

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
                            WHERE wc.serviceCenter.id = :serviceCenterId
                            And wc.vehicle.vin = :vehicleVin
                            And ro.status = :status
                            AND ro.technical.id = :userId
                        """)
        List<RepairOrder> findByServiceCenterIdAndVehicleVinAndStatusAndUserId(
                        @Param("serviceCenterId") Long serviceCenterId,
                        @Param("vehicleVin") String vin, @Param("status") RepairOrder.OrderStatus status,
                        @Param("userId") Long userId);

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

        @Query("""
                            SELECT ro FROM RepairOrder ro
                            JOIN ro.warrantyClaim wc
                            JOIN wc.vehicle v
                            JOIN v.customer c
                            WHERE wc.serviceCenter.id = :serviceCenterId
                            AND LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            And ro.status = :status
                            AND ro.technical.id = :userId
                        """)
        List<RepairOrder> findByServiceCenterIdAndCustomerNameAndStatusAndUserId(
                        @Param("serviceCenterId") Long serviceCenterId,
                        @Param(":keyword") String name, @Param("status") RepairOrder.OrderStatus status,
                        @Param("userId") Long userId);

        @Query("SELECT COUNT(ro) FROM RepairOrder ro WHERE ro.technical.id = :techId AND ro.status = :status1 or ro.status = :status2")
        long countWaitingJobsByTechnician(@Param("techId") Long techId,
                        @Param("status1") RepairOrder.OrderStatus status1,
                        @Param("status2") RepairOrder.OrderStatus status2);

        @Query("""
                            SELECT COUNT(ro) > 0
                            FROM RepairOrder ro
                            JOIN ro.warrantyClaim wc
                            WHERE ro.id = :repairOrderId
                              AND wc.serviceCenter.id = :serviceCenterId
                              AND wc.status = :status
                        """)
        boolean existsByRepairOrderIdAndServiceCenterIdAndStatus(
                        @Param("repairOrderId") long repairOrderId,
                        @Param("serviceCenterId") long serviceCenterId,
                        @Param("status") WarrantyClaim.ClaimStatus status);

        @Query("SELECT count(r) FROM RepairOrder r WHERE r.technical.id = :techId AND r.status IN :statuses")
        long countByTechnicalAndStatusIn(@Param("techId") Long techId,
                        @Param("statuses") List<RepairOrder.OrderStatus> statuses);

        @Query("""
                        SELECT COALESCE(MAX(ro.estimated), 0)
                        FROM RepairOrder ro
                        WHERE ro.technical.id = :technicalId
                        AND ro.status IN :statuses
                        """)
        int findMaxEstimatedByTechnical(
                        @Param("technicalId") Long technicalId,
                        @Param("statuses") List<RepairOrder.OrderStatus> statuses);

        // Lấy startDate mới nhất
        @Query("SELECT MAX(ro.startDate) FROM RepairOrder ro " +
                        "WHERE ro.technical.id = :technicalId AND ro.status IN :statuses")
        LocalDateTime findLatestStartByTechnical(@Param("technicalId") Long technicalId,
                        @Param("statuses") List<RepairOrder.OrderStatus> statuses);
}
