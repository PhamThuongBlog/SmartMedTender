package com.medbid.matching.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GapAnalysisResponse {
    private UUID tenderId;
    private UUID productId;
    private String productName;
    private String tenderName;

    // Tổng quan
    private int totalRequirements;
    private int passedRequirements;
    private int failedRequirements;
    private double overallScore;

    // Danh sách tiêu chí không đạt
    private List<GapItem> missingCriteria;

    // Danh sách tài liệu/chứng chỉ còn thiếu
    private List<GapItem> missingDocuments;

    // Danh sách chứng chỉ sắp hoặc đã hết hạn
    private List<GapItem> expiredCertificates;

    // Khuyến nghị hành động
    private List<String> recommendedActions;

    // Gợi ý giá
    private PriceSuggestion priceSuggestion;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GapItem {
        private String category;       // TECHNICAL, CERTIFICATION, DOCUMENT, EXPERIENCE...
        private String description;
        private String severity;       // CRITICAL, WARNING, INFO
        private String currentStatus;  // MISSING, EXPIRED, INSUFFICIENT
        private String recommendation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PriceSuggestion {
        private BigDecimal suggestedPrice;
        private BigDecimal lastWinningPrice;
        private BigDecimal averagePrice;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private String confidence;     // CAO, TRUNG BINH, THAP, RAT THAP
        private int dataPoints;
        private String currency;
    }
}
