package com.medbid.ai.provider;

/**
 * Record representing the result of comparing product specs against tender requirements.
 *
 * @param passed          whether the product passes all mandatory requirements
 * @param score           overall compliance score (0.0 to 1.0)
 * @param missingCriteria description of criteria that were not met
 * @param recommendation  AI-generated recommendation or suggested alternative
 */
public record ComparisonResult(
        boolean passed,
        double score,
        String missingCriteria,
        String recommendation
) {}
