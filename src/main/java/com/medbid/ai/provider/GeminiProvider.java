package com.medbid.ai.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Google Gemini AI provider stub.
 * Activated when app.ai.default-provider=gemini.
 * Requires a Google AI API key. Structure is ready for implementation.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.ai.default-provider", havingValue = "gemini")
public class GeminiProvider implements AIProvider {

    private final String apiKey;
    private final String model;
    private final boolean apiConfigured;

    public GeminiProvider(
            @Value("${app.ai.gemini.api-key:}") String apiKey,
            @Value("${app.ai.gemini.model:gemini-2.0-flash}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.apiConfigured = apiKey != null && !apiKey.isBlank();

        if (apiConfigured) {
            log.info("GeminiProvider initialized with model: {}", model);
        } else {
            log.warn("Gemini API key not configured. Provider is in stub mode.");
        }
    }

    @Override
    public ExtractionResult extractRequirements(String text, String context) {
        if (!apiConfigured) {
            throw new UnsupportedOperationException(
                    "Gemini AI requires API configuration. " +
                    "Please set the GEMINI_API_KEY environment variable or configure app.ai.gemini.api-key."
            );
        }
        // TODO: Implement Gemini API call via RestClient/WebClient
        // POST https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent
        // See: https://ai.google.dev/gemini-api/docs
        throw new UnsupportedOperationException("Gemini extractRequirements - implementation pending.");
    }

    @Override
    public ComparisonResult compareTechnicalSpecs(String productSpecs, String tenderSpecs) {
        if (!apiConfigured) {
            throw new UnsupportedOperationException("Gemini AI requires API configuration.");
        }
        throw new UnsupportedOperationException("Gemini compareTechnicalSpecs - implementation pending.");
    }

    @Override
    public ChecklistResult generateChecklist(String tenderRequirements) {
        if (!apiConfigured) {
            throw new UnsupportedOperationException("Gemini AI requires API configuration.");
        }
        throw new UnsupportedOperationException("Gemini generateChecklist - implementation pending.");
    }
}
