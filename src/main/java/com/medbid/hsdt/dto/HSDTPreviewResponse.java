package com.medbid.hsdt.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HSDTPreviewResponse {
    // Tender info
    private UUID tenderId;
    private String tenderName;
    private String bidPackageCode;
    private String procuringEntity;
    private LocalDateTime submissionDeadline;
    private BigDecimal estimatedValue;
    private String currency;

    // Enterprise info
    private String companyName;
    private String taxCode;
    private String companyAddress;
    private String legalRepresentative;

    // Products + matching
    private List<ProductEntry> products;

    // Summary
    private int totalProducts;
    private BigDecimal totalPrice;
    private int completeProducts;    // products passing all mandatory requirements
    private int incompleteProducts;  // products with missing criteria/docs

    // Smart checklist
    private List<ChecklistItem> checklist;

    // Generated at
    private LocalDateTime generatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductEntry {
        private UUID productId;
        private String productName;
        private String manufacturer;
        private String brand;
        private String model;
        private String originCountry;
        private int itemNumber;

        // Matching
        private double matchScore;
        private int passedRequirements;
        private int totalRequirements;
        private boolean allMandatoryPassed;
        private List<String> failedMandatoryReqs;

        // Documents
        private List<DocStatus> documents;

        // Price
        private BigDecimal suggestedPrice;
        private BigDecimal importPrice;
        private BigDecimal sellingPrice;
        private BigDecimal lastWinningPrice;
        private String priceConfidence;
        private int priceDataPoints;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocStatus {
        private String docType;       // CO, CQ, ISO_13485, CE, FDA, CATALOGUE
        private String docName;
        private boolean available;
        private boolean expired;
        private String expiryDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChecklistItem {
        private String section;       // HANH_CHINH, KY_THUAT, CHUNG_CHI, TAI_CHINH, KHAC
        private String item;
        private String status;        // OK, MISSING, EXPIRED, WARNING
        private String detail;
        private boolean mandatory;
    }
}
