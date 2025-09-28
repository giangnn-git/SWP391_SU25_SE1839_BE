package vn.hoidanit.jobhunter.domain;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "PartPriceHistory")
public class PartPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) // rõ nghĩa hơn
    @JoinColumn(name = "partID", nullable = false)
    private Part part;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    public PartPriceHistory() {
    }

    public PartPriceHistory(Part part, Double price, LocalDate startDate, LocalDate endDate) {
        this.part = Objects.requireNonNull(part, "Part cannot be null");
        this.price = Objects.requireNonNull(price, "Price cannot be null");
        this.startDate = startDate != null ? startDate : LocalDate.now();
        this.endDate = endDate;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PartPriceHistory))
            return false;
        PartPriceHistory that = (PartPriceHistory) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "PartPriceHistory{partPriceID=" + id + ", partId=" + (part != null ? part.getId() : null) + ", price="
                + price + ", startDate=" + startDate + ", endDate=" + endDate + "}";
    }
}
