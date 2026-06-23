package com.medbid.matching.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "match_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tender_id", nullable = false)
    private UUID tenderId;

    @Column(name = "tender_requirement_id", nullable = false)
    private UUID tenderRequirementId;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "passed", nullable = false)
    @Builder.Default
    private Boolean passed = false;

    @Column(name = "missing_criteria", columnDefinition = "TEXT")
    private String missingCriteria;

    @Column(name = "score")
    private Double score;

    @Column(name = "is_manual_override", nullable = false)
    @Builder.Default
    private Boolean isManualOverride = false;

    @Column(name = "override_reason", columnDefinition = "TEXT")
    private String overrideReason;

    @Column(name = "override_by")
    private UUID overrideBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
