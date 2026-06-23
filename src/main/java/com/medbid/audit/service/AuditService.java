package com.medbid.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medbid.audit.entity.AuditLog;
import com.medbid.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Async
    @Transactional
    public void log(String action, String entityType, String entityId,
                    Object oldValue, Object newValue, String details,
                    UUID userId, String username, String ipAddress, String userAgent) {
        try {
            AuditLog logEntry = new AuditLog();
            logEntry.setAction(action);
            logEntry.setEntityType(entityType);
            logEntry.setEntityId(entityId);
            logEntry.setOldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null);
            logEntry.setNewValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null);
            logEntry.setDetails(details);
            logEntry.setUserId(userId);
            logEntry.setUsername(username);
            logEntry.setIpAddress(ipAddress);
            logEntry.setUserAgent(userAgent);
            auditLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to write audit log", e);
        }
    }

    public Page<AuditLog> getByUser(UUID userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Page<AuditLog> getByEntity(String entityType, String entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable);
    }

    public List<AuditLog> getByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByCreatedAtBetween(start, end);
    }

    public Page<AuditLog> getByAction(String action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable);
    }
}
