package com.medbid.product.controller;

import com.medbid.product.dto.ProductDocumentDto;
import com.medbid.product.dto.ProductDocumentUploadRequest;
import com.medbid.product.service.DocumentLibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for document library (CO/CQ/ISO/CE/FDA/Catalogue).
 * Provides global search across all product documents.
 */
@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentLibraryController {

    private final DocumentLibraryService documentLibraryService;

    /**
     * Search/list all documents with filters.
     */
    @GetMapping
    public ResponseEntity<Page<ProductDocumentDto>> listDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) String status) {

        log.debug("Listing documents: page={}, size={}, search={}, productId={}, type={}, status={}",
                page, size, search, productId, documentType, status);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProductDocumentDto> result = documentLibraryService.searchDocuments(
                search, productId, documentType, status, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Get a single document by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDocumentDto> getDocument(@PathVariable UUID id) {
        log.debug("Getting document: id={}", id);
        ProductDocumentDto doc = documentLibraryService.getDocument(id);
        return ResponseEntity.ok(doc);
    }

    /**
     * Upload a new document with file.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDocumentDto> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productId") UUID productId,
            @RequestParam("documentType") String documentType,
            @RequestParam("documentName") String documentName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "issuingAuthority", required = false) String issuingAuthority,
            @RequestParam(value = "issueDate", required = false) String issueDate,
            @RequestParam(value = "expiryDate", required = false) String expiryDate) {

        log.info("Uploading document: productId={}, type={}, name={}, file={}",
                productId, documentType, documentName, file.getOriginalFilename());

        ProductDocumentUploadRequest request = ProductDocumentUploadRequest.builder()
                .productId(productId)
                .documentType(documentType)
                .documentName(documentName)
                .issuingAuthority(issuingAuthority)
                .issueDate(issueDate != null && !issueDate.isEmpty() ? java.time.LocalDate.parse(issueDate) : null)
                .expiryDate(expiryDate != null && !expiryDate.isEmpty() ? java.time.LocalDate.parse(expiryDate) : null)
                .build();

        ProductDocumentDto result = documentLibraryService.uploadDocument(file, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Update document metadata.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDocumentDto> updateDocument(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {

        log.info("Updating document: id={}", id);

        ProductDocumentUploadRequest request = ProductDocumentUploadRequest.builder()
                .productId(body.containsKey("productId") ? UUID.fromString(body.get("productId").toString()) : null)
                .documentType((String) body.get("documentType"))
                .documentName((String) body.get("documentName"))
                .issuingAuthority((String) body.get("issuingAuthority"))
                .issueDate(body.containsKey("issueDate") && body.get("issueDate") != null
                        ? java.time.LocalDate.parse(body.get("issueDate").toString()) : null)
                .expiryDate(body.containsKey("expiryDate") && body.get("expiryDate") != null
                        ? java.time.LocalDate.parse(body.get("expiryDate").toString()) : null)
                .build();

        ProductDocumentDto result = documentLibraryService.updateDocument(id, request);
        return ResponseEntity.ok(result);
    }

    /**
     * Delete a document (soft delete).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDocument(@PathVariable UUID id) {
        log.info("Deleting document: id={}", id);
        documentLibraryService.deleteDocument(id);
        return ResponseEntity.ok(Map.of("status", "DELETED", "message", "Đã xóa tài liệu"));
    }

    /**
     * Download a document file.
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable UUID id) {
        log.info("Downloading document: id={}", id);
        ProductDocumentDto doc = documentLibraryService.getDocument(id);
        Path filePath = documentLibraryService.getDocumentFilePath(id);

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = determineContentType(doc.getFileName());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + doc.getFileName() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            log.error("Failed to read file for document: id={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String determineContentType(String fileName) {
        if (fileName == null) return "application/octet-stream";
        String ext = fileName.toLowerCase();
        if (ext.endsWith(".pdf")) return "application/pdf";
        if (ext.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (ext.endsWith(".doc")) return "application/msword";
        if (ext.endsWith(".png")) return "image/png";
        if (ext.endsWith(".jpg") || ext.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }
}
