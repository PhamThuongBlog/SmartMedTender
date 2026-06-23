package com.medbid.ai.provider;

/**
 * Strategy interface for AI/LLM providers.
 * Each implementation handles structured extraction and comparison
 * from tender document text using a specific AI model.
 */
public interface AIProvider {

    /**
     * Extract structured requirements from raw tender document text.
     *
     * @param text    the raw text from OCR or document parsing
     * @param context additional context (e.g., document type, section heading)
     * @return ExtractionResult containing parsed requirements and metadata
     */
    ExtractionResult extractRequirements(String text, String context);

    /**
     * Compare product technical specifications against tender requirements.
     *
     * @param productSpecs technical specifications of the product (JSON or structured text)
     * @param tenderSpecs  technical requirements from the tender (JSON or structured text)
     * @return ComparisonResult indicating pass/fail, score, missing criteria, and recommendations
     */
    ComparisonResult compareTechnicalSpecs(String productSpecs, String tenderSpecs);

    /**
     * Generate a compliance checklist from extracted tender requirements.
     *
     * @param tenderRequirements the extracted tender requirements as structured text
     * @return ChecklistResult with actionable checklist items
     */
    ChecklistResult generateChecklist(String tenderRequirements);
}
