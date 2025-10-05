package com.fptu.swp391.se1839.oemevwarrantymanagement.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Entity;
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
@Table(name = "PartPolicy")
public class PartPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "partPolicy")
    @Builder.Default
    private Set<WarrantyPolicy> warrantyPolicies = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "partId")
    private Part part;

    @Builder.Default
    private LocalDate startDate = LocalDate.now();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PartPolicy))
            return false;
        PartPolicy that = (PartPolicy) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "PartPolicy{id=" + id + ", startDate=" + startDate + ", warrantyPolicyCount="
                + (warrantyPolicies != null ? warrantyPolicies.size() : 0) + "}";
    }
}
