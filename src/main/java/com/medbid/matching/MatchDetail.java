package com.medbid.matching;

import java.util.UUID;

public record MatchDetail(
        UUID matchResultId,
        UUID requirementId,
        String requirement,
        String type,
        String operator,
        String requiredValue,
        String unit,
        String actualValue,
        String status,
        double score,
        String notes,
        Boolean isManualOverride,
        String overrideReason
) {}
