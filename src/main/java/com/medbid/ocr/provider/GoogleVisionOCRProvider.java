package com.medbid.ocr.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Google Vision OCR provider stub.
 * Activated when app.ocr.default-provider=google.
 * Requires a Google Cloud Vision API key and project configuration.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.ocr.default-provider", havingValue = "google")
public class GoogleVisionOCRProvider implements OCRProvider {

    public GoogleVisionOCRProvider() {
        log.info("GoogleVisionOCRProvider registered (stub mode). Requires API configuration.");
    }

    @Override
    public OCRResult extractText(String filePath) {
        throw new UnsupportedOperationException(
                "Google Vision OCR requires API configuration. " +
                "Please set up your Google Cloud Vision credentials and implement the API call."
        );
    }

    @Override
    public List<OCRTableResult> extractTable(String filePath) {
        throw new UnsupportedOperationException(
                "Google Vision OCR table extraction requires API configuration."
        );
    }

    @Override
    public String detectLanguage(String text) {
        throw new UnsupportedOperationException(
                "Google Vision OCR language detection requires API configuration."
        );
    }
}
