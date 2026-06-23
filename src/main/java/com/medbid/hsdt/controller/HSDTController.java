package com.medbid.hsdt.controller;

import com.medbid.exception.BusinessException;
import com.medbid.export.service.ExportService;
import com.medbid.hsdt.dto.HSDTBuildRequest;
import com.medbid.hsdt.dto.HSDTPreviewResponse;
import com.medbid.hsdt.service.HSDTBuilderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/hsdt")
@RequiredArgsConstructor
public class HSDTController {

    private final HSDTBuilderService builderService;
    private final ExportService exportService;

    /**
     * Preview HSDT data with full checklist, matching, pricing, and document status.
     */
    @PostMapping("/preview")
    public ResponseEntity<HSDTPreviewResponse> preview(@Valid @RequestBody HSDTBuildRequest request) {
        log.info("HSDT preview: tenderId={}, products={}", request.tenderId(), request.productIds().size());
        HSDTPreviewResponse response = builderService.buildPreview(request.tenderId(), request.productIds());
        return ResponseEntity.ok(response);
    }

    /**
     * Build and export HSDT in specified format.
     * Supports: word, pdf, zip, excel
     */
    @Transactional
    @PostMapping("/export/{format}")
    public ResponseEntity<Resource> exportHSDT(
            @PathVariable String format,
            @Valid @RequestBody HSDTBuildRequest request) {
        log.info("HSDT export: format={}, tenderId={}, products={}", format, request.tenderId(), request.productIds().size());

        // First build preview data to validate
        HSDTPreviewResponse preview = builderService.buildPreview(request.tenderId(), request.productIds());

        try {
            byte[] fileData;
            String fileName;
            String contentType;

            switch (format.toLowerCase()) {
                case "word", "docx" -> {
                    fileData = exportService.exportWord(request.tenderId());
                    fileName = "HSDT_" + sanitizeFileName(preview.getTenderName()) + ".docx";
                    contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                }
                case "pdf" -> {
                    fileData = exportService.exportPdf(request.tenderId());
                    fileName = "HSDT_" + sanitizeFileName(preview.getTenderName()) + ".pdf";
                    contentType = "application/pdf";
                }
                case "zip" -> {
                    fileData = exportService.exportZip(request.tenderId());
                    fileName = "HSDT_" + sanitizeFileName(preview.getTenderName()) + ".zip";
                    contentType = "application/zip";
                }
                case "excel", "xlsx" -> {
                    fileData = exportService.exportExcel(request.tenderId());
                    fileName = "HSDT_" + sanitizeFileName(preview.getTenderName()) + ".xlsx";
                    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                }
                default -> throw new BusinessException("Định dạng không hỗ trợ: " + format + ". Hỗ trợ: word, pdf, zip, excel");
            }

            ByteArrayResource resource = new ByteArrayResource(fileData);
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileData.length))
                    .body(resource);

        } catch (Exception e) {
            log.error("HSDT export failed", e);
            throw new BusinessException("Không thể xuất HSDT: " + e.getMessage());
        }
    }

    /**
     * Generate smart checklist for a tender with selected products.
     */
    @PostMapping("/checklist")
    public ResponseEntity<List<HSDTPreviewResponse.ChecklistItem>> generateChecklist(
            @Valid @RequestBody HSDTBuildRequest request) {
        log.info("HSDT checklist: tenderId={}, products={}", request.tenderId(), request.productIds().size());
        HSDTPreviewResponse preview = builderService.buildPreview(request.tenderId(), request.productIds());
        return ResponseEntity.ok(preview.getChecklist());
    }

    /**
     * Get the current export history for a tender.
     */
    @GetMapping("/export/{tenderId}/history")
    public ResponseEntity<?> getExportHistory(@PathVariable UUID tenderId) {
        return ResponseEntity.ok(exportService.getHistory(tenderId, org.springframework.data.domain.PageRequest.of(0, 20)));
    }

    private String sanitizeFileName(String name) {
        if (name == null) return "Document";
        return name.replaceAll("[^a-zA-Z0-9À-ỹ_\\-\\s]", "").replaceAll("\\s+", "_").substring(0, Math.min(50, name.length()));
    }
}
