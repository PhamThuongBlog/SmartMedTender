package com.medbid.matching;

import java.util.List;
import java.util.UUID;

public record MatchResponse(
        UUID tenderId,
        UUID productId,
        String productName,
        int totalRequirements,
        int passed,
        int failed,
        double overallScore,
        List<MatchDetail> details
) {}
