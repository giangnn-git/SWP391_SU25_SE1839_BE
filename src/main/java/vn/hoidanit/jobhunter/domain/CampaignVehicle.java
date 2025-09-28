package vn.hoidanit.jobhunter.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "CampaignVehicle", uniqueConstraints = @UniqueConstraint(columnNames = { "campaignID", "vin" }))
public class CampaignVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "campaignID", nullable = false)
    private ServiceCampaign serviceCampaign;

    @ManyToOne
    @JoinColumn(name = "vin", nullable = false)
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignVehicleStatus status = CampaignVehicleStatus.NOTIFIED;

    public enum CampaignVehicleStatus {
        NOTIFIED, COMPLETED
    }

    public CampaignVehicle() {
    }

    public CampaignVehicle(ServiceCampaign serviceCampaign, Vehicle vehicle, CampaignVehicleStatus status) {
        this.serviceCampaign = serviceCampaign;
        this.vehicle = vehicle;
        this.status = status != null ? status : CampaignVehicleStatus.NOTIFIED;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ServiceCampaign getServiceCampaign() {
        return serviceCampaign;
    }

    public void setServiceCampaign(ServiceCampaign serviceCampaign) {
        this.serviceCampaign = serviceCampaign;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public CampaignVehicleStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignVehicleStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CampaignVehicle))
            return false;
        CampaignVehicle that = (CampaignVehicle) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "CampaignVehicle [id=" + id + ", serviceCampaign=" + serviceCampaign + ", vehicle=" + vehicle
                + ", status=" + status + "]";
    }
}
