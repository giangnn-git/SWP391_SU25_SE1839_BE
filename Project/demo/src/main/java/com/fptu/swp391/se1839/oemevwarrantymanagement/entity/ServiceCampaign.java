package com.fptu.swp391.se1839.oemevwarrantymanagement.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
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
@Table(name = "ServiceCampaign")
public class ServiceCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    private LocalDate startDate = LocalDate.now();

    private LocalDate endDate;

    @OneToMany(mappedBy = "serviceCampaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CampaignVehicle> campaignVehicles = new HashSet<>();

    @OneToMany(mappedBy = "serviceCampaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<WarrantyClaim> warrantyClaims = new HashSet<>();

    @AssertTrue(message = "End date must be after start date")
    public boolean isEndDateValid() {
        return endDate == null || endDate.isAfter(startDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ServiceCampaign))
            return false;
        ServiceCampaign that = (ServiceCampaign) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ServiceCampaign [id=" + id + ", name=" + name + ", description=" + description + ", startDate="
                + startDate + ", endDate=" + endDate + ", campaignVehicle=" + campaignVehicles + ", warrantyClaim="
                + warrantyClaims.size() + "]";
    }
}
