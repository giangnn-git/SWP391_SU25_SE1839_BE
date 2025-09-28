package vn.hoidanit.jobhunter.domain;

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

@Entity
@Table(name = "PartPolicy")
public class PartPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "partPolicy")
    private Set<WarrantyPolicy> warrantyPolicies = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "partId")
    private Part part;

    private LocalDate startDate = LocalDate.now();

    public PartPolicy() {
    }

    public PartPolicy(Set<WarrantyPolicy> warrantyPolicies, Part part, LocalDate startDate) {
        this.warrantyPolicies = warrantyPolicies != null ? warrantyPolicies : new HashSet<>();
        Objects.requireNonNull(part, "Part cannot be null");
        this.startDate = startDate;
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

    public Set<WarrantyPolicy> getWarrantyPolicies() {
        return warrantyPolicies;
    }

    public void setWarrantyPolicies(Set<WarrantyPolicy> warrantyPolicies) {
        this.warrantyPolicies = warrantyPolicies;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

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
