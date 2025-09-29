package com.fptu.swp391.se1839.oemevwarrantymanagement.domain;


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
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

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
    private LocalDate purchaseDate = LocalDate.now();

    @ManyToOne(optional = false)
    @JoinColumn(name = "customerId", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CampaignVehicle> campaignVehicles = new HashSet<>();

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VehiclePart> vehicleParts = new HashSet<>();

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WarrantyClaim> warrantyClaims = new HashSet<>();

    public Vehicle() {
    }

    public Vehicle(@Size(min = 17, max = 17, message = "VIN must be exactly 17 characters") String vin, Model model,
            @Min(value = 1886, message = "Product year must be >= 1886") int productYear,
            @PastOrPresent(message = "Purchase date cannot be in the future") LocalDate purchaseDate, Customer customer,
            Set<CampaignVehicle> campaignVehicles, Set<VehiclePart> vehicleParts, Set<WarrantyClaim> warrantyClaims) {
        super();
        this.vin = vin;
        this.model = model;
        this.productYear = productYear;
        this.purchaseDate = purchaseDate != null ? purchaseDate : LocalDate.now();
        this.customer = customer;
        this.campaignVehicles = campaignVehicles != null ? campaignVehicles : new HashSet<>();
        this.vehicleParts = vehicleParts != null ? vehicleParts : new HashSet<>();
        this.warrantyClaims = warrantyClaims != null ? warrantyClaims : new HashSet<>();
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public int getProductYear() {
        return productYear;
    }

    public void setProductYear(int productYear) {
        this.productYear = productYear;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Set<CampaignVehicle> getCampaignVehicles() {
        return campaignVehicles;
    }

    public void setCampaignVehicles(Set<CampaignVehicle> campaignVehicles) {
        this.campaignVehicles = campaignVehicles;
    }

    public Set<VehiclePart> getVehicleParts() {
        return vehicleParts;
    }

    public void setVehicleParts(Set<VehiclePart> vehicleParts) {
        this.vehicleParts = vehicleParts;
    }

    public Set<WarrantyClaim> getWarrantyClaims() {
        return warrantyClaims;
    }

    public void setWarrantyClaims(Set<WarrantyClaim> warrantyClaims) {
        this.warrantyClaims = warrantyClaims;
    }

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

