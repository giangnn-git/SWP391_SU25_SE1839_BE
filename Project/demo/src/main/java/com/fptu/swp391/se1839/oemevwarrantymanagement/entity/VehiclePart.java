package com.fptu.swp391.se1839.oemevwarrantymanagement.entity;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Entity;
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
@Table(name = "VehiclePart")
public class VehiclePart {

    @Id
    private String serialNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vehicleVin", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(optional = false)
    @JoinColumn(name = "partId", nullable = false)
    private Part part;

    @ManyToOne(optional = false)
    @JoinColumn(name = "claimId", nullable = false)
    private WarrantyClaim warrantyClaim;

    private LocalDate installationDate;

    private LocalDate removalDate;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof VehiclePart))
            return false;
        VehiclePart that = (VehiclePart) o;
        return serialNumber != null && serialNumber.equals(that.serialNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(serialNumber);
    }

    // toString
    @Override
    public String toString() {
        return "VehiclePart{" + "serialNumber='" + serialNumber + '\'' + ", vehicleVin="
                + (vehicle != null ? vehicle.getVin() : null) + ", partId=" + (part != null ? part.getId() : null)
                + ", claimId=" + (warrantyClaim != null ? warrantyClaim.getId() : null) + ", installationDate="
                + installationDate + ", removalDate=" + removalDate + '}';
    }
}
