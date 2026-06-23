package com.medbid.expiry.service;

import com.medbid.enterprise.entity.LegalDocument;
import com.medbid.enterprise.repository.LegalDocumentRepository;
import com.medbid.expiry.dto.ExpiryAlertDto;
import com.medbid.expiry.dto.ExpiryCheckResponse;
import com.medbid.expiry.entity.ExpiryAlert;
import com.medbid.expiry.repository.ExpiryAlertRepository;
import com.medbid.notification.service.NotificationService;
import com.medbid.product.entity.Product;
import com.medbid.product.entity.ProductDocument;
import com.medbid.product.repository.ProductDocumentRepository;
import com.medbid.product.repository.ProductRepository;
import com.medbid.product.service.DocumentLibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExpiryAlertService {

    private final ExpiryAlertRepository expiryAlertRepository;
    private final LegalDocumentRepository legalDocumentRepository;
    private final ProductDocumentRepository productDocumentRepository;
    private final ProductRepository productRepository;
    private final DocumentLibraryService documentLibraryService;
    private final NotificationService notificationService;

    /**
     * Daily scheduled check at 8:00 AM.
     */
    @Scheduled(cron = "${app.expiry.check-schedule:0 0 8 * * ?}")
    @Async
    public void scheduledExpiryCheck() {
        log.info("Starting scheduled expiry check...");
        try {
            ExpiryCheckResponse result = checkExpirationsNow();
            log.info("Scheduled expiry check completed: {} total, {} critical, {} warning, {} info",
                    result.getTotalAlerts(), result.getCriticalCount(),
                    result.getWarningCount(), result.getInfoCount());
        } catch (Exception e) {
            log.error("Scheduled expiry check failed", e);
        }
    }

    /**
     * Perform a full expiry scan across all document sources.
     * Creates alerts for items expiring within 90 days or already expired.
     */
    public ExpiryCheckResponse checkExpirationsNow() {
        LocalDate today = LocalDate.now();
        LocalDate ninetyDaysFromNow = today.plusDays(90);
        List<ExpiryAlert> newAlerts = new ArrayList<>();

        // 1. Check Legal Documents
        List<LegalDocument> legalDocs = legalDocumentRepository.findAll();
        for (LegalDocument doc : legalDocs) {
            if (doc.getDeleted() || doc.getExpiryDate() == null) continue;
            if (shouldCreateAlert("LEGAL_DOCUMENT", doc.getId())) {
                long daysRemaining = ChronoUnit.DAYS.between(today, doc.getExpiryDate());
                newAlerts.add(createAlert(
                        "LEGAL_EXPIRY", "LEGAL_DOCUMENT", doc.getId(),
                        "Tài liệu pháp lý sắp hết hạn: " + doc.getDocumentName(),
                        buildLegalDocMessage(doc, daysRemaining),
                        (int) daysRemaining,
                        daysRemaining
                ));
            }
        }

        // 2. Check Product Documents (CO/CQ/ISO/CE/FDA/Catalogue)
        List<Product> allProducts = productRepository.findAll();
        for (Product p : allProducts) {
            if (p.getDeleted()) continue;
            List<ProductDocument> docs = productDocumentRepository.findByProductIdAndDeletedFalse(p.getId());
            for (ProductDocument doc : docs) {
                if (doc.getExpiryDate() == null) continue;
                if (shouldCreateAlert("PRODUCT_DOCUMENT", doc.getId())) {
                    long daysRemaining = ChronoUnit.DAYS.between(today, doc.getExpiryDate());
                    newAlerts.add(createAlert(
                            "DOCUMENT_EXPIRY", "PRODUCT_DOCUMENT", doc.getId(),
                            "Chứng chỉ sản phẩm sắp hết hạn: " + doc.getDocumentName(),
                            buildProductDocMessage(p, doc, daysRemaining),
                            (int) daysRemaining,
                            daysRemaining
                    ));
                }
            }

            // 3. Check Product Registration Expiry
            if (p.getRegistrationExpiryDate() != null && shouldCreateAlert("PRODUCT_REGISTRATION", p.getId())) {
                long daysRemaining = ChronoUnit.DAYS.between(today, p.getRegistrationExpiryDate());
                newAlerts.add(createAlert(
                        "REGISTRATION_EXPIRY", "PRODUCT", p.getId(),
                        "Đăng ký sản phẩm sắp hết hạn: " + p.getName(),
                        buildRegistrationMessage(p, daysRemaining),
                        (int) daysRemaining,
                        daysRemaining
                ));
            }
        }

        // Save all new alerts
        if (!newAlerts.isEmpty()) {
            expiryAlertRepository.saveAll(newAlerts);
            log.info("Created {} new expiry alerts", newAlerts.size());

            // Send notifications for critical alerts
            List<ExpiryAlert> criticalAlerts = newAlerts.stream()
                    .filter(a -> "CRITICAL".equals(a.getSeverity()))
                    .toList();
            for (ExpiryAlert alert : criticalAlerts) {
                notificationService.sendBroadcast(
                        alert.getTitle(),
                        alert.getMessage(),
                        "EXPIRY_ALERT",
                        "HIGH"
                );
            }
        }

        // Build response
        List<ExpiryAlert> allActive = expiryAlertRepository.findByIsReadFalseAndIsDismissedFalseOrderByCreatedAtDesc(
                Pageable.unpaged()).getContent();

        int criticalCount = (int) allActive.stream().filter(a -> "CRITICAL".equals(a.getSeverity())).count();
        int warningCount = (int) allActive.stream().filter(a -> "WARNING".equals(a.getSeverity())).count();
        int infoCount = (int) allActive.stream().filter(a -> "INFO".equals(a.getSeverity())).count();
        int expiredCount = (int) allActive.stream().filter(a -> a.getDaysRemaining() < 0).count();

        return ExpiryCheckResponse.builder()
                .totalAlerts(allActive.size())
                .criticalCount(criticalCount)
                .warningCount(warningCount)
                .infoCount(infoCount)
                .expiredCount(expiredCount)
                .message("Đã kiểm tra và tạo " + newAlerts.size() + " cảnh báo mới")
                .newAlerts(newAlerts.stream().map(this::toDto).collect(Collectors.toList()))
                .build();
    }

    /**
     * Get paginated alerts.
     */
    @Transactional(readOnly = true)
    public Page<ExpiryAlertDto> getAlerts(Pageable pageable, String severity) {
        Page<ExpiryAlert> alerts;
        if (severity != null && !severity.isBlank()) {
            alerts = expiryAlertRepository.findBySeverityAndIsReadFalseAndIsDismissedFalseOrderByCreatedAtDesc(
                    severity, pageable);
        } else {
            alerts = expiryAlertRepository.findByIsReadFalseAndIsDismissedFalseOrderByCreatedAtDesc(pageable);
        }
        return alerts.map(this::toDto);
    }

    /**
     * Get alert counts by severity.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getAlertSummary() {
        Map<String, Long> summary = new LinkedHashMap<>();
        summary.put("total", expiryAlertRepository.countByIsReadFalseAndIsDismissedFalse());
        summary.put("critical", expiryAlertRepository.countBySeverityAndIsReadFalseAndIsDismissedFalse("CRITICAL"));
        summary.put("warning", expiryAlertRepository.countBySeverityAndIsReadFalseAndIsDismissedFalse("WARNING"));
        summary.put("info", expiryAlertRepository.countBySeverityAndIsReadFalseAndIsDismissedFalse("INFO"));
        return summary;
    }

    /**
     * Dismiss a single alert.
     */
    public void dismissAlert(UUID alertId, UUID userId) {
        expiryAlertRepository.findById(alertId).ifPresent(alert -> {
            alert.setIsDismissed(true);
            alert.setDismissedAt(LocalDateTime.now());
            alert.setDismissedBy(userId);
            expiryAlertRepository.save(alert);
            log.info("Alert dismissed: id={}, by user={}", alertId, userId);
        });
    }

    /**
     * Dismiss all active alerts.
     */
    public void dismissAllAlerts(UUID userId) {
        int count = expiryAlertRepository.dismissAll(LocalDateTime.now(), userId);
        log.info("Dismissed {} alerts by user {}", count, userId);
    }

    // --- Private helpers ---

    private boolean shouldCreateAlert(String referenceType, UUID referenceId) {
        return !expiryAlertRepository.existsByReferenceTypeAndReferenceIdAndIsDismissedFalse(referenceType, referenceId);
    }

    private ExpiryAlert createAlert(String alertType, String referenceType, UUID referenceId,
                                     String title, String message, int daysRemaining, long days) {
        String severity;
        if (days < 0) severity = "CRITICAL";
        else if (days <= 30) severity = "CRITICAL";
        else if (days <= 60) severity = "WARNING";
        else severity = "INFO";

        return ExpiryAlert.builder()
                .alertType(alertType)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .title(title)
                .message(message)
                .daysRemaining(daysRemaining)
                .severity(severity)
                .isRead(false)
                .isDismissed(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private String buildLegalDocMessage(LegalDocument doc, long daysRemaining) {
        String docType = formatDocType(doc.getDocumentType());
        String expiryDate = doc.getExpiryDate() != null ? doc.getExpiryDate().toString() : "N/A";
        if (daysRemaining < 0) {
            return String.format("Tài liệu %s (%s) đã hết hạn vào ngày %s (quá hạn %d ngày). Vui lòng gia hạn ngay.",
                    doc.getDocumentName(), docType, expiryDate, Math.abs(daysRemaining));
        }
        return String.format("Tài liệu %s (%s) sẽ hết hạn vào ngày %s (còn %d ngày). Vui lòng chuẩn bị gia hạn.",
                doc.getDocumentName(), docType, expiryDate, daysRemaining);
    }

    private String buildProductDocMessage(Product product, ProductDocument doc, long daysRemaining) {
        String expiryDate = doc.getExpiryDate() != null ? doc.getExpiryDate().toString() : "N/A";
        if (daysRemaining < 0) {
            return String.format("Chứng chỉ %s (%s) của sản phẩm \"%s\" đã hết hạn vào ngày %s (quá hạn %d ngày).",
                    doc.getDocumentName(), doc.getDocumentType(), product.getName(), expiryDate, Math.abs(daysRemaining));
        }
        return String.format("Chứng chỉ %s (%s) của sản phẩm \"%s\" sẽ hết hạn vào ngày %s (còn %d ngày).",
                doc.getDocumentName(), doc.getDocumentType(), product.getName(), expiryDate, daysRemaining);
    }

    private String buildRegistrationMessage(Product product, long daysRemaining) {
        String expiryDate = product.getRegistrationExpiryDate() != null ? product.getRegistrationExpiryDate().toString() : "N/A";
        String regNum = product.getRegistrationNumber() != null ? product.getRegistrationNumber() : "N/A";
        if (daysRemaining < 0) {
            return String.format("Đăng ký lưu hành số %s của sản phẩm \"%s\" đã hết hạn vào ngày %s (quá hạn %d ngày).",
                    regNum, product.getName(), expiryDate, Math.abs(daysRemaining));
        }
        return String.format("Đăng ký lưu hành số %s của sản phẩm \"%s\" sẽ hết hạn vào ngày %s (còn %d ngày).",
                regNum, product.getName(), expiryDate, daysRemaining);
    }

    private String formatDocType(String type) {
        Map<String, String> map = Map.ofEntries(
                Map.entry("BUSINESS_LICENSE", "ĐKKD"),
                Map.entry("TAX_REGISTRATION", "Đăng ký thuế"),
                Map.entry("GMP_CERT", "GMP"),
                Map.entry("DISTRIBUTION_AUTH", "Phân phối"),
                Map.entry("ISO_13485", "ISO 13485"),
                Map.entry("ISO_9001", "ISO 9001"),
                Map.entry("CE", "CE"),
                Map.entry("FDA", "FDA"),
                Map.entry("CO", "CO"),
                Map.entry("CQ", "CQ"),
                Map.entry("CATALOGUE", "Catalogue"),
                Map.entry("OTHER", "Khác")
        );
        return map.getOrDefault(type, type);
    }

    private ExpiryAlertDto toDto(ExpiryAlert alert) {
        return ExpiryAlertDto.builder()
                .id(alert.getId())
                .alertType(alert.getAlertType())
                .referenceType(alert.getReferenceType())
                .referenceId(alert.getReferenceId())
                .title(alert.getTitle())
                .message(alert.getMessage())
                .daysRemaining(alert.getDaysRemaining())
                .severity(alert.getSeverity())
                .isRead(alert.getIsRead())
                .isDismissed(alert.getIsDismissed())
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
