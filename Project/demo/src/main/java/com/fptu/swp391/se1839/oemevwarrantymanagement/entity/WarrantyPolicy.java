package com.fptu.swp391.se1839.oemevwarrantymanagement.entity;

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
