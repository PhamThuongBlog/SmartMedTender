package com.medbid.hsmt.controller;

import com.medbid.common.constant.AppConstants;
import com.medbid.exception.BusinessException;
import com.medbid.exception.ResourceNotFoundException;
import com.medbid.tender.entity.TenderDocument;
import com.medbid.tender.entity.TenderRequirement;
import com.medbid.hsmt.repository.TenderDocumentRepository;
import com.medbid.hsmt.repository.HsmtRequirementRepository;
import com.medbid.hsmt.service.HsmtProcessingService;
import com.medbid.hsmt.service.HsmtUploadService;
import com.medbid.hsmt.service.OCRReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for HSMT (Hồ Sơ Mời Thầu) management.
 * Handles file uploads, requirement extraction, and manual review workflows.
 */
@Slf4j
@RestController
@RequestMapping("/api/hsmt")
public class HsmtController {

    private final HsmtUploadService uploadService;
    private final HsmtProcessingService processingService;
    private final OCRReviewService reviewService;
    private final TenderDocumentRepository documentRepository;
    private final HsmtRequirementRepository requirementRepository;

    public HsmtController(
            HsmtUploadService uploadService,
            HsmtProcessingService processingService,
            OCRReviewService reviewService,
            TenderDocumentRepository documentRepository,
            HsmtRequirementRepository requirementRepository) {
        this.uploadService = uploadService;
        this.processingService = processingService;
        this.reviewService = reviewService;
        this.documentRepository = documentRepository;
        this.requirementRepository = requirementRepository;
    }

    /**
     * Upload an HSMT file for processing.
     * Accepts multipart file uploads up to 50MB.
     * Validates MIME type and starts the OCR -> AI extraction pipeline.
     *
     * @param file         the uploaded file
     * @param tenderId     the associated tender ID
     * @param documentType the document type (default: "HSMT")
     * @return the created TenderDocument record
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadHsmtFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tenderId") UUID tenderId,
            @RequestParam(value = "documentType", defaultValue = "HSMT") String documentType) {

        log.info("HSMT upload request: tenderId={}, fileName={}, size={}, type={}",
                tenderId, file.getOriginalFilename(), file.getSize(), file.getContentType());

        TenderDocument document = uploadService.uploadFile(file, tenderId, documentType);

        // Fetch extracted requirements
        List<TenderRequirement> requirements = requirementRepository.findByTenderId(tenderId);

        long mandatoryCount = requirements.stream()
                .filter(req -> Boolean.TRUE.equals(req.getMandatory()))
                .count();

        double avgConfidence = requirements.stream()
                .mapToDouble(req -> req.getConfidenceScore() != null ? req.getConfidenceScore() : 0.0)
                .average()
                .orElse(0.0);

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("status", document.getOcrStatus());
        result.put("documentId", document.getId());
        result.put("tenderId", tenderId);
        result.put("totalRequirements", requirements.size());
        result.put("mandatoryCount", mandatoryCount);
        result.put("confidence", avgConfidence);
        result.put("requirements", requirements);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * List all extracted requirements for a tender.
     *
     * @param tenderId the tender ID
     * @param status   optional status filter (EXTRACTED, VERIFIED, REJECTED, MATCHED)
     * @return list of TenderRequirement entities
     */
    @GetMapping("/{tenderId}/requirements")
    public ResponseEntity<List<TenderRequirement>> getRequirements(
            @PathVariable UUID tenderId,
            @RequestParam(required = false) String status) {
        log.debug("Fetching requirements for tender: {}, status={}", tenderId, status);
        List<TenderRequirement> requirements = reviewService.getRequirements(tenderId, status);
        return ResponseEntity.ok(requirements);
    }

