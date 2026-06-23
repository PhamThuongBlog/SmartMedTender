package com.medbid.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_HSMT_UPLOAD = "hsmt-upload-topic";
    public static final String TOPIC_OCR_PROCESSING = "ocr-processing-topic";
    public static final String TOPIC_AI_EXTRACTION = "ai-extraction-topic";
    public static final String TOPIC_EXPORT = "export-topic";
    public static final String TOPIC_NOTIFICATION = "notification-topic";
    public static final String TOPIC_AUDIT_LOG = "audit-log-topic";
    public static final String TOPIC_RETRY = "retry-topic";
    public static final String TOPIC_DLQ = "dlq-topic";

    @Bean
    public NewTopic hsmtUploadTopic() {
        return TopicBuilder.name(TOPIC_HSMT_UPLOAD).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic ocrProcessingTopic() {
        return TopicBuilder.name(TOPIC_OCR_PROCESSING).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic aiExtractionTopic() {
        return TopicBuilder.name(TOPIC_AI_EXTRACTION).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic exportTopic() {
        return TopicBuilder.name(TOPIC_EXPORT).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name(TOPIC_NOTIFICATION).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic auditLogTopic() {
        return TopicBuilder.name(TOPIC_AUDIT_LOG).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic retryTopic() {
        return TopicBuilder.name(TOPIC_RETRY).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic dlqTopic() {
        return TopicBuilder.name(TOPIC_DLQ).partitions(1).replicas(1).build();
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(new DefaultErrorHandler(
                new FixedBackOff(2000L, 3L)));
        return factory;
    }
}
