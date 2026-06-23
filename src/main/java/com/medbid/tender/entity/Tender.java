package com.medbid.tender.entity;

import com.medbid.common.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tenders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Tender extends BaseEntity {

    @NotBlank
    @Column(name = "name", length = 500, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "bid_package_code", length = 100)
    private String bidPackageCode;

    @Column(name = "procuring_entity", length = 500)
    private String procuringEntity;

    @Column(name = "submission_deadline")
    private LocalDateTime submissionDeadline;

    @Column(name = "opening_date")
    private LocalDateTime openingDate;

    @Column(name = "estimated_value", precision = 18, scale = 2)
    private BigDecimal estimatedValue;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "VND";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private TenderStatus status = TenderStatus.DRAFT;

    @Column(name = "cloned_from_id")
    private UUID clonedFromId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "tender", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TenderItem> items = new ArrayList<>();

    @Transient
    @Builder.Default
    private List<TenderRequirement> requirements = new ArrayList<>();

    @Transient
    @Builder.Default
    private List<TenderDocument> documents = new ArrayList<>();

    public void addItem(TenderItem item) {
        items.add(item);
        item.setTender(this);
    }

    public void removeItem(TenderItem item) {
        items.remove(item);
        item.setTender(null);
    }

    public void addRequirement(TenderRequirement requirement) {
        requirements.add(requirement);
        requirement.setTenderId(this.getId());
    }

    public void removeRequirement(TenderRequirement requirement) {
        requirements.remove(requirement);
    }

    public void addDocument(TenderDocument document) {
        documents.add(document);
        document.setTenderId(this.getId());
    }

    public void removeDocument(TenderDocument document) {
        documents.remove(document);
    }
}
