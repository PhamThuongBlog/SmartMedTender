package com.medbid.tender.entity;

import com.medbid.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "tender_requirements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TenderRequirement extends BaseEntity {

    @Column(name = "tender_id", nullable = false)
    private UUID tenderId;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "type", length = 100)
    private String type;

    @Column(name = "operator", length = 20)
    private String operator;

    @Column(name = "value", length = 500)
    private String value;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "mandatory", nullable = false)
    @Builder.Default
    private Boolean mandatory = true;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 3;

    @Column(name = "source", length = 50, nullable = false)
    @Builder.Default
    private String source = "MANUAL";

    @Column(name = "source_document_id")
    private UUID sourceDocumentId;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private String status = "EXTRACTED";
}
