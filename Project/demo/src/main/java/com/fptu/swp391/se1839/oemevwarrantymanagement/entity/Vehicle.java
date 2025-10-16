package com.fptu.swp391.se1839.oemevwarrantymanagement.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
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
@Table(name = "Vehicle")
public class Vehicle {

    @Id
    @Size(min = 17, max = 17, message = "VIN must be exactly 17 characters")
    @Column(length = 17, nullable = false, unique = true)
    private String vin;

    @ManyToOne(optional = false)
    @JoinColumn(name = "modelId", nullable = false)
    private Model model;

    @Min(value = 1886, message = "Product year must be >= 1886")
    @Column(nullable = false)
    private int productYear;

    @Column(nullable = false)
    @Default
    private LocalDate purchaseDate = LocalDate.now();

    @ManyToOne(optional = false)
    @JoinColumn(name = "customerId")
    private Customer customer;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CampaignVehicle> campaignVehicles = new HashSet<>();

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<VehiclePart> vehicleParts = new HashSet<>();

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<WarrantyClaim> warrantyClaims = new HashSet<>();

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Vehicle))
            return false;
        Vehicle vehicle = (Vehicle) o;
        return vin != null && vin.equals(vehicle.vin);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(vin);
    }

    @Override
    public String toString() {
        return "Vehicle [vin=" + vin + ", model=" + model + ", productYear=" + productYear + ", purchaseDate="
                + purchaseDate + ", customer=" + customer + ", campaignVehicles=" + campaignVehicles + ", vehiclePart="
                + vehicleParts.size() + ", warrantyClaim=" + warrantyClaims.size() + "]";
    }
}
