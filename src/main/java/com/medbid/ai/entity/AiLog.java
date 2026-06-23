package com.medbid.ai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(length = 100)
    private String model;

    @Column(nullable = false, length = 100)
    private String requestType;

    @Column(columnDefinition = "TEXT")
    private String requestPrompt;

    @Column(columnDefinition = "TEXT")
    private String responseText;

    @Column
    private Integer tokensUsed;

    @Column
    private Long latencyMs;

    @Column(precision = 10, scale = 6)
    private BigDecimal cost;

    @Column(nullable = false)
    @Builder.Default
    private Boolean success = true;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
