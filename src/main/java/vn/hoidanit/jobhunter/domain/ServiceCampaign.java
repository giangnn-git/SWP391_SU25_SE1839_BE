package vn.hoidanit.jobhunter.domain;

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

    private LocalDate startDate = LocalDate.now();

    private LocalDate endDate;

    @OneToMany(mappedBy = "serviceCampaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CampaignVehicle> campaignVehicles = new HashSet<>();

    @OneToMany(mappedBy = "serviceCampaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WarrantyClaim> warrantyClaims = new HashSet<>();

    @AssertTrue(message = "End date must be after start date")
    public boolean isEndDateValid() {
        return endDate == null || endDate.isAfter(startDate);
    }

    public ServiceCampaign() {
    }

    public ServiceCampaign(String name, String description, LocalDate startDate, LocalDate endDate,
            Set<CampaignVehicle> campaignVehicles, Set<WarrantyClaim> warrantyClaims) {
        this.name = name;
        this.description = description;
        this.startDate = startDate != null ? startDate : LocalDate.now();
        this.endDate = endDate;
        this.campaignVehicles = campaignVehicles != null ? campaignVehicles : new HashSet<>();
        this.warrantyClaims = warrantyClaims != null ? warrantyClaims : new HashSet<>();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Set<CampaignVehicle> getCampaignVehicles() {
        return campaignVehicles;
    }

    public void setCampaignVehicles(Set<CampaignVehicle> campaignVehicles) {
        this.campaignVehicles = campaignVehicles;
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
