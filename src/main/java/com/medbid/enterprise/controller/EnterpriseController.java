package com.medbid.enterprise.controller;

import com.medbid.enterprise.dto.*;
import com.medbid.enterprise.service.BankAccountService;
import com.medbid.enterprise.service.EnterpriseService;
import com.medbid.enterprise.service.LegalDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/enterprises")
@RequiredArgsConstructor
public class EnterpriseController {

    private final EnterpriseService enterpriseService;
    private final LegalDocumentService legalDocumentService;
    private final BankAccountService bankAccountService;

    // ============================================
    // Enterprise Profile Endpoints
    // ============================================

    /**
     * Get the primary enterprise profile (first one found, for single-company users).
     * Also accessible at /api/enterprise/profile for frontend compatibility.
     */
    @GetMapping("/profile")
    public ResponseEntity<EnterpriseProfileDto> getProfile() {
        return ResponseEntity.ok(enterpriseService.getPrimaryProfile());
    }

    /**
     * Create or update the primary enterprise profile.
     * Also accessible at /api/enterprise/profile for frontend compatibility.
     */
    @PutMapping("/profile")
    public ResponseEntity<EnterpriseProfileDto> updateProfile(@Valid @RequestBody EnterpriseProfileDto request) {
        return ResponseEntity.ok(enterpriseService.saveProfile(request));
    }

    @GetMapping
    public ResponseEntity<Page<EnterpriseProfileDto>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(enterpriseService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnterpriseProfileDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(enterpriseService.getById(id));
    }

    @PostMapping
    public ResponseEntity<EnterpriseProfileDto> create(@Valid @RequestBody EnterpriseCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enterpriseService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnterpriseProfileDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody EnterpriseUpdateRequest request) {
        return ResponseEntity.ok(enterpriseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        enterpriseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================
    // Legal Document Endpoints (scoped to enterprise)
    // ============================================

    @GetMapping("/{enterpriseId}/documents")
    public ResponseEntity<List<LegalDocumentDto>> getDocuments(@PathVariable UUID enterpriseId) {
        return ResponseEntity.ok(legalDocumentService.getByEnterpriseId(enterpriseId));
    }

    @GetMapping("/{enterpriseId}/documents/{documentId}")
    public ResponseEntity<LegalDocumentDto> getDocument(
            @PathVariable UUID enterpriseId,
            @PathVariable UUID documentId) {
        return ResponseEntity.ok(legalDocumentService.getById(documentId));
    }

    @PostMapping("/{enterpriseId}/documents")
    public ResponseEntity<LegalDocumentDto> uploadDocument(
            @PathVariable UUID enterpriseId,
            @Valid @RequestBody LegalDocumentUploadRequest request) {
        // In production, file metadata would come from multipart upload.
        // For now, filePath/fileName/fileSize are set as placeholders.
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(legalDocumentService.uploadDocument(enterpriseId, request, null, null, null));
    }

    @PutMapping("/{enterpriseId}/documents/{documentId}")
    public ResponseEntity<LegalDocumentDto> updateDocument(
            @PathVariable UUID enterpriseId,
            @PathVariable UUID documentId,
            @Valid @RequestBody LegalDocumentUploadRequest request) {
        return ResponseEntity.ok(legalDocumentService.updateDocument(documentId, request));
    }

    @DeleteMapping("/{enterpriseId}/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable UUID enterpriseId,
            @PathVariable UUID documentId) {
        legalDocumentService.delete(documentId);
        return ResponseEntity.noContent().build();
    }

    // ============================================
    // Bank Account Endpoints (scoped to enterprise)
    // ============================================

    @GetMapping("/{enterpriseId}/bank-accounts")
    public ResponseEntity<List<BankAccountDto>> getBankAccounts(@PathVariable UUID enterpriseId) {
        return ResponseEntity.ok(bankAccountService.getByEnterpriseId(enterpriseId));
    }

    @GetMapping("/{enterpriseId}/bank-accounts/{accountId}")
    public ResponseEntity<BankAccountDto> getBankAccount(
            @PathVariable UUID enterpriseId,
            @PathVariable UUID accountId) {
        return ResponseEntity.ok(bankAccountService.getById(accountId));
    }

    @PostMapping("/{enterpriseId}/bank-accounts")
    public ResponseEntity<BankAccountDto> createBankAccount(
            @PathVariable UUID enterpriseId,
            @Valid @RequestBody BankAccountDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bankAccountService.create(enterpriseId, request));
    }

    @PutMapping("/{enterpriseId}/bank-accounts/{accountId}")
    public ResponseEntity<BankAccountDto> updateBankAccount(
            @PathVariable UUID enterpriseId,
            @PathVariable UUID accountId,
            @Valid @RequestBody BankAccountDto request) {
        return ResponseEntity.ok(bankAccountService.update(accountId, request));
    }

    @DeleteMapping("/{enterpriseId}/bank-accounts/{accountId}")
    public ResponseEntity<Void> deleteBankAccount(
            @PathVariable UUID enterpriseId,
            @PathVariable UUID accountId) {
        bankAccountService.delete(accountId);
        return ResponseEntity.noContent().build();
    }

    // ============================================
    // Document Expiry Warnings
    // ============================================

    @GetMapping("/documents/expiring")
    public ResponseEntity<List<LegalDocumentDto>> getExpiringDocuments() {
        return ResponseEntity.ok(legalDocumentService.getExpiringDocuments());
    }

    @GetMapping("/documents/expired")
    public ResponseEntity<List<LegalDocumentDto>> getExpiredDocuments() {
        return ResponseEntity.ok(legalDocumentService.getExpiredDocuments());
    }
}
