package com.medbid.hsmt.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medbid.common.constant.AppConstants;
import com.medbid.ocr.service.OCRService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka consumer for OCR retry processing.
 * Listens on "ocr-processing-topic" for OCR reprocessing requests.
 * On failure, sends to retry-topic or dlq-topic.
 */
@Slf4j
@Component
public class OcrProcessingConsumer {

    private final OCRService ocrService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRIES = 3;

    public OcrProcessingConsumer(
            OCRService ocrService,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.ocrService = ocrService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = AppConstants.KAFKA_TOPIC_OCR_PROCESSING,
            groupId = "hsmt-processing-group"
    )
    public void consume(ConsumerRecord<String, String> record) {
        String key = record.key();
        String value = record.value();

        log.info("Received OCR processing message: key={}", key);

        try {
            Map<String, Object> message = objectMapper.readValue(value, Map.class);
            String filePath = (String) message.get("filePath");

            if (filePath == null || filePath.isBlank()) {
                log.error("Invalid OCR message: missing filePath. Key={}", key);
                sendToDlq(key, value, "Missing filePath");
                return;
            }

            // Retry count check
            int retryCount = getRetryCount(record);
            if (retryCount >= MAX_RETRIES) {
                log.error("Max OCR retries exceeded for file: {}. Sending to DLQ.", filePath);
                sendToDlq(key, value, "Max OCR retries exceeded");
                return;
            }

            // Process OCR
            ocrService.processFile(filePath);

            log.info("OCR reprocessing successful for file: {}", filePath);

        } catch (Exception e) {
            log.error("OCR retry processing failed: key={}, error={}", key, e.getMessage(), e);
            handleRetry(key, value, record, e);
        }
    }

    private void handleRetry(String key, String value, ConsumerRecord<String, String> record, Exception e) {
        int retryCount = getRetryCount(record);
        if (retryCount < MAX_RETRIES) {
            try {
                String retryMessage = enrichRetryCount(value, retryCount + 1);
                kafkaTemplate.send(AppConstants.KAFKA_TOPIC_RETRY, key, retryMessage);
                log.info("Sent OCR message to retry topic: key={}, attempt={}", key, retryCount + 1);
            } catch (Exception kafkaEx) {
                sendToDlq(key, value, e.getMessage());
            }
        } else {
            sendToDlq(key, value, e.getMessage());
        }
    }

    private void sendToDlq(String key, String value, String error) {
        try {
            String dlqMessage = enrichField(value, "error", error);
            kafkaTemplate.send(AppConstants.KAFKA_TOPIC_DLQ, key, dlqMessage);
            log.info("Sent OCR message to DLQ: key={}, error={}", key, error);
        } catch (Exception e) {
            log.error("Failed to send OCR message to DLQ: key={}", key, e);
        }
    }

    private int getRetryCount(ConsumerRecord<String, String> record) {
        try {
            var header = record.headers().lastHeader("retry-count");
            if (header != null) {
                return Integer.parseInt(new String(header.value()));
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private String enrichRetryCount(String originalMessage, int retryCount) {
        return enrichField(originalMessage, "retryCount", retryCount);
    }

    private String enrichField(String originalMessage, String field, Object value) {
        try {
            Map<String, Object> map = objectMapper.readValue(originalMessage, Map.class);
            map.put(field, value);
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return originalMessage;
        }
    }
}
