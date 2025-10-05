package com.fptu.swp391.se1839.oemevwarrantymanagement.entity;

import java.time.LocalDate;
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
@Table(name = "SCExpense")
public class SCExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "serviceCenterId", nullable = false)
    private ServiceCenter serviceCenter;

    @ManyToOne(optional = false)
    @JoinColumn(name = "repairOrderId", nullable = false)
    private RepairOrder repairOrder;

    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseStatus status;// = ExpenseStatus.UNPAID;

    private LocalDate paidDate;

    public enum ExpenseStatus {
        UNPAID, PAID, REJECTED
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SCExpense))
            return false;
        SCExpense that = (SCExpense) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "SCExpense{" + "id=" + id + ", serviceCenterId=" + (serviceCenter != null ? serviceCenter.getId() : null)
                + ", repairOrderId=" + (repairOrder != null ? repairOrder.getId() : null) + ", amount=" + amount
                + ", status=" + status + ", paidDate=" + paidDate + '}';
    }
}
