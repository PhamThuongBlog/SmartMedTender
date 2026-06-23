package com.medbid.common.constant;

public final class AppConstants {

    private AppConstants() {}

    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_STAFF = "STAFF";
    public static final String ROLE_REVIEWER = "REVIEWER";
    public static final String ROLE_LEGAL = "LEGAL";
    public static final String ROLE_SALES = "SALES";

    public static final String TENDER_STATUS_DRAFT = "DRAFT";
    public static final String TENDER_STATUS_REVIEWING = "REVIEWING";
    public static final String TENDER_STATUS_APPROVED = "APPROVED";
    public static final String TENDER_STATUS_SUBMITTED = "SUBMITTED";
    public static final String TENDER_STATUS_WON = "WON";
    public static final String TENDER_STATUS_LOST = "LOST";
    public static final String TENDER_STATUS_CANCELED = "CANCELED";

    public static final String REQ_STATUS_EXTRACTED = "EXTRACTED";
    public static final String REQ_STATUS_VERIFIED = "VERIFIED";
    public static final String REQ_STATUS_MATCHED = "MATCHED";
    public static final String REQ_STATUS_REJECTED = "REJECTED";

    public static final String KAFKA_TOPIC_HSMT_UPLOAD = "hsmt-upload-topic";
    public static final String KAFKA_TOPIC_OCR_PROCESSING = "ocr-processing-topic";
    public static final String KAFKA_TOPIC_AI_EXTRACTION = "ai-extraction-topic";
    public static final String KAFKA_TOPIC_EXPORT = "export-topic";
    public static final String KAFKA_TOPIC_NOTIFICATION = "notification-topic";
    public static final String KAFKA_TOPIC_AUDIT_LOG = "audit-log-topic";
    public static final String KAFKA_TOPIC_RETRY = "retry-topic";
    public static final String KAFKA_TOPIC_DLQ = "dlq-topic";
}
