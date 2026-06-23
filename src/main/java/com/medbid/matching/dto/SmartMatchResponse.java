package com.medbid.matching.dto;

import com.medbid.matching.MatchDetail;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Extended match response with compliance, price, and gap information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmartMatchResponse {
    private UUID tenderId;
    private UUID productId;
    private String productName;
    private String productManufacturer;
    private String productCategory;
    private int totalRequirements;
    private int passed;
    private int failed;
    private double overallScore;
    private List<MatchDetail> details;

    // Document compliance summary
    private boolean hasIso;
    private boolean hasCe;
    private boolean hasFda;
    private boolean hasCoCq;
    private List<String> missingDocuments;
    private List<String> expiredDocuments;

    // Price suggestion
    private BigDecimal suggestedPrice;
    private BigDecimal lastWinningPrice;
    private String priceConfidence;
    private int priceDataPoints;

    // Gap warnings
    private List<String> gapWarnings;
}
