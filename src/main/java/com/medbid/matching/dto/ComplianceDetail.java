package com.medbid.matching.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceDetail {
    private UUID requirementId;
    private String requirement;
    private String type;           // CERTIFICATION, TECHNICAL, EXPERIENCE
    private boolean compliant;
    private String status;         // OK, MISSING_DOC, EXPIRED_DOC, INSUFFICIENT
    private List<String> missingDocuments;
    private List<String> expiredDocuments;
    private double score;
    private String notes;
}
