package com.medbid.product.entity;

import com.medbid.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Product extends BaseEntity {

    @Column(name = "name", length = 500, nullable = false)
    private String name;

    @Column(name = "manufacturer", length = 255)
    private String manufacturer;

    @Column(name = "brand", length = 255)
    private String brand;

    @Column(name = "model", length = 255)
    private String model;

    @Column(name = "origin_country", length = 100)
    private String originCountry;

    @Column(name = "category", length = 255)
    private String category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "technical_specs", columnDefinition = "jsonb")
    private Map<String, Object> technicalSpecs;

    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Column(name = "registration_issue_date")
    private LocalDate registrationIssueDate;

    @Column(name = "registration_expiry_date")
    private LocalDate registrationExpiryDate;

    @Column(name = "has_iso", nullable = false)
    @Builder.Default
    private Boolean hasIso = false;

    @Column(name = "has_fda", nullable = false)
    @Builder.Default
    private Boolean hasFda = false;

    @Column(name = "has_ce", nullable = false)
    @Builder.Default
    private Boolean hasCe = false;

    @Column(name = "has_co_cq", nullable = false)
    @Builder.Default
    private Boolean hasCoCq = false;

    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductDocument> productDocuments = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> productImages = new ArrayList<>();

    public void addDocument(ProductDocument document) {
        productDocuments.add(document);
        document.setProduct(this);
    }

    public void removeDocument(ProductDocument document) {
        productDocuments.remove(document);
        document.setProduct(null);
    }

    public void addImage(ProductImage image) {
        productImages.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image) {
        productImages.remove(image);
        image.setProduct(null);
    }

    @PrePersist
    public void prePersist() {
        if (hasIso == null) hasIso = false;
        if (hasFda == null) hasFda = false;
        if (hasCe == null) hasCe = false;
        if (hasCoCq == null) hasCoCq = false;
        if (status == null) status = "ACTIVE";
        if (getDeleted() == null) setDeleted(false);
    }
}
