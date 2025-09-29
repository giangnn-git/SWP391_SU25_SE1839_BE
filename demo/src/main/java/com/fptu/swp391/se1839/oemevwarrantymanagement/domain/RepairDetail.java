package com.fptu.swp391.se1839.oemevwarrantymanagement.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "RepairDetail")
public class RepairDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "repairOrderId", nullable = false)
    private RepairOrder repairOrder;

    @ManyToOne(optional = false)
    @JoinColumn(name = "partID", nullable = false)
    private Part part;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DetailStatus status = DetailStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String description;

    public enum DetailStatus {
        PENDING, USED, REPLACED, REJECTED
    }

    public RepairDetail() {
    }

    public RepairDetail(RepairOrder repairOrder, Part part, DetailStatus status, String description) {
        this.repairOrder = Objects.requireNonNull(repairOrder, "RepairOrder cannot be null");
        this.part = Objects.requireNonNull(part, "Part cannot be null");
        this.status = status != null ? status : DetailStatus.PENDING;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RepairOrder getRepairOrder() {
        return repairOrder;
    }

    public void setRepairOrder(RepairOrder repairOrder) {
        this.repairOrder = repairOrder;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public DetailStatus getStatus() {
        return status;
    }

    public void setStatus(DetailStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RepairDetail))
            return false;
        RepairDetail that = (RepairDetail) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "RepairDetail{" + "id=" + id + ", repairOrderId=" + (repairOrder != null ? repairOrder.getId() : null)
                + ", partId=" + (part != null ? part.getId() : null) + ", status=" + status + ", description='"
                + description + '\'' + '}';
    }
}

