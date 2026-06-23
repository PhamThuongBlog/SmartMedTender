package com.medbid.hsmt.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medbid.common.constant.AppConstants;
import com.medbid.hsmt.repository.TenderDocumentRepository;
import com.medbid.hsmt.service.HsmtProcessingService;
import com.medbid.tender.entity.TenderDocument;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Kafka consumer for HSMT file upload events.
 * Listens on "hsmt-upload-topic" with group "hsmt-processing-group".
 * Triggers the OCR -> AI extraction pipeline.
 * On failure, sends to retry-topic or dlq-topic.
 */
@Slf4j
@Component
public class HsmtUploadConsumer {

    private final HsmtProcessingService processingService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final TenderDocumentRepository documentRepository;

    private static final int MAX_RETRIES = 3;

    public HsmtUploadConsumer(
            HsmtProcessingService processingService,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            TenderDocumentRepository documentRepository) {
        this.processingService = processingService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.documentRepository = documentRepository;
    }

    @KafkaListener(
            topics = AppConstants.KAFKA_TOPIC_HSMT_UPLOAD,
            groupId = "hsmt-processing-group"
    )
    public void consume(ConsumerRecord<String, String> record) {
        String key = record.key();
        String value = record.value();

        log.info("Received HSMT upload message: key={}, partition={}, offset={}",
                key, record.partition(), record.offset());

        try {
            Map<String, Object> message = objectMapper.readValue(value, Map.class);
            String documentIdStr = (String) message.get("documentId");

            if (documentIdStr == null || documentIdStr.isBlank()) {
                log.error("Invalid upload message: missing documentId. Key={}", key);
                sendToDlq(key, value, "Missing documentId");
                return;
            }

            UUID documentId = UUID.fromString(documentIdStr);

            // Skip if already processed synchronously during upload
            TenderDocument doc = documentRepository.findById(documentId).orElse(null);
            if (doc != null && "COMPLETED".equals(doc.getOcrStatus())) {
                log.info("Document {} already processed (status={}). Skipping.", documentId, doc.getOcrStatus());
                return;
            }

            // Check retry count from headers
            int retryCount = getRetryCount(record);
            if (retryCount >= MAX_RETRIES) {
                log.error("Max retries ({}) exceeded for document {}. Sending to DLQ.", MAX_RETRIES, documentId);
                sendToDlq(key, value, "Max retries exceeded");
                return;
            }

            // Process the document through the pipeline
            processingService.processDocument(documentId);

            log.info("Successfully processed HSMT document: {}", documentId);

        } catch (Exception e) {
            log.error("Failed to process HSMT upload message: key={}, error={}", key, e.getMessage(), e);
            handleFailure(key, value, record, e);
        }
    }

    private void handleFailure(String key, String value, ConsumerRecord<String, String> record, Exception e) {
        int retryCount = getRetryCount(record);

        if (retryCount < MAX_RETRIES) {
            // Send to retry topic with incremented retry count
            try {
                String retryMessage = enrichWithRetryCount(value, retryCount + 1);
                kafkaTemplate.send(AppConstants.KAFKA_TOPIC_RETRY, key, retryMessage);
                log.info("Sent message to retry topic: key={}, retry={}", key, retryCount + 1);
            } catch (Exception kafkaEx) {
                log.error("Failed to send to retry topic, sending to DLQ", kafkaEx);
                sendToDlq(key, value, e.getMessage());
            }
        } else {
            sendToDlq(key, value, e.getMessage());
        }
    }

    private void sendToDlq(String key, String value, String error) {
        try {
            String dlqMessage = enrichWithError(value, error);
            kafkaTemplate.send(AppConstants.KAFKA_TOPIC_DLQ, key, dlqMessage);
            log.info("Sent message to DLQ: key={}, error={}", key, error);
        } catch (Exception e) {
            log.error("Failed to send to DLQ: key={}", key, e);
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

    private String enrichWithRetryCount(String originalMessage, int retryCount) {
        try {
            Map<String, Object> map = objectMapper.readValue(originalMessage, Map.class);
            map.put("retryCount", retryCount);
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return originalMessage;
        }
    }

    private String enrichWithError(String originalMessage, String error) {
        try {
            Map<String, Object> map = objectMapper.readValue(originalMessage, Map.class);
            map.put("error", error);
            map.put("failedAt", java.time.Instant.now().toString());
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return originalMessage;
        }
    }
}
