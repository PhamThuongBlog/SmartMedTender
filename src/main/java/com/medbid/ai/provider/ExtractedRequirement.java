package com.medbid.ai.provider;

/**
 * Record representing a single extracted requirement from a tender document.
 *
 * @param description     the requirement description (e.g., "CPU tối thiểu Intel Core i5")
 * @param type           the requirement type (e.g., "TECHNICAL", "LEGAL", "FINANCIAL")
 * @param operator       the comparison operator (e.g., ">=", "=", "<=")
 * @param value          the target value (e.g., "3.0", "Có")
 * @param unit           the unit of measurement (e.g., "GHz", "GB", "năm")
 * @param mandatory      whether this requirement is mandatory
 * @param priority       priority level (1=highest, 5=lowest)
 * @param confidenceScore confidence score of the extraction (0.0 to 1.0)
 */
public record ExtractedRequirement(
        String description,
        String type,
        String operator,
        String value,
        String unit,
        boolean mandatory,
        int priority,
        double confidenceScore
) {}
