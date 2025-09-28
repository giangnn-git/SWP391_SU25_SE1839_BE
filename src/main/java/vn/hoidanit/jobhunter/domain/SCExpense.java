package vn.hoidanit.jobhunter.domain;

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

    public SCExpense() {
    }

    public SCExpense(ServiceCenter serviceCenter, RepairOrder repairOrder, Double amount, ExpenseStatus status,
            LocalDate paidDate) {
        this.serviceCenter = Objects.requireNonNull(serviceCenter, "ServiceCenter cannot be null");
        this.repairOrder = Objects.requireNonNull(repairOrder, "RepairOrder cannot be null");
        this.amount = amount != null ? amount : 0.0;
        this.status = status != null ? status : ExpenseStatus.UNPAID;
        this.paidDate = paidDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ServiceCenter getServiceCenter() {
        return serviceCenter;
    }

    public void setServiceCenter(ServiceCenter serviceCenter) {
        this.serviceCenter = serviceCenter;
    }

    public RepairOrder getRepairOrder() {
        return repairOrder;
    }

    public void setRepairOrder(RepairOrder repairOrder) {
        this.repairOrder = repairOrder;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public ExpenseStatus getStatus() {
        return status;
    }

    public void setStatus(ExpenseStatus status) {
        this.status = status;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
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
