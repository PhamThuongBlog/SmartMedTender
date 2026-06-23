package com.medbid.monitoring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("service", "SmartMedTender V2");
        result.put("version", "2.0.0");
        result.put("status", "RUNNING");
        result.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "UP");
        result.put("timestamp", java.time.LocalDateTime.now().toString());

        Map<String, String> checks = new LinkedHashMap<>();
        checks.put("database", checkDatabase() ? "UP" : "DOWN");
        checks.put("disk", checkDisk() ? "UP" : "DOWN");
        checks.put("memory", checkMemory() ? "UP" : "DOWN");
        result.put("checks", checks);

        return ResponseEntity.ok(result);
    }

    private boolean checkDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(5);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkDisk() {
        java.io.File disk = new java.io.File(".");
        return disk.getFreeSpace() > 100 * 1024 * 1024;
    }

    private boolean checkMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.freeMemory() > 50 * 1024 * 1024;
    }
}
