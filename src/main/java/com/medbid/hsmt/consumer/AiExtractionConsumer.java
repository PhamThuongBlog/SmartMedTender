package com.medbid.hsmt.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medbid.common.constant.AppConstants;
import com.medbid.ai.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka consumer for AI extraction retry processing.
 * Listens on "ai-extraction-topic" for AI extraction reprocessing requests.
 * On failure, sends to retry-topic or dlq-topic.
 */
@Slf4j
@Component
public class AiExtractionConsumer {

    private final AIService aiService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRIES = 2;

    public AiExtractionConsumer(
            AIService aiService,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.aiService = aiService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = AppConstants.KAFKA_TOPIC_AI_EXTRACTION,
            groupId = "hsmt-processing-group"
    )
    public void consume(ConsumerRecord<String, String> record) {
        String key = record.key();
        String value = record.value();

        log.info("Received AI extraction message: key={}", key);

        try {
            Map<String, Object> message = objectMapper.readValue(value, Map.class);
            String text = (String) message.get("text");
            String type = (String) message.getOrDefault("type", "EXTRACT_REQUIREMENTS").toString();

            if (text == null || text.isBlank()) {
                log.error("Invalid AI extraction message: missing text. Key={}", key);
                sendToDlq(key, value, "Missing text");
                return;
            }

            // Retry count check
            int retryCount = getRetryCount(record);
            if (retryCount >= MAX_RETRIES) {
                log.error("Max AI extraction retries exceeded. Sending to DLQ.");
                sendToDlq(key, value, "Max AI extraction retries exceeded");
                return;
            }

            // Process AI extraction
            switch (type) {
                case "EXTRACT_REQUIREMENTS" -> aiService.extractRequirements(text);
                case "COMPARE_PRODUCTS" -> {
                    String productSpecs = (String) message.getOrDefault("productSpecs", "");
                    String tenderSpecs = (String) message.getOrDefault("tenderSpecs", "");
                    aiService.compareProducts(productSpecs, tenderSpecs);
                }
                default -> log.warn("Unknown AI extraction type: {}", type);
            }

            log.info("AI extraction reprocessing successful for key: {}", key);

        } catch (Exception e) {
            log.error("AI extraction retry processing failed: key={}, error={}", key, e.getMessage(), e);
            handleRetry(key, value, record, e);
        }
    }

    private void handleRetry(String key, String value, ConsumerRecord<String, String> record, Exception e) {
        int retryCount = getRetryCount(record);
        if (retryCount < MAX_RETRIES) {
            try {
                String retryMessage = enrichField(value, "retryCount", retryCount + 1);
                kafkaTemplate.send(AppConstants.KAFKA_TOPIC_RETRY, key, retryMessage);
                log.info("Sent AI extraction message to retry topic: key={}, attempt={}", key, retryCount + 1);
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
            log.info("Sent AI extraction message to DLQ: key={}, error={}", key, error);
        } catch (Exception e) {
            log.error("Failed to send AI extraction message to DLQ: key={}", key, e);
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
