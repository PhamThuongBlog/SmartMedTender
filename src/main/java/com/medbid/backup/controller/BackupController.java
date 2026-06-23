package com.medbid.backup.controller;

import com.medbid.backup.service.BackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class BackupController {

    private final BackupService backupService;

    @Value("${app.backup.rto-minutes:60}")
    private int rtoMinutes;

    @Value("${app.backup.rpo-minutes:1440}")
    private int rpoMinutes;

    @Value("${app.backup.offsite-dir:./backups/offsite}")
    private String offsiteDir;

    @PostMapping
    public ResponseEntity<Map<String, String>> createBackup() {
        String result = backupService.performBackup();
        return ResponseEntity.ok(Map.of(
                "status", result.startsWith("FAILED") ? "ERROR" : "SUCCESS",
                "file", result
        ));
    }

    /**
     * Off-site backup — copies backup to external/cloud storage location.
     * Simulates cloud upload by copying to a separate directory.
     */
    @PostMapping("/offsite")
    public ResponseEntity<Map<String, Object>> offsiteBackup() {
        Map<String, Object> response = new LinkedHashMap<>();

        // Create local backup first
        String localResult = backupService.performBackup();
        if (localResult.startsWith("FAILED")) {
            response.put("status", "ERROR");
            response.put("localBackup", "FAILED");
            response.put("message", "Local backup failed, cannot proceed with off-site");
            return ResponseEntity.internalServerError().body(response);
        }

        // Simulate off-site transfer
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String offsiteFile = "offsite_backup_" + timestamp + ".sql";

            java.nio.file.Path source = java.nio.file.Path.of(localResult);
            java.nio.file.Path target = java.nio.file.Path.of(offsiteDir, offsiteFile);
            java.nio.file.Files.createDirectories(target.getParent());
            java.nio.file.Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            long fileSize = java.nio.file.Files.size(target);

            response.put("status", "SUCCESS");
            response.put("localFile", localResult);
            response.put("offsiteFile", target.toString());
            response.put("offsiteSizeBytes", fileSize);
            response.put("transferredAt", timestamp);
            response.put("message", "Backup đã được chuyển đến vị trí off-site");
        } catch (Exception e) {
            response.put("status", "WARNING");
            response.put("localFile", localResult);
            response.put("message", "Backup cục bộ thành công nhưng off-site thất bại: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/restore")
    public ResponseEntity<Map<String, String>> restore(@RequestParam String file) {
        String result = backupService.restoreBackup(file);
        return ResponseEntity.ok(Map.of("status", result));
    }

    /**
     * Get RTO/RPO SLA configuration and backup status.
     */
    @GetMapping("/sla")
    public ResponseEntity<Map<String, Object>> getSLA() {
        Map<String, Object> sla = new LinkedHashMap<>();
        sla.put("rtoMinutes", rtoMinutes);
        sla.put("rpoMinutes", rpoMinutes);
        sla.put("rtoDescription", "RTO: " + rtoMinutes + " phút — thời gian tối đa để khôi phục hệ thống sau sự cố");
        sla.put("rpoDescription", "RPO: " + rpoMinutes + " phút — dữ liệu bị mất tối đa kể từ lần backup cuối");
        sla.put("backupSchedule", "0 0 2 * * ? (2:00 AM hàng ngày)");
        sla.put("retentionDays", 30);
        sla.put("offsiteEnabled", true);
        sla.put("offsiteDir", offsiteDir);

        // List recent backups
        try {
            java.nio.file.Path backupPath = java.nio.file.Path.of(backupService.getBackupDir());
            if (java.nio.file.Files.exists(backupPath)) {
                var files = java.nio.file.Files.list(backupPath)
                        .filter(f -> f.toString().endsWith(".sql"))
                        .sorted((a, b) -> b.compareTo(a))
                        .limit(5)
                        .map(p -> {
                            try {
                                long size = java.nio.file.Files.size(p);
                                return p.getFileName().toString() + " (" + (size / 1024) + " KB)";
                            } catch (Exception e) { return p.getFileName().toString(); }
                        })
                        .toList();
                sla.put("recentBackups", files);
            }
        } catch (Exception e) {
            sla.put("recentBackups", "Không thể liệt kê: " + e.getMessage());
        }

        return ResponseEntity.ok(sla);
    }
}
