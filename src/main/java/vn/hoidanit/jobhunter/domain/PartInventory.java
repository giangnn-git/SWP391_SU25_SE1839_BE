package vn.hoidanit.jobhunter.domain;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "PartInventory")
public class PartInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "partID", nullable = false)
    private Part part;

    @ManyToOne
    @JoinColumn(name = "serviceCenterId", nullable = false)
    private ServiceCenter serviceCenter;

    public PartInventory() {
    }

    public PartInventory(Part part, ServiceCenter serviceCenter) {
        this.part = Objects.requireNonNull(part, "Part cannot be null");
        this.serviceCenter = Objects.requireNonNull(serviceCenter, "ServiceCenter cannot be null");
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

    public ServiceCenter getServiceCenter() {
        return serviceCenter;
    }

    public void setServiceCenter(ServiceCenter serviceCenter) {
        this.serviceCenter = serviceCenter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PartInventory))
            return false;
        PartInventory that = (PartInventory) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "PartInventory{" + "id=" + id + ", partId=" + (part != null ? part.getId() : null) + ", serviceCenterId="
                + (serviceCenter != null ? serviceCenter.getId() : null) + '}';
    }
}
