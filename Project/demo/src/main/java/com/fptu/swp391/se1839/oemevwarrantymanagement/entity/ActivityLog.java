package com.fptu.swp391.se1839.oemevwarrantymanagement.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ActivityLog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String title;
    String status;
    String description;
    String detail;
    LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String meta;

    @ManyToOne
    @JoinColumn(name = "vehicleId")
    Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "claimId")
    WarrantyClaim claim;

    @ManyToOne
    @JoinColumn(name = "repairOrderId")
    RepairOrder repairOrder;
}
