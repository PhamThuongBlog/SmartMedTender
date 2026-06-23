package com.medbid.ai.service;

import com.medbid.ai.provider.*;
import com.medbid.common.constant.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

/**
 * Service orchestrating AI-based extraction and comparison with retry, async execution, and logging.
 * Uses AIProviderFactory to select the active AI provider.
 * On failure after retries, sends a message to the retry-topic.
 */
@Slf4j
@Service
public class AIService {

    private final AIProviderFactory aiProviderFactory;
    private final AiLogService aiLogService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public AIService(AIProviderFactory aiProviderFactory,
                     AiLogService aiLogService,
                     KafkaTemplate<String, String> kafkaTemplate) {
        this.aiProviderFactory = aiProviderFactory;
        this.aiLogService = aiLogService;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Extract structured requirements from raw tender text asynchronously.
     * Retries up to 2 times with a 2-second backoff.
     * Logs to ai_logs regardless of outcome.
     *
     * @param text the raw document text to analyze
     * @return CompletableFuture containing the ExtractionResult
     */
    @Async
    @Retryable(maxAttempts = 2, backoff = @Backoff(delay = 2000))
    public CompletableFuture<ExtractionResult> extractRequirements(String text) {
        AIProvider provider = aiProviderFactory.getProvider();
        String providerName = provider.getClass().getSimpleName();
        long startTime = System.currentTimeMillis();

        log.info("Starting AI requirement extraction: provider={}, textLength={}", providerName, text.length());

        try {
            ExtractionResult result = provider.extractRequirements(text, "HSMT Document");

            long latencyMs = System.currentTimeMillis() - startTime;

            aiLogService.saveSuccess(
                    providerName,
                    result.model(),
                    "EXTRACT_REQUIREMENTS",
                    text.length() > 1000 ? text.substring(0, 1000) + "..." : text,
                    "Extracted " + result.requirements().size() + " requirements",
                    result.tokensUsed(),
                    latencyMs,
                    BigDecimal.ZERO
            );

            log.info("AI extraction complete: {} requirements, tokens={}, latency={}ms",
                    result.requirements().size(), result.tokensUsed(), latencyMs);

            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            long latencyMs = System.currentTimeMillis() - startTime;
            log.error("AI extraction failed for provider: {}", providerName, e);

            aiLogService.saveFailure(
                    providerName,
                    "unknown",
                    "EXTRACT_REQUIREMENTS",
                    text.length() > 1000 ? text.substring(0, 1000) + "..." : text,
                    e.getMessage(),
                    0,
                    latencyMs
            );

            // Send to retry topic
            try {
                String message = String.format(
                        "{\"type\":\"EXTRACT_REQUIREMENTS\",\"provider\":\"%s\",\"error\":\"%s\"}",
                        providerName, e.getMessage()
                );
                kafkaTemplate.send(AppConstants.KAFKA_TOPIC_RETRY, "ai-extraction", message);
                log.info("Sent AI extraction retry message to topic {}", AppConstants.KAFKA_TOPIC_RETRY);
            } catch (Exception kafkaEx) {
                log.error("Failed to send retry message to Kafka", kafkaEx);
            }

            throw new RuntimeException("AI extraction failed: " + e.getMessage(), e);
        }
    }

    /**
     * Compare product specifications against tender requirements asynchronously.
     * Retries up to 2 times with a 2-second backoff.
     *
     * @param productSpecs the product's technical specifications
     * @param tenderSpecs  the tender's technical requirements
     * @return CompletableFuture containing the ComparisonResult
     */
    @Async
    @Retryable(maxAttempts = 2, backoff = @Backoff(delay = 2000))
    public CompletableFuture<ComparisonResult> compareProducts(String productSpecs, String tenderSpecs) {
        AIProvider provider = aiProviderFactory.getProvider();
        String providerName = provider.getClass().getSimpleName();
        long startTime = System.currentTimeMillis();

        log.info("Starting AI product comparison: provider={}", providerName);

        try {
            ComparisonResult result = provider.compareTechnicalSpecs(productSpecs, tenderSpecs);
            long latencyMs = System.currentTimeMillis() - startTime;

            aiLogService.saveSuccess(
                    providerName,
                    "unknown",
                    "COMPARE_PRODUCTS",
                    "Product: " + (productSpecs.length() > 500 ? productSpecs.substring(0, 500) + "..." : productSpecs),
                    "Passed=" + result.passed() + ", Score=" + result.score(),
                    0,
                    latencyMs,
                    BigDecimal.ZERO
            );

            log.info("AI comparison complete: passed={}, score={}, latency={}ms",
                    result.passed(), result.score(), latencyMs);

            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            long latencyMs = System.currentTimeMillis() - startTime;
            log.error("AI comparison failed for provider: {}", providerName, e);

            aiLogService.saveFailure(
                    providerName,
                    "unknown",
                    "COMPARE_PRODUCTS",
                    productSpecs.length() > 500 ? productSpecs.substring(0, 500) + "..." : productSpecs,
                    e.getMessage(),
                    0,
                    latencyMs
            );

            throw new RuntimeException("AI comparison failed: " + e.getMessage(), e);
        }
    }
}
