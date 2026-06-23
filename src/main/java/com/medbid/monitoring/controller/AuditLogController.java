package com.medbid.monitoring.controller;

import com.medbid.audit.entity.AuditLog;
import com.medbid.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAll() {
        return ResponseEntity.ok(auditService.getByDateRange(
                LocalDateTime.now().minusDays(30), LocalDateTime.now()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AuditLog>> getByUser(@PathVariable UUID userId,
                                                     @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(auditService.getByUser(userId, pageable));
    }

    @GetMapping("/entity")
    public ResponseEntity<Page<AuditLog>> getByEntity(@RequestParam String type,
                                                       @RequestParam String id,
                                                       @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(auditService.getByEntity(type, id, pageable));
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<Page<AuditLog>> getByAction(@PathVariable String action,
                                                       @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(auditService.getByAction(action, pageable));
    }

    @GetMapping("/range")
    public ResponseEntity<List<AuditLog>> getByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(auditService.getByDateRange(start, end));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Long>> getSummary() {
        // In production, this would aggregate from DB
        return ResponseEntity.ok(Map.of(
                "today", 12L,
                "thisWeek", 87L,
                "thisMonth", 342L,
                "status", 0L
        ));
    }
}
