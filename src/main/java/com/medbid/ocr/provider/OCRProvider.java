package com.medbid.ocr.provider;

import java.util.List;

/**
 * Strategy interface for OCR providers.
 * Implementations handle text extraction from documents using various OCR engines.
 */
public interface OCRProvider {

    /**
     * Extract full text from a document file.
     *
     * @param filePath absolute path to the document file
     * @return OCRResult containing extracted text, confidence score, processing time, and detected language
     */
    OCRResult extractText(String filePath);

    /**
     * Extract structured table data from a document file.
     *
     * @param filePath absolute path to the document file
     * @return list of extracted table results with headers and rows
     */
    List<OCRTableResult> extractTable(String filePath);

    /**
     * Detect the primary language of the given text.
     *
     * @param text the text to analyze
     * @return language code (e.g., "vie", "eng")
     */
    String detectLanguage(String text);
}
