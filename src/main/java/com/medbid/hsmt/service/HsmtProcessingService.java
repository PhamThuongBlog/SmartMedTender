package com.medbid.hsmt.service;

import com.medbid.ai.provider.ExtractedRequirement;
import com.medbid.ai.provider.ExtractionResult;
import com.medbid.ai.service.AIService;
import com.medbid.common.constant.AppConstants;
import com.medbid.exception.ResourceNotFoundException;
import com.medbid.tender.entity.TenderDocument;
import com.medbid.tender.entity.TenderRequirement;
import com.medbid.hsmt.repository.TenderDocumentRepository;
import com.medbid.hsmt.repository.HsmtRequirementRepository;
import com.medbid.ocr.provider.OCRResult;
import com.medbid.ocr.service.OCRService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Orchestrates the HSMT processing pipeline:
 * 1. Retrieve document from DB
 * 2. OCR text extraction via OCRService
 * 3. AI requirement extraction via AIService
 * 4. Save extracted TenderRequirements
 * 5. Handle status transitions on TenderDocument
 */
@Slf4j
@Service
public class HsmtProcessingService {

    private final TenderDocumentRepository documentRepository;
    private final HsmtRequirementRepository requirementRepository;
    private final OCRService ocrService;
    private final AIService aiService;

    public HsmtProcessingService(
            TenderDocumentRepository documentRepository,
            HsmtRequirementRepository requirementRepository,
            OCRService ocrService,
            AIService aiService) {
        this.documentRepository = documentRepository;
        this.requirementRepository = requirementRepository;
        this.ocrService = ocrService;
        this.aiService = aiService;
    }

    /**
     * Process a document through the full OCR -> AI pipeline.
     * Updates document status at each stage.
     *
     * @param documentId the ID of the TenderDocument to process
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processDocument(UUID documentId) {
        TenderDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("TenderDocument", "id", documentId));

        log.info("Starting pipeline processing for document: id={}, file={}", documentId, document.getFilePath());

        // Update status to PROCESSING
        document.setOcrStatus("PROCESSING");
        documentRepository.save(document);

        try {
            // Step 1: OCR extraction
            CompletableFuture<OCRResult> ocrFuture = ocrService.processFile(document.getFilePath());
            OCRResult ocrResult = ocrFuture.join();

            document.setOcrStatus("OCR_COMPLETED");
            documentRepository.save(document);

            log.info("OCR completed for document {}: {} chars extracted, confidence={}",
                    documentId, ocrResult.text().length(), ocrResult.confidence());

            if (ocrResult.text().isBlank()) {
                document.setOcrStatus("FAILED");
                documentRepository.save(document);
                log.warn("OCR produced empty text for document {}. Skipping AI extraction.", documentId);
                return;
            }

            // Step 2: AI requirement extraction
            document.setOcrStatus("AI_EXTRACTING");
            documentRepository.save(document);

            CompletableFuture<ExtractionResult> aiFuture = aiService.extractRequirements(ocrResult.text());
            ExtractionResult extractionResult = aiFuture.join();

            // Step 3: Save extracted requirements
            List<ExtractedRequirement> extractedReqs = extractionResult.requirements();
            List<TenderRequirement> entities = extractedReqs.stream()
                    .map(req -> {
                        TenderRequirement tr = new TenderRequirement();
                        tr.setTenderId(document.getTenderId());
                        tr.setDescription(req.description());
                        tr.setType(req.type());
                        tr.setOperator(req.operator());
                        tr.setValue(req.value());
                        tr.setUnit(req.unit());
                        tr.setMandatory(req.mandatory());
                        tr.setPriority(req.priority());
                        tr.setSource("AI_EXTRACTED");
                        tr.setSourceDocumentId(documentId);
                        tr.setConfidenceScore(req.confidenceScore());
                        tr.setStatus(AppConstants.REQ_STATUS_EXTRACTED);
                        return tr;
                    })
                    .collect(Collectors.toList());

            requirementRepository.saveAll(entities);

            // Step 4: Mark document as completed
            document.setOcrStatus("COMPLETED");
            document.setPageCount(estimatePageCount(ocrResult.text()));
            documentRepository.save(document);

            log.info("Pipeline completed for document {}: extracted {} requirements",
                    documentId, entities.size());

        } catch (Exception e) {
            log.error("Pipeline processing failed for document {}: {}", documentId, e.getMessage(), e);
            document.setOcrStatus("FAILED");
            documentRepository.save(document);
        }
    }

    /**
     * Reprocess a document (re-extract requirements).
     * Soft-deletes existing AI-extracted requirements and runs the pipeline again.
     */
    @Transactional
    public void reprocessDocument(UUID documentId) {
        TenderDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("TenderDocument", "id", documentId));

        // Soft delete existing AI-extracted requirements
        requirementRepository.softDeleteByTenderIdAndSource(document.getTenderId(), "AI_EXTRACTED");

        // Reset document status and re-process
        document.setOcrStatus("PENDING");
        documentRepository.save(document);

        processDocument(documentId);
    }

    /**
     * Roughly estimate page count based on text length.
     * Average page has ~3000 characters.
     */
    private int estimatePageCount(String text) {
        if (text == null || text.isBlank()) return 0;
        return Math.max(1, (int) Math.ceil(text.length() / 3000.0));
    }
}
