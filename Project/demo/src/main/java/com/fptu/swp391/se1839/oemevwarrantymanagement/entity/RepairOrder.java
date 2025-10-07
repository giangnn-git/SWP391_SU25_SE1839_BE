package com.fptu.swp391.se1839.oemevwarrantymanagement.entity;

import java.time.LocalDateTime;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
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
    @JoinColumn(name = "userId")
    private User technical;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.WAITING;

    @OneToMany(mappedBy = "repairOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RepairDetail> repairDetails = new HashSet<>();

    @OneToMany(mappedBy = "repairOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<SCExpense> scExpenses = new HashSet<>();

    public enum OrderStatus {
        WAITING, PENDING, IN_PROGRESS, COMPLETED, CANCELLED
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
