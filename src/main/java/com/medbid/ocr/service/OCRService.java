package com.medbid.ocr.service;

import com.medbid.common.constant.AppConstants;
import com.medbid.ocr.provider.OCRProvider;
import com.medbid.ocr.provider.OCRProviderFactory;
import com.medbid.ocr.provider.OCRResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service orchestrating OCR processing with retry, async execution, and logging.
 * Uses OCRProviderFactory to select the active OCR provider.
 * On failure after retries, sends a message to the retry-topic for later processing.
 */
@Slf4j
@Service
public class OCRService {

    private final OCRProviderFactory providerFactory;
    private final OcrLogService ocrLogService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OCRService(OCRProviderFactory providerFactory,
                      OcrLogService ocrLogService,
                      KafkaTemplate<String, String> kafkaTemplate) {
        this.providerFactory = providerFactory;
        this.ocrLogService = ocrLogService;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Process a document file through OCR asynchronously.
     * Retries up to 3 times with a 2-second backoff on failure.
     * Logs the result to ocr_logs regardless of outcome.
     * On final failure, sends a message to the retry topic.
     *
     * @param filePath absolute path to the document file
     * @return CompletableFuture containing the OCRResult
     */
    @Async
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public CompletableFuture<OCRResult> processFile(String filePath) {
        OCRProvider provider = providerFactory.getDefaultProvider();
        String providerName = provider.getClass().getSimpleName();

        log.info("Starting OCR processing: file={}, provider={}", filePath, providerName);

        try {
            OCRResult result = provider.extractText(filePath);

            ocrLogService.saveSuccess(
                    providerName,
                    filePath,
                    result.text(),
                    result.confidence(),
                    result.processingTimeMs()
            );

            log.info("OCR processing completed successfully: file={}, chars={}, confidence={}, time={}ms",
                    filePath, result.text().length(), result.confidence(), result.processingTimeMs());

            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            log.error("OCR processing failed for file: {} with provider: {}", filePath, providerName, e);

            ocrLogService.saveFailure(
                    providerName,
                    filePath,
                    e.getMessage(),
                    0L
            );

            // Send to retry topic for later reprocessing
            try {
                String message = String.format(
                        "{\"filePath\":\"%s\",\"provider\":\"%s\",\"error\":\"%s\"}",
                        filePath, providerName, e.getMessage()
                );
                kafkaTemplate.send(AppConstants.KAFKA_TOPIC_RETRY, filePath, message);
                log.info("Sent retry message to topic {}: {}", AppConstants.KAFKA_TOPIC_RETRY, filePath);
            } catch (Exception kafkaEx) {
                log.error("Failed to send retry message to Kafka", kafkaEx);
            }

            throw new RuntimeException("OCR processing failed: " + e.getMessage(), e);
        }
    }
}
