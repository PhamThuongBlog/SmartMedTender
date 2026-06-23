package com.medbid.ai.provider;

import java.util.List;

/**
 * Record representing the result of an AI requirement extraction operation.
 *
 * @param requirements list of extracted requirements
 * @param model        the AI model used for extraction (e.g., "gpt-4o", "gemini-2.0-flash")
 * @param tokensUsed   number of tokens consumed by the AI call
 * @param latencyMs    total latency of the AI call in milliseconds
 */
public record ExtractionResult(
        List<ExtractedRequirement> requirements,
        String model,
        int tokensUsed,
        long latencyMs
) {}
