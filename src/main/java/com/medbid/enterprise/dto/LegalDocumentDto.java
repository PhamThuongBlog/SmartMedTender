package com.medbid.enterprise.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record LegalDocumentDto(
        UUID id,
        UUID enterpriseId,
        String documentType,
        String documentName,
        String filePath,
        String fileName,
        Long fileSize,
        LocalDate issueDate,
        LocalDate expiryDate,
        String issuingAuthority,
        String status,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
