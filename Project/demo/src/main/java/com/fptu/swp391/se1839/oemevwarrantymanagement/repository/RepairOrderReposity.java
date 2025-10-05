package com.fptu.swp391.se1839.oemevwarrantymanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairOrder;

import java.time.LocalDate;

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

}
