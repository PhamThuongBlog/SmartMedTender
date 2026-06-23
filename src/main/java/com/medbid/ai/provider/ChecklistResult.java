package com.medbid.ai.provider;

import java.util.List;

/**
 * Record representing a generated compliance checklist.
 *
 * @param items checklist items (actionable tasks)
 * @param notes additional notes or recommendations
 */
public record ChecklistResult(
        List<String> items,
        String notes
) {}
