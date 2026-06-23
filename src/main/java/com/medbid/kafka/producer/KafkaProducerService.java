package com.medbid.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void send(String topic, String key, Object payload) {
        try {
            String message = payload instanceof String ? (String) payload : objectMapper.writeValueAsString(payload);
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, message);
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send message to topic {}: {}", topic, ex.getMessage());
                } else {
                    log.debug("Message sent to topic {}, offset {}", topic,
                            result.getRecordMetadata().offset());
                }
            });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payload for topic {}", topic, e);
        }
    }

    public void send(String topic, Object payload) {
        send(topic, null, payload);
    }
}