    /**
     * List all uploaded documents for a tender.
     *
     * @param tenderId the tender ID
     * @return list of TenderDocument entities
     */
    @GetMapping("/{tenderId}/documents")
    public ResponseEntity<List<TenderDocument>> getDocuments(@PathVariable UUID tenderId) {
        log.debug("Fetching documents for tender: {}", tenderId);
        List<TenderDocument> documents = documentRepository.findByTenderId(tenderId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Manually update an extracted requirement (for correction by reviewers).
     *
     * @param id   the requirement ID
     * @param body JSON body with fields to update
     * @return the updated TenderRequirement
     */
    @PutMapping("/requirements/{id}")
    public ResponseEntity<TenderRequirement> updateRequirement(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {

        log.info("Manual update for requirement: {}", id);

        TenderRequirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TenderRequirement", "id", id));

        if (body.containsKey("description")) {
            requirement.setDescription((String) body.get("description"));
        }
        if (body.containsKey("type")) {
            requirement.setType((String) body.get("type"));
        }
        if (body.containsKey("operator")) {
            requirement.setOperator((String) body.get("operator"));
        }
        if (body.containsKey("value")) {
            requirement.setValue((String) body.get("value"));
        }
        if (body.containsKey("unit")) {
            requirement.setUnit((String) body.get("unit"));
        }
        if (body.containsKey("mandatory")) {
            Object mandatory = body.get("mandatory");
            if (mandatory instanceof Boolean b) {
                requirement.setMandatory(b);
            } else if (mandatory instanceof String s) {
                requirement.setMandatory(Boolean.parseBoolean(s));
            }
        }
        if (body.containsKey("priority")) {
            Object priority = body.get("priority");
            if (priority instanceof Number n) {
                requirement.setPriority(n.intValue());
            }
        }

        TenderRequirement saved = requirementRepository.save(requirement);
        log.info("Requirement updated: id={}", id);
        return ResponseEntity.ok(saved);
    }

    /**
     * Approve a requirement after human review.
     * Changes status from EXTRACTED to VERIFIED.
     *
     * @param id the requirement ID
     * @return the updated TenderRequirement
     */
    @PostMapping("/requirements/{id}/approve")
    public ResponseEntity<TenderRequirement> approveRequirement(@PathVariable UUID id) {
        log.info("Approving requirement: {}", id);
        TenderRequirement saved = reviewService.approveRequirement(id);
        return ResponseEntity.ok(saved);
    }

    /**
     * Reject a requirement after human review.
     * Changes status from EXTRACTED to REJECTED.
     *
     * @param id   the requirement ID
     * @param body optional JSON body with "reason" field
     * @return the updated TenderRequirement
     */
    @PostMapping("/requirements/{id}/reject")
    public ResponseEntity<TenderRequirement> rejectRequirement(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "") : "";
        log.info("Rejecting requirement: id={}, reason={}", id, reason);
        TenderRequirement saved = reviewService.rejectRequirement(id, reason);
        return ResponseEntity.ok(saved);
    }

    /**
     * Batch approve all EXTRACTED requirements for a tender.
     */
    @PostMapping("/requirements/batch-approve")
    public ResponseEntity<Map<String, Object>> batchApproveRequirements(
            @RequestParam UUID tenderId) {
        log.info("Batch approving requirements for tender: {}", tenderId);
        int count = reviewService.batchApprove(tenderId);
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Đã phê duyệt " + count + " yêu cầu",
                "approvedCount", count
        ));
    }

    /**
     * Batch reject all EXTRACTED requirements for a tender.
     */
    @PostMapping("/requirements/batch-reject")
    public ResponseEntity<Map<String, Object>> batchRejectRequirements(
            @RequestParam UUID tenderId) {
        log.info("Batch rejecting requirements for tender: {}", tenderId);
        int count = reviewService.batchReject(tenderId);
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Đã từ chối " + count + " yêu cầu",
                "rejectedCount", count
        ));
    }

    /**
     * Batch upload multiple HSMT files for a tender.
     */
    @PostMapping(value = "/upload/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadBatch(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("tenderId") UUID tenderId,
            @RequestParam(value = "documentType", defaultValue = "HSMT") String documentType) {

        log.info("Batch HSMT upload: tenderId={}, fileCount={}", tenderId, files.size());

        List<Map<String, Object>> results = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (MultipartFile file : files) {
            try {
                TenderDocument document = uploadService.uploadFile(file, tenderId, documentType);
                results.add(Map.of(
                        "fileName", (Object) file.getOriginalFilename(),
                        "status", "COMPLETED",
                        "documentId", document.getId().toString()
                ));
                successCount++;
            } catch (Exception e) {
                log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
                results.add(Map.of(
                        "fileName", (Object) file.getOriginalFilename(),
                        "status", "FAILED",
                        "error", e.getMessage()
                ));
                failCount++;
            }
        }

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("tenderId", tenderId);
        result.put("totalFiles", files.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("results", results);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Reprocess a document through the OCR -> AI pipeline.
     *
     * @param documentId the document ID to reprocess
     * @return 202 Accepted
     */
    @PostMapping("/documents/{documentId}/reprocess")
    public ResponseEntity<Map<String, String>> reprocessDocument(@PathVariable UUID documentId) {
        log.info("Reprocessing document: {}", documentId);
        processingService.reprocessDocument(documentId);
        return ResponseEntity.accepted().body(Map.of(
                "status", "ACCEPTED",
                "message", "Document reprocessing started",
                "documentId", documentId.toString()
        ));
    }
}
