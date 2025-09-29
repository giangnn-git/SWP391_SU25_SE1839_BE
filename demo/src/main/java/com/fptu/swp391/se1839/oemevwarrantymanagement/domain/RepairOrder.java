package com.fptu.swp391.se1839.oemevwarrantymanagement.domain;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "RepairOrder")
public class RepairOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "claimID", nullable = false)
    private WarrantyClaim warrantyClaim;

    @ManyToOne(optional = false)
    @JoinColumn(name = "userId", nullable = false)
    private User technical;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;// = OrderStatus.PENDING;

    @OneToMany(mappedBy = "repairOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RepairDetail> repairDetails = new HashSet<>();

    @OneToMany(mappedBy = "repairOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SCExpense> scExpenses = new HashSet<>();

    public enum OrderStatus {
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public RepairOrder() {
    }

    public RepairOrder(WarrantyClaim warrantyClaim, User technical, LocalDate startDate, LocalDate endDate,
            OrderStatus status, Set<RepairDetail> repairDetails, Set<SCExpense> scExpenses) {
        this.warrantyClaim = Objects.requireNonNull(warrantyClaim, "WarrantyClaim cannot be null");
        this.technical = Objects.requireNonNull(technical, "Technician cannot be null");
        this.startDate = startDate != null ? startDate : LocalDate.now();
        this.endDate = endDate;
        this.status = status != null ? status : OrderStatus.PENDING;
        this.repairDetails = repairDetails != null ? repairDetails : new HashSet<>();
        this.scExpenses = scExpenses != null ? scExpenses : new HashSet<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WarrantyClaim getWarrantyClaim() {
        return warrantyClaim;
    }

    public void setWarrantyClaim(WarrantyClaim warrantyClaim) {
        this.warrantyClaim = warrantyClaim;
    }

    public User getTechnician() {
        return technical;
    }

    public void setTechnician(User technician) {
        this.technical = technician;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Set<RepairDetail> getRepairDetails() {
        return repairDetails;
    }

    public void setRepairDetails(Set<RepairDetail> repairDetails) {
        this.repairDetails = repairDetails;
    }

    public Set<SCExpense> getScExpenses() {
        return scExpenses;
    }

    public void setScExpenses(Set<SCExpense> scExpenses) {
        this.scExpenses = scExpenses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RepairOrder))
            return false;
        RepairOrder that = (RepairOrder) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "RepairOrder{" + "id=" + id + ", warrantyClaimId="
                + (warrantyClaim != null ? warrantyClaim.getId() : null) + ", technicianId="
                + (technical != null ? technical.getId() : null) + ", startDate=" + startDate + ", endDate=" + endDate
                + ", status=" + status + ", repairDetailsCount=" + (repairDetails != null ? repairDetails.size() : 0)
                + ", scExpensesCount=" + (scExpenses != null ? scExpenses.size() : 0) + '}';
    }
}

