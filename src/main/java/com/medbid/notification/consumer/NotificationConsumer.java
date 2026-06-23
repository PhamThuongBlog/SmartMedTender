package com.medbid.notification.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medbid.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "notification-topic", groupId = "notification-group")
    public void consume(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            UUID userId = node.has("userId") && !node.get("userId").isNull()
                    ? UUID.fromString(node.get("userId").asText()) : null;
            String title = node.get("title").asText();
            String body = node.get("message").asText();
            String type = node.has("type") ? node.get("type").asText() : "SYSTEM";
            String priority = node.has("priority") ? node.get("priority").asText() : "MEDIUM";
            String link = node.has("link") ? node.get("link").asText() : null;

            if (userId != null) {
                notificationService.sendNotification(userId, title, body, type, priority, link);
            } else {
                notificationService.sendBroadcast(title, body, type, priority);
            }
        } catch (Exception e) {
            log.error("Failed to process notification message: {}", message, e);
        }
    }
}
