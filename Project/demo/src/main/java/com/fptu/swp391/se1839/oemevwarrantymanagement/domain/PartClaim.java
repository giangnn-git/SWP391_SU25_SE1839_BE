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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "PartClaim", uniqueConstraints = @UniqueConstraint(columnNames = { "partId", "warrantyClaimId" }))
public class PartClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "partId", nullable = false)
    private Part part;

    @ManyToOne(optional = false)
    @JoinColumn(name = "warrantyClaimId", nullable = false)
    private WarrantyClaim warrantyClaim;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status/* = ClaimStatus.PENDING */;

    public enum ClaimStatus {
        PENDING, APPROVED, REJECTED
    }

    public PartClaim() {
    }

    public PartClaim(Part part, WarrantyClaim warrantyClaim,
            @Min(value = 1, message = "Quantity must be at least 1") int quantity, ClaimStatus status) {
        this.part = Objects.requireNonNull(part, "Part cannot be null");
        this.warrantyClaim = Objects.requireNonNull(warrantyClaim, "WarrantyClaim cannot be null");
        this.quantity = quantity;
        this.status = status != null ? status : ClaimStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public WarrantyClaim getWarrantyClaim() {
        return warrantyClaim;
    }

    public void setWarrantyClaim(WarrantyClaim warrantyClaim) {
        this.warrantyClaim = warrantyClaim;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PartClaim))
            return false;
        PartClaim that = (PartClaim) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "PartClaim{id=" + id + ", partId=" + (part != null ? part.getId() : null) + ", warrantyClaimId="
                + (warrantyClaim != null ? warrantyClaim.getId() : null) + ", quantity=" + quantity + ", status="
                + status + "}";
    }
}
