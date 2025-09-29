package com.fptu.swp391.se1839.oemevwarrantymanagement.domain;


import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fptu.swp391.se1839.oemevwarrantymanagement.domain.PartClaim.ClaimStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "WarrantyClaim")
public class WarrantyClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vin", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(optional = true)
    @JoinColumn(name = "serviceCenterId", nullable = true)
    private ServiceCenter serviceCenter;

    @NotNull
    @Column(nullable = false)
    private LocalDate claimDate;

    @NotNull
    @Column(nullable = false)
    private int mileage;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotNull
    private String description;

    @Column(nullable = true)
    private LocalDate decisionDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClaimStatus status = ClaimStatus.PENDING;

    public enum ClaimPriority {
        NORMAL, HIGH, URGENT
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClaimPriority priority = ClaimPriority.NORMAL;

    // One claim can have many attachments
    @OneToMany(mappedBy = "warrantyClaim", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ClaimAttachment> claimAttachments = new HashSet<>();

    @OneToMany(mappedBy = "warrantyClaim", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RepairOrder> repairOrders = new HashSet<>();

    @OneToMany(mappedBy = "warrantyClaim", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VehiclePart> vehicleParts = new HashSet<>();

    @OneToMany(mappedBy = "warrantyClaim", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PartClaim> partClaims = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "serviceCampaignId")
    private ServiceCampaign serviceCampaign;

    public WarrantyClaim() {
    }

    public WarrantyClaim(Long claimID, Vehicle vehicle, ServiceCenter serviceCenter, @NotNull LocalDate claimDate,
            int mileage, @NotNull String description, LocalDate decisionDate, @NotNull ClaimStatus status,
            Set<ClaimAttachment> claimAttachments, Set<RepairOrder> repairOrders, Set<VehiclePart> vehicleParts,
            ServiceCampaign serviceCampaign, Set<PartClaim> partClaims, ClaimPriority priority) {
        this.id = claimID;
        this.vehicle = Objects.requireNonNull(vehicle, "Vehicle cannot be null");
        this.serviceCenter = serviceCenter;
        this.claimDate = claimDate != null ? claimDate : LocalDate.now();
        this.mileage = mileage;
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.decisionDate = decisionDate;
        this.status = status != null ? status : ClaimStatus.PENDING;
        this.claimAttachments = claimAttachments != null ? claimAttachments : new HashSet<>();
        this.repairOrders = repairOrders != null ? repairOrders : new HashSet<>();
        this.vehicleParts = vehicleParts != null ? vehicleParts : new HashSet<>();
        this.serviceCampaign = serviceCampaign;
        this.partClaims = partClaims != null ? partClaims : new HashSet<>();
        this.priority = priority;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public ServiceCenter getServiceCenter() {
        return serviceCenter;
    }

    public void setServiceCenter(ServiceCenter serviceCenter) {
        this.serviceCenter = serviceCenter;
    }

    public LocalDate getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(LocalDate claimDate) {
        this.claimDate = claimDate;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(LocalDate decisionDate) {
        this.decisionDate = decisionDate;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public Set<RepairOrder> getRepairOrders() {
        return repairOrders;
    }

    public void setRepairOrders(Set<RepairOrder> repairOrders) {
        this.repairOrders = repairOrders;
    }

    public Set<VehiclePart> getVehicleParts() {
        return vehicleParts;
    }

    public void setVehicleParts(Set<VehiclePart> vehicleParts) {
        this.vehicleParts = vehicleParts;
    }

    public ServiceCampaign getServiceCampaign() {
        return serviceCampaign;
    }

    public void setServiceCampaign(ServiceCampaign serviceCampaign) {
        this.serviceCampaign = serviceCampaign;
    }

    public Set<ClaimAttachment> getClaimAttachments() {
        return claimAttachments;
    }

    public void setClaimAttachments(Set<ClaimAttachment> claimAttachments) {
        this.claimAttachments = claimAttachments;
    }

    public Set<PartClaim> getPartClaims() {
        return partClaims;
    }

    public void setPartClaims(Set<PartClaim> partClaims) {
        this.partClaims = partClaims;
    }

    public ClaimPriority getPriority() {
        return priority;
    }

    public void setPriority(ClaimPriority priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WarrantyClaim))
            return false;
        WarrantyClaim that = (WarrantyClaim) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "WarrantyClaim{" + "id=" + id + ", vehicleVIN=" + (vehicle == null ? "null" : vehicle.getVin())
                + ", serviceCenterId=" + (serviceCenter == null ? "null" : serviceCenter.getId()) + ", claimDate="
                + claimDate + ", mileage=" + mileage + ", description="
                + (description == null ? "null" : "'" + description + "'") + ", decisionDate=" + decisionDate
                + ", status=" + status + ", priority=" + priority + ", claimAttachmentCount="
                + (claimAttachments == null ? 0 : claimAttachments.size()) + ", repairOrderCount="
                + (repairOrders == null ? 0 : repairOrders.size()) + ", vehiclePartCount="
                + (vehicleParts == null ? 0 : vehicleParts.size()) + ", serviceCampaignId="
                + (serviceCampaign == null ? "null" : serviceCampaign.getId()) + ", partClaimCount="
                + (partClaims == null ? 0 : partClaims.size()) + '}';
    }
}
