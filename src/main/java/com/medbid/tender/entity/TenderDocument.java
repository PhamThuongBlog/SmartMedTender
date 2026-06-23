package com.medbid.tender.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tender_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenderDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tender_id", nullable = false)
    private UUID tenderId;

    @Column(name = "document_type", length = 100, nullable = false)
    private String documentType;

    @Column(name = "file_path", length = 1000, nullable = false)
    private String filePath;

    @Column(name = "file_name", length = 500)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "ocr_status", length = 50)
    @Builder.Default
    private String ocrStatus = "PENDING";

    @Column(name = "uploaded_at", nullable = false)
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
