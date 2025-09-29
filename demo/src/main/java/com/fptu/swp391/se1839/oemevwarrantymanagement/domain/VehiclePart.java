package com.fptu.swp391.se1839.oemevwarrantymanagement.domain;


import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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

    // Constructors
    public VehiclePart() {
    }

    public VehiclePart(String serialNumber, Vehicle vehicle, Part part, WarrantyClaim warrantyClaim,
            LocalDate installationDate, LocalDate removalDate) {
        this.serialNumber = Objects.requireNonNull(serialNumber, "SerialNumber cannot be null");
        this.vehicle = Objects.requireNonNull(vehicle, "Vehicle cannot be null");
        this.part = Objects.requireNonNull(part, "Part cannot be null");
        this.warrantyClaim = Objects.requireNonNull(warrantyClaim, "WarrantyClaim cannot be null");
        this.installationDate = installationDate;
        this.removalDate = removalDate;
    }

    // Getters & Setters
    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
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

    public LocalDate getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(LocalDate installationDate) {
        this.installationDate = installationDate;
    }

    public LocalDate getRemovalDate() {
        return removalDate;
    }

    public void setRemovalDate(LocalDate removalDate) {
        this.removalDate = removalDate;
    }

    // equals & hashCode (dựa vào serialNumber)
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

