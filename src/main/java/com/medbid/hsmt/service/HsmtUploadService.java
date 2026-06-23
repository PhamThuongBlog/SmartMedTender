package com.medbid.hsmt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medbid.common.constant.AppConstants;
import com.medbid.exception.BusinessException;
import com.medbid.tender.entity.TenderDocument;
import com.medbid.hsmt.repository.TenderDocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Service for handling HSMT file uploads.
 * Validates file type, saves to disk, creates DB records, and sends Kafka messages
 * to trigger the OCR -> AI extraction pipeline.
 */
@Slf4j
@Service
public class HsmtUploadService {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel",
            "application/zip",
            "application/x-zip-compressed",
            "image/png",
            "image/jpeg",
            "image/jpg"
    );

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    private final TenderDocumentRepository documentRepository;
    private final HsmtProcessingService processingService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Path uploadDir;

    public HsmtUploadService(
            TenderDocumentRepository documentRepository,
            HsmtProcessingService processingService,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.file.upload-dir:./uploads}") String uploadDirPath) {
        this.documentRepository = documentRepository;
        this.processingService = processingService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.uploadDir);
            log.info("HSMT upload directory initialized: {}", this.uploadDir);
        } catch (IOException e) {
            log.error("Failed to create upload directory: {}", this.uploadDir, e);
            throw new RuntimeException("Cannot create upload directory", e);
        }
    }

    /**
     * Upload a HSMT file for a tender.
     *
     * @param file       the multipart file to upload
     * @param tenderId   the associated tender ID
     * @param documentType the type of document (e.g., "HSMT", "PHU_LUC", "BAN_VE")
     * @return the created TenderDocument record
     */
    @Transactional
    public TenderDocument uploadFile(MultipartFile file, UUID tenderId, String documentType) {
        // Validate file
        validateFile(file);

        // Generate unique file name to prevent collisions
        String originalFileName = file.getOriginalFilename();
        String extension = getFileExtension(originalFileName);
        String storedFileName = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        // Create tender-specific subdirectory
        Path tenderDir = uploadDir.resolve(tenderId.toString());
        try {
            Files.createDirectories(tenderDir);
        } catch (IOException e) {
            log.error("Failed to create tender directory: {}", tenderDir, e);
            throw new BusinessException("Không thể tạo thư mục lưu trữ cho gói thầu");
        }

        // Save file to disk
        Path targetPath = tenderDir.resolve(storedFileName);
        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved: {} (original: {}, size: {} bytes)", targetPath, originalFileName, file.getSize());
        } catch (IOException e) {
            log.error("Failed to save file: {}", targetPath, e);
            throw new BusinessException("Không thể lưu file: " + e.getMessage());
        }

        // Create database record
        TenderDocument document = new TenderDocument();
        document.setTenderId(tenderId);
        document.setDocumentType(documentType);
        document.setFilePath(targetPath.toString());
        document.setFileName(originalFileName);
        document.setFileSize(file.getSize());
        document.setPageCount(null);
        document.setOcrStatus("PENDING");
        document.setUploadedAt(LocalDateTime.now());

        TenderDocument saved = documentRepository.save(document);
        log.info("TenderDocument saved: id={}, tenderId={}, type={}", saved.getId(), tenderId, documentType);

        // Process document synchronously (OCR + AI extraction)
        try {
            processingService.processDocument(saved.getId());
        } catch (Exception e) {
            log.error("Synchronous processing failed for document {}: {}", saved.getId(), e.getMessage(), e);
        }

        // Send Kafka message for retry/audit pipeline
        sendUploadMessage(saved);

        return saved;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File không được để trống");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("Kích thước file vượt quá giới hạn 50MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("Định dạng file không được hỗ trợ: " + contentType +
                    ". Các định dạng hỗ trợ: PDF, DOCX, XLSX, ZIP, PNG, JPG");
        }
    }

    private void sendUploadMessage(TenderDocument document) {
        try {
            Map<String, Object> message = Map.of(
                    "documentId", document.getId().toString(),
                    "tenderId", document.getTenderId().toString(),
                    "filePath", document.getFilePath(),
                    "fileName", document.getFileName(),
                    "documentType", document.getDocumentType(),
                    "fileSize", document.getFileSize(),
                    "uploadedAt", document.getUploadedAt().toString()
            );

            String jsonMessage = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(
                    AppConstants.KAFKA_TOPIC_HSMT_UPLOAD,
                    document.getId().toString(),
                    jsonMessage
            );
            log.info("HSMT upload message sent to topic {}: documentId={}",
                    AppConstants.KAFKA_TOPIC_HSMT_UPLOAD, document.getId());

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize upload message to JSON", e);
            throw new BusinessException("Lỗi xử lý dữ liệu upload");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}
