package com.medbid.enterprise.controller;

import com.medbid.enterprise.dto.EnterpriseProfileDto;
import com.medbid.enterprise.dto.LegalDocumentDto;
import com.medbid.enterprise.dto.LegalDocumentUploadRequest;
import com.medbid.enterprise.service.EnterpriseService;
import com.medbid.enterprise.service.LegalDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Compatibility controller mapped to /api/enterprise (singular)
 * to support the frontend's existing API calls.
 * The main enterprise endpoints are at /api/enterprises (plural).
 */
@RestController
@RequestMapping("/api/enterprise")
@RequiredArgsConstructor
public class EnterpriseProfileController {

    private final EnterpriseService enterpriseService;
    private final LegalDocumentService legalDocumentService;

    @GetMapping("/profile")
    public ResponseEntity<EnterpriseProfileDto> getProfile() {
        return ResponseEntity.ok(enterpriseService.getPrimaryProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<EnterpriseProfileDto> updateProfile(@Valid @RequestBody EnterpriseProfileDto request) {
        return ResponseEntity.ok(enterpriseService.saveProfile(request));
    }

    /**
     * Legal documents for the primary enterprise (frontend compatibility).
     */
    @GetMapping("/legal-docs")
    public ResponseEntity<Page<LegalDocumentDto>> getLegalDocs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String documentType) {
        EnterpriseProfileDto profile = enterpriseService.getPrimaryProfile();
        List<LegalDocumentDto> docs = legalDocumentService.getByEnterpriseId(profile.id());
        // Apply simple filtering (frontend compatibility)
        if (search != null && !search.isBlank()) {
            docs = docs.stream()
                    .filter(d -> d.documentName() != null && d.documentName().toLowerCase().contains(search.toLowerCase()))
                    .toList();
        }
        if (documentType != null && !documentType.isBlank()) {
            docs = docs.stream()
                    .filter(d -> documentType.equals(d.documentType()))
                    .toList();
        }
        // Simple pagination
        int total = docs.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, total);
        List<LegalDocumentDto> pagedDocs = (fromIndex < total) ? docs.subList(fromIndex, toIndex) : List.of();
        Page<LegalDocumentDto> result = new org.springframework.data.domain.PageImpl<>(pagedDocs, PageRequest.of(page, size), total);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/legal-docs")
    public ResponseEntity<LegalDocumentDto> uploadLegalDoc(@Valid @RequestBody LegalDocumentUploadRequest request) {
        EnterpriseProfileDto profile = enterpriseService.getPrimaryProfile();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(legalDocumentService.uploadDocument(profile.id(), request, null, null, null));
    }

    @PutMapping("/legal-docs/{id}")
    public ResponseEntity<LegalDocumentDto> updateLegalDoc(
            @PathVariable UUID id,
            @Valid @RequestBody LegalDocumentUploadRequest request) {
        return ResponseEntity.ok(legalDocumentService.updateDocument(id, request));
    }

    @DeleteMapping("/legal-docs/{id}")
    public ResponseEntity<Void> deleteLegalDoc(@PathVariable UUID id) {
        legalDocumentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
