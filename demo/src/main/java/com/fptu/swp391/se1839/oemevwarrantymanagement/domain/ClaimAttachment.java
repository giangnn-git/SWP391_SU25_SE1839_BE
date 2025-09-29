package com.fptu.swp391.se1839.oemevwarrantymanagement.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "ClaimAttachment")
public class ClaimAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "claimID", nullable = false)
    private WarrantyClaim warrantyClaim;

    @NotBlank
    @Column(nullable = false)
    private String filePath;

    @Column(columnDefinition = "TEXT")
    private String description;

    public ClaimAttachment() {
    }

    public ClaimAttachment(WarrantyClaim warrantyClaim, @NotBlank String filePath, String description) {
        this.warrantyClaim = Objects.requireNonNull(warrantyClaim, "WarrantyClaim cannot be null");
        this.filePath = Objects.requireNonNull(filePath, "FilePath cannot be null or blank");
        this.description = description;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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
        if (!(o instanceof ClaimAttachment))
            return false;
        ClaimAttachment that = (ClaimAttachment) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ClaimAttachment{id=" + id + ", warrantyClaimId="
                + (warrantyClaim != null ? warrantyClaim.getId() : null) + ", filePath='" + filePath + '\''
                + ", description='" + description + '\'' + '}';
    }
}

