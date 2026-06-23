package com.medbid.ocr.provider;

import java.util.List;

/**
 * Record representing the result of table extraction from a document.
 *
 * @param headers the column headers of the extracted table
 * @param rows    the data rows, where each row is a list of cell values
 */
public record OCRTableResult(
        List<String> headers,
        List<List<String>> rows
) {}
