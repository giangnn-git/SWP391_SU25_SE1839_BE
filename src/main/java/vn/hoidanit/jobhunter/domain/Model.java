package vn.hoidanit.jobhunter.domain;

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

@Entity
@Table(name = "Model")
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL)
    private Set<Vehicle> vehicles = new HashSet<>();

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL)
    private Set<ModelPart> modelParts = new HashSet<>();

    public Model() {
    }

    public Model(String name, Set<Vehicle> vehicles, Set<ModelPart> modelParts) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.vehicles = vehicles != null ? vehicles : new HashSet<>();
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

    public Set<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(Set<Vehicle> vehicles) {
        this.vehicles = vehicles;
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
        if (!(o instanceof Model))
            return false;
        Model that = (Model) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "Model{id=" + id + ", name='" + name + "', vehicleCount=" + vehicles.size() + "', modelPart="
                + modelParts.size() + "}";
    }
}
