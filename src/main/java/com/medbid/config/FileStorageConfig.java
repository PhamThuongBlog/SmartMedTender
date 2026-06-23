package com.medbid.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Configuration
public class FileStorageConfig {

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${app.backup.dir:./backups}")
    private String backupDir;

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
            log.info("Upload directory: {}", uploadPath.toAbsolutePath());

            Path backupPath = Paths.get(backupDir);
            Files.createDirectories(backupPath);
            log.info("Backup directory: {}", backupPath.toAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to initialize directories", e);
        }
    }
}
