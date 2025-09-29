package com.fptu.swp391.se1839.oemevwarrantymanagement.domain;


import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "WarrantyPolicy")
public class WarrantyPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @Min(value = 1, message = "Duration must be at least 1 month")
    @Max(value = 120, message = "Duration cannot exceed 120 months") // tối đa 10 năm
    private int durationPeriod;

    @Min(value = 0, message = "Mileage limit cannot be negative")
    @Max(value = 1000000, message = "Mileage limit cannot exceed 1,000,000 km")
    private int mileageLimit;

    @ManyToOne
    @JoinColumn(name = "partPolicyId") // Khóa ngoại trỏ sang PartPolicy
    private PartPolicy partPolicy;

    public WarrantyPolicy() {
    }

    public WarrantyPolicy(@Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters") String name,
            @Min(value = 1, message = "Duration must be at least 1 month") @Max(value = 120, message = "Duration cannot exceed 120 months") int durationPeriod,
            @Min(value = 0, message = "Mileage limit cannot be negative") @Max(value = 1000000, message = "Mileage limit cannot exceed 1,000,000 km") int mileageLimit,
            PartPolicy partPolicy) {
        this.name = name;
        this.durationPeriod = durationPeriod;
        this.mileageLimit = mileageLimit;
        this.partPolicy = partPolicy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDurationPeriod() {
        return durationPeriod;
    }

    public void setDurationPeriod(int durationPeriod) {
        this.durationPeriod = durationPeriod;
    }

    public int getMileageLimit() {
        return mileageLimit;
    }

    public void setMileageLimit(int mileageLimit) {
        this.mileageLimit = mileageLimit;
    }

    public PartPolicy getPartPolicies() {
        return partPolicy;
    }

    public void setPartPolicies(PartPolicy partPolicies) {
        this.partPolicy = partPolicies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WarrantyPolicy))
            return false;
        WarrantyPolicy that = (WarrantyPolicy) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "WarrantyPolicy{id=" + id + ", name='" + name + '\'' + ", durationPeriod=" + durationPeriod
                + ", mileageLimit=" + mileageLimit + ", partPolicyId="
                + (partPolicy != null ? partPolicy.getId() : "null") + "}";
    }
}

