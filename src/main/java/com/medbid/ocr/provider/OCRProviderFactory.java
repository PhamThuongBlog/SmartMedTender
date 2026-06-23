package com.medbid.ocr.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory for selecting and managing OCR providers.
 * Uses Strategy pattern: all OCRProvider beans are injected,
 * and the active provider is selected based on configuration.
 * Falls back to TesseractOCRProvider if the configured provider is unavailable.
 */
@Slf4j
@Component
public class OCRProviderFactory {

    private final Map<String, OCRProvider> providerMap;
    private final String defaultProviderName;
    private final OCRProvider fallbackProvider;

    public OCRProviderFactory(
            List<OCRProvider> providers,
            @Value("${app.ocr.default-provider:tesseract}") String defaultProviderName) {
        this.defaultProviderName = defaultProviderName;
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(
                        p -> p.getClass().getSimpleName().replace("OCRProvider", "").toLowerCase(),
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        // Find Tesseract as ultimate fallback
        this.fallbackProvider = providers.stream()
                .filter(p -> p instanceof TesseractOCRProvider)
                .findFirst()
                .orElse(null);

        log.info("OCRProviderFactory initialized with providers: {}", providerMap.keySet());
    }

    /**
     * Get a specific OCR provider by name (e.g., "tesseract", "google", "azure").
     *
     * @param name provider name (case-insensitive)
     * @return the matching OCRProvider, or falls back to Tesseract
     */
    public OCRProvider getProvider(String name) {
        OCRProvider provider = providerMap.get(name.toLowerCase());
        if (provider != null) {
            // If the provider is a stub (throws UnsupportedOperationException), fall back
            if (isStubProvider(provider)) {
                log.warn("Provider '{}' is a stub (not fully configured). Falling back to Tesseract.", name);
                return getFallbackOrThrow();
            }
            log.debug("Using OCR provider: {}", name);
            return provider;
        }
        log.warn("OCR provider '{}' not found. Falling back.", name);
        return getFallbackOrThrow();
    }

    /**
     * Get the default provider as configured in application properties.
     */
    public OCRProvider getDefaultProvider() {
        return getProvider(defaultProviderName);
    }

    private OCRProvider getFallbackOrThrow() {
        if (fallbackProvider != null) {
            log.info("Falling back to TesseractOCRProvider.");
            return fallbackProvider;
        }
        throw new IllegalStateException("No OCR provider available. Ensure TesseractOCRProvider is on the classpath.");
    }

    private boolean isStubProvider(OCRProvider provider) {
        // Quick check: if calling extractText would throw, it's a stub
        // We avoid actually calling it; instead check by class name convention
        String className = provider.getClass().getSimpleName();
        return className.contains("Google") || className.contains("Azure");
    }
}
