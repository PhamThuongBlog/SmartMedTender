package com.medbid.product.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDocumentUploadRequest {
    private UUID productId;
    private String documentType;
    private String documentName;
    private String issuingAuthority;
    private LocalDate issueDate;
    private LocalDate expiryDate;
}
