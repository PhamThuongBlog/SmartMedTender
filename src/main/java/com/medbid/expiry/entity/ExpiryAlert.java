package com.medbid.expiry.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "expiry_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpiryAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "alert_type", length = 100, nullable = false)
    private String alertType;

    @Column(name = "reference_type", length = 100, nullable = false)
    private String referenceType;

    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "days_remaining", nullable = false)
    @Builder.Default
    private Integer daysRemaining = 0;

    @Column(name = "severity", length = 20, nullable = false)
    @Builder.Default
    private String severity = "INFO";

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "is_dismissed", nullable = false)
    @Builder.Default
    private Boolean isDismissed = false;

    @Column(name = "dismissed_at")
    private LocalDateTime dismissedAt;

    @Column(name = "dismissed_by")
    private UUID dismissedBy;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
