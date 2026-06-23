package com.medbid.ai.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Anthropic Claude AI provider stub.
 * Activated when app.ai.default-provider=claude.
 * Requires an Anthropic API key. Structure is ready for implementation.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.ai.default-provider", havingValue = "claude")
public class ClaudeProvider implements AIProvider {

    private final String apiKey;
    private final String model;
    private final boolean apiConfigured;

    public ClaudeProvider(
            @Value("${app.ai.claude.api-key:}") String apiKey,
            @Value("${app.ai.claude.model:claude-sonnet-4-20250514}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.apiConfigured = apiKey != null && !apiKey.isBlank();

        if (apiConfigured) {
            log.info("ClaudeProvider initialized with model: {}", model);
        } else {
            log.warn("Claude API key not configured. Provider is in stub mode.");
        }
    }

    @Override
    public ExtractionResult extractRequirements(String text, String context) {
        if (!apiConfigured) {
            throw new UnsupportedOperationException(
                    "Claude AI requires API configuration. " +
                    "Please set the CLAUDE_API_KEY environment variable or configure app.ai.claude.api-key."
            );
        }
        // TODO: Implement Anthropic Messages API call via RestClient
        // POST https://api.anthropic.com/v1/messages
        // Header: x-api-key, anthropic-version: 2023-06-01
        // See: https://docs.anthropic.com/en/api
        throw new UnsupportedOperationException("Claude extractRequirements - implementation pending.");
    }

    @Override
    public ComparisonResult compareTechnicalSpecs(String productSpecs, String tenderSpecs) {
        if (!apiConfigured) {
            throw new UnsupportedOperationException("Claude AI requires API configuration.");
        }
        throw new UnsupportedOperationException("Claude compareTechnicalSpecs - implementation pending.");
    }

    @Override
    public ChecklistResult generateChecklist(String tenderRequirements) {
        if (!apiConfigured) {
            throw new UnsupportedOperationException("Claude AI requires API configuration.");
        }
        throw new UnsupportedOperationException("Claude generateChecklist - implementation pending.");
    }
}
