package com.medbid.export.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "export_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tender_id", nullable = false)
    private UUID tenderId;

    @Column(name = "export_type", length = 50, nullable = false)
    private String exportType;

    @Column(name = "file_format", length = 20, nullable = false)
    private String fileFormat;

    @Column(name = "file_path", length = 1000)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private String status = "PROCESSING";

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
