package com.medbid.ai.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory for selecting and managing AI providers.
 * Uses Strategy pattern: all AIProvider beans are injected,
 * and the active provider is selected based on configuration.
 * Falls back to OpenAIProvider (rule-based) when the configured provider is unavailable.
 */
@Slf4j
@Component
public class AIProviderFactory {

    private final Map<String, AIProvider> providerMap;
    private final String defaultProviderName;
    private final AIProvider fallbackProvider;

    public AIProviderFactory(
            List<AIProvider> providers,
            @Value("${app.ai.default-provider:openai}") String defaultProviderName) {
        this.defaultProviderName = defaultProviderName;
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(
                        p -> p.getClass().getSimpleName().replace("Provider", "").toLowerCase(),
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        // OpenAI is the primary fallback (it has rule-based fallback built-in)
        this.fallbackProvider = providers.stream()
                .filter(p -> p instanceof OpenAIProvider)
                .findFirst()
                .orElseGet(() -> providers.stream().findFirst().orElse(null));

        log.info("AIProviderFactory initialized with providers: {}", providerMap.keySet());
    }

    /**
     * Get a specific AI provider by name (e.g., "openai", "gemini", "claude").
     * Falls back if the requested provider is unavailable.
     *
     * @param name provider name (case-insensitive)
     * @return the matching AIProvider, or a fallback
     */
    public AIProvider getProvider(String name) {
        AIProvider provider = providerMap.get(name.toLowerCase());
        if (provider != null) {
            if (isStubProvider(provider)) {
                log.warn("AI provider '{}' is a stub (not configured). Falling back to available provider.", name);
                return getFallbackProvider();
            }
            log.debug("Using AI provider: {}", name);
            return provider;
        }
        log.warn("AI provider '{}' not found. Falling back.", name);
        return getFallbackProvider();
    }

    /**
     * Get the active configured provider, with automatic fallback.
     */
    public AIProvider getProvider() {
        return getProvider(defaultProviderName);
    }

    /**
     * Get the fallback provider. OpenAIProvider is preferred because it has
     * a built-in rule-based parser fallback for Vietnamese medical tender language.
     */
    public AIProvider getFallbackProvider() {
        if (fallbackProvider != null) {
            log.info("Falling back to AI provider: {}", fallbackProvider.getClass().getSimpleName());
            return fallbackProvider;
        }
        throw new IllegalStateException("No AI provider available. Ensure at least OpenAIProvider is on the classpath.");
    }

    private boolean isStubProvider(AIProvider provider) {
        String className = provider.getClass().getSimpleName();
        // Gemini and Claude are stubs until configured with API keys
        if (className.contains("Gemini") || className.contains("Claude")) {
            // Check if they would throw - they do if apiKey is not configured
            try {
                provider.generateChecklist("test");
                return false;
            } catch (UnsupportedOperationException e) {
                return true;
            }
        }
        return false;
    }
}
