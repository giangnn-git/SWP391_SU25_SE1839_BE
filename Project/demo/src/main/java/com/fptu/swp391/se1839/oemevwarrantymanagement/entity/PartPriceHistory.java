package com.fptu.swp391.se1839.oemevwarrantymanagement.entity;

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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
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
    @Builder.Default
    private LocalDate startDate = LocalDate.now();

    @Column
    private LocalDate endDate;

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
