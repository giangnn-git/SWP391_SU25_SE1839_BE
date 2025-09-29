package com.fptu.swp391.se1839.oemevwarrantymanagement.domain;

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

@Entity
@Table(name = "ModelPart")
public class ModelPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "modelId", nullable = false)
    private Model model;

    @ManyToOne
    @JoinColumn(name = "partId", nullable = false)
    private Part part;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModelPartStatus isMandatory = ModelPartStatus.NOTIFIED;

    public enum ModelPartStatus {
        NOTIFIED, COMPLETED
    }

    // Constructors
    public ModelPart() {
    }

    public ModelPart(Model model, Part part, ModelPartStatus isMandatory) {
        this.model = Objects.requireNonNull(model, "Model cannot be null");
        this.part = Objects.requireNonNull(part, "Part cannot be null");
        this.isMandatory = isMandatory != null ? isMandatory : ModelPartStatus.NOTIFIED;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public ModelPartStatus getIsMandatory() {
        return isMandatory;
    }

    public void setIsMandatory(ModelPartStatus isMandatory) {
        this.isMandatory = isMandatory;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ModelPart))
            return false;
        ModelPart that = (ModelPart) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // toString
    @Override
    public String toString() {
        return "ModelPart{" + "id=" + id + ", modelId=" + (model != null ? model.getId() : null) + ", partId="
                + (part != null ? part.getId() : null) + ", isMandatory=" + isMandatory + '}';
    }
}
