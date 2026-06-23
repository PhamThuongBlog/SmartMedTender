package com.medbid.expiry.controller;

import com.medbid.expiry.dto.ExpiryAlertDto;
import com.medbid.expiry.dto.ExpiryCheckResponse;
import com.medbid.expiry.service.ExpiryAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for expiry alert management.
 * Handles alert listing, expiry checks, and alert dismissal.
 */
@Slf4j
@RestController
@RequestMapping("/api/expiry")
@RequiredArgsConstructor
public class ExpiryAlertController {

    private final ExpiryAlertService expiryAlertService;

    /**
     * Get paginated list of active expiry alerts.
     */
    @GetMapping("/alerts")
    public ResponseEntity<Page<ExpiryAlertDto>> getAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String severity) {

        log.debug("Getting expiry alerts: page={}, size={}, severity={}", page, size, severity);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ExpiryAlertDto> alerts = expiryAlertService.getAlerts(pageable, severity);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get alert summary counts by severity.
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Long>> getSummary() {
        log.debug("Getting expiry alert summary");
        Map<String, Long> summary = expiryAlertService.getAlertSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Trigger an immediate expiry check.
     */
    @PostMapping("/check-now")
    public ResponseEntity<ExpiryCheckResponse> checkNow() {
        log.info("Manual expiry check triggered");
        ExpiryCheckResponse result = expiryAlertService.checkExpirationsNow();
        return ResponseEntity.ok(result);
    }

    /**
     * Dismiss a single alert.
     */
    @PutMapping("/alerts/{id}/dismiss")
    public ResponseEntity<Map<String, String>> dismissAlert(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID userId) {

        log.info("Dismissing alert: id={}, userId={}", id, userId);
        expiryAlertService.dismissAlert(id, userId);
        return ResponseEntity.ok(Map.of(
                "status", "DISMISSED",
                "message", "Đã bỏ qua cảnh báo",
                "alertId", id.toString()
        ));
    }

    /**
     * Dismiss all active alerts.
     */
    @PutMapping("/alerts/dismiss-all")
    public ResponseEntity<Map<String, String>> dismissAllAlerts(
            @RequestParam(required = false) UUID userId) {

        log.info("Dismissing all alerts, userId={}", userId);
        expiryAlertService.dismissAllAlerts(userId);
        return ResponseEntity.ok(Map.of(
                "status", "DISMISSED",
                "message", "Đã bỏ qua tất cả cảnh báo"
        ));
    }
}
