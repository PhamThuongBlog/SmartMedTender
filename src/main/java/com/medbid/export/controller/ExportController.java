package com.medbid.export.controller;

import com.medbid.export.entity.ExportHistory;
import com.medbid.export.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/word/{tenderId}")
    public ResponseEntity<byte[]> exportWord(@PathVariable UUID tenderId) {
        byte[] data = exportService.exportWord(tenderId);

        String filename = "HSDT_" + getShortId(tenderId) + ".docx";
        return buildFileResponse(data, filename,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    @GetMapping("/pdf/{tenderId}")
    public ResponseEntity<byte[]> exportPdf(@PathVariable UUID tenderId) {
        byte[] data = exportService.exportPdf(tenderId);

        String filename = "HSDT_" + getShortId(tenderId) + ".pdf";
        return buildFileResponse(data, filename, MediaType.APPLICATION_PDF_VALUE);
    }

    @GetMapping("/zip/{tenderId}")
    public ResponseEntity<byte[]> exportZip(@PathVariable UUID tenderId) {
        byte[] data = exportService.exportZip(tenderId);

        String filename = "HSDT_" + getShortId(tenderId) + ".zip";
        return buildFileResponse(data, filename, "application/zip");
    }

    @GetMapping("/excel/{tenderId}")
    public ResponseEntity<byte[]> exportExcel(@PathVariable UUID tenderId) {
        byte[] data = exportService.exportExcel(tenderId);

        String filename = "HSDT_" + getShortId(tenderId) + ".xlsx";
        return buildFileResponse(data, filename,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/history")
    public ResponseEntity<Page<ExportHistory>> getHistory(
            @RequestParam(required = false) UUID tenderId,
            @PageableDefault(size = 20, sort = "createdAt,DESC") Pageable pageable) {
        return ResponseEntity.ok(exportService.getHistory(tenderId, pageable));
    }

    private ResponseEntity<byte[]> buildFileResponse(byte[] data, String filename, String contentType) {
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(encodedFilename, StandardCharsets.UTF_8)
                .build());
        headers.setContentLength(data.length);
        headers.setCacheControl(CacheControl.noCache().getHeaderValue());

        return ResponseEntity.ok().headers(headers).body(data);
    }

    private String getShortId(UUID id) {
        return id.toString().substring(0, 8).toUpperCase();
    }
}
