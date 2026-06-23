package com.medbid.enterprise.dto;

import java.time.LocalDate;

public record LegalDocumentUploadRequest(
        String documentType,
        String documentName,
        LocalDate issueDate,
        LocalDate expiryDate,
        String issuingAuthority,
        String notes
) {}
