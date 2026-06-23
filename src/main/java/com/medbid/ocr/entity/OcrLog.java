package com.medbid.ocr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ocr_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OcrLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(length = 1000)
    private String sourceFile;

    @Column(columnDefinition = "TEXT")
    private String resultText;

    @Column
    private Double confidence;

    @Column
    private Long processingTimeMs;

    @Column(nullable = false)
    @Builder.Default
    private Boolean success = true;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
