package com.fptu.swp391.se1839.oemevwarrantymanagement.domain;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "Part")
public class Part {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 30)
    private String partCategory;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String description;

    @OneToMany(mappedBy = "part", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PartPolicy> partPolicies = new HashSet<>();

    @OneToMany(mappedBy = "part", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PartPriceHistory> partPriceHistories = new HashSet<>();

    @OneToMany(mappedBy = "part", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PartInventory> partInventories = new HashSet<>();

    @OneToMany(mappedBy = "part", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PartClaim> partClaims = new HashSet<>();

    @OneToMany(mappedBy = "part", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RepairDetail> repairDetails = new HashSet<>();

    @OneToMany(mappedBy = "part", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VehiclePart> vehicleParts = new HashSet<>();

    @OneToMany(mappedBy = "part", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ModelPart> modelParts = new HashSet<>();

    public Part() {
    }

    public Part(
            @NotBlank(message = "Name cannot be blank") @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters") String name,
            String partCategory, String description, Set<PartPolicy> partPolicies,
            Set<PartPriceHistory> partPriceHistories, Set<PartInventory> partInventories, Set<PartClaim> partClaims,
            Set<RepairDetail> repairDetails, Set<VehiclePart> vehicleParts, Set<ModelPart> modelParts) {
        this.name = name;
        this.partCategory = partCategory;
        this.description = description;
        this.partPolicies = partPolicies != null ? partPolicies : new HashSet<>();
        this.partPriceHistories = partPriceHistories != null ? partPriceHistories : new HashSet<>();
        this.partInventories = partInventories != null ? partInventories : new HashSet<>();
        this.partClaims = partClaims != null ? partClaims : new HashSet<>();
        this.repairDetails = repairDetails != null ? repairDetails : new HashSet<>();
        this.vehicleParts = vehicleParts != null ? vehicleParts : new HashSet<>();
        this.modelParts = modelParts != null ? modelParts : new HashSet<>();
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

    public String getPartCategory() {
        return partCategory;
    }

    public void setPartCategory(String partCategory) {
        this.partCategory = partCategory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<PartPolicy> getPartPolicies() {
        return partPolicies;
    }

    public void setPartPolicies(Set<PartPolicy> partPolicies) {
        this.partPolicies = partPolicies;
    }

    public Set<PartClaim> getPartClaims() {
        return partClaims;
    }

    public void setPartClaims(Set<PartClaim> partClaims) {
        this.partClaims = partClaims;
    }

    public Set<RepairDetail> getRepairDetails() {
        return repairDetails;
    }

    public void setRepairDetails(Set<RepairDetail> repairDetails) {
        this.repairDetails = repairDetails;
    }

    public Set<PartPriceHistory> getPartPriceHistories() {
        return partPriceHistories;
    }

    public void setPartPriceHistories(Set<PartPriceHistory> partPriceHistories) {
        this.partPriceHistories = partPriceHistories;
    }

    public Set<PartInventory> getPartInventories() {
        return partInventories;
    }

    public void setPartInventories(Set<PartInventory> partInventories) {
        this.partInventories = partInventories;
    }

    public Set<VehiclePart> getVehicleParts() {
        return vehicleParts;
    }

    public void setVehicleParts(Set<VehiclePart> vehicleParts) {
        this.vehicleParts = vehicleParts;
    }

    public Set<ModelPart> getModelParts() {
        return modelParts;
    }

    public void setModelParts(Set<ModelPart> modelParts) {
        this.modelParts = modelParts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Part))
            return false;
        Part that = (Part) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Part{id=" + id + ", name='" + name + "', partCategory='" + partCategory + "', description='"
                + description + "', partPolicyCount=" + partPolicies.size() + ", priceHistoryCount="
                + partPriceHistories.size() + ", inventoryCount=" + partInventories.size() + ", partClaimCount="
                + partClaims.size() + ", repairDetailCount=" + repairDetails.size() + ", vehiclePart="
                + vehicleParts.size() + ", modelPart=" + +modelParts.size() + "}";
    }

}