package com.medbid.backup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupService {

    @Value("${app.backup.dir:./backups}")
    private String backupDir;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Scheduled(cron = "${app.backup.schedule:0 0 2 * * ?}")
    public void scheduledBackup() {
        log.info("Starting scheduled database backup...");
        String result = performBackup();
        log.info("Backup completed: {}", result);
    }

    public String performBackup() {
        try {
            Path backupPath = Paths.get(backupDir);
            Files.createDirectories(backupPath);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "backup_" + timestamp + ".sql";
            Path filePath = backupPath.resolve(fileName);

            String dbName = extractDbName();
            String host = extractHost();
            String port = extractPort();

            ProcessBuilder pb = new ProcessBuilder(
                    "pg_dump",
                    "-h", host,
                    "-p", port,
                    "-U", dbUsername,
                    "-d", dbName,
                    "-F", "c",
                    "-f", filePath.toString()
            );
            pb.environment().put("PGPASSWORD", dbPassword);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                long size = Files.size(filePath);
                log.info("Backup created: {} ({} bytes)", filePath, size);
                return filePath.toString();
            } else {
                String error = new String(process.getInputStream().readAllBytes());
                log.error("Backup failed: {}", error);
                return "FAILED: " + error;
            }
        } catch (Exception e) {
            log.error("Backup error", e);
            return "FAILED: " + e.getMessage();
        }
    }

    public String restoreBackup(String backupFile) {
        try {
            Path filePath = Paths.get(backupDir).resolve(backupFile);
            if (!Files.exists(filePath)) {
                return "FAILED: Backup file not found: " + backupFile;
            }

            String dbName = extractDbName();
            String host = extractHost();
            String port = extractPort();

            ProcessBuilder pb = new ProcessBuilder(
                    "pg_restore",
                    "-h", host,
                    "-p", port,
                    "-U", dbUsername,
                    "-d", dbName,
                    "-c",
                    filePath.toString()
            );
            pb.environment().put("PGPASSWORD", dbPassword);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Restore completed from: {}", backupFile);
                return "SUCCESS";
            } else {
                String error = new String(process.getInputStream().readAllBytes());
                log.error("Restore failed: {}", error);
                return "FAILED: " + error;
            }
        } catch (Exception e) {
            log.error("Restore error", e);
            return "FAILED: " + e.getMessage();
        }
    }

    public String getBackupDir() { return backupDir; }

    private String extractDbName() {
        return extractFromUrl("[^/]+$").replaceAll("\\?.*", "");
    }

    private String extractHost() {
        String result = extractFromUrl("//([^:]+)");
        return result.isEmpty() ? "localhost" : result;
    }

    private String extractPort() {
        String result = extractFromUrl(":(\\d+)/");
        return result.isEmpty() ? "5432" : result;
    }

    private String extractFromUrl(String regex) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex).matcher(datasourceUrl);
        return m.find() ? m.group(1) : "";
    }
}
