package com.medbid.product.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDocumentDto {
    private UUID id;
    private UUID productId;
    private String productName;
    private String documentType;
    private String documentName;
    private String filePath;
    private String fileName;
    private Long fileSize;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String issuingAuthority;
    private String notes;
    private String status;
}
