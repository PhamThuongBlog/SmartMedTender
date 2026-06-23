package com.medbid.ocr.provider;

/**
 * Record representing the result of an OCR text extraction operation.
 *
 * @param text             the extracted text content
 * @param confidence       confidence score of the extraction (0.0 to 1.0)
 * @param processingTimeMs total processing time in milliseconds
 * @param language         detected language code (e.g., "vie", "eng")
 */
public record OCRResult(
        String text,
        double confidence,
        long processingTimeMs,
        String language
) {}
