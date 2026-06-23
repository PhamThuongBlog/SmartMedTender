package com.medbid.ocr.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Azure AI Document Intelligence (Form Recognizer) OCR provider stub.
 * Activated when app.ocr.default-provider=azure.
 * Requires an Azure subscription and Document Intelligence resource configuration.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.ocr.default-provider", havingValue = "azure")
public class AzureOCRProvider implements OCRProvider {

    public AzureOCRProvider() {
        log.info("AzureOCRProvider registered (stub mode). Requires API configuration.");
    }

    @Override
    public OCRResult extractText(String filePath) {
        throw new UnsupportedOperationException(
                "Azure Document Intelligence OCR requires API configuration. " +
                "Please set up your Azure Document Intelligence endpoint and API key."
        );
    }

    @Override
    public List<OCRTableResult> extractTable(String filePath) {
        throw new UnsupportedOperationException(
                "Azure Document Intelligence table extraction requires API configuration."
        );
    }

    @Override
    public String detectLanguage(String text) {
        throw new UnsupportedOperationException(
                "Azure Document Intelligence language detection requires API configuration."
        );
    }
}
