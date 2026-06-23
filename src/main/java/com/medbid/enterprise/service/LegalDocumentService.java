package com.medbid.enterprise.service;

import com.medbid.enterprise.dto.LegalDocumentDto;
import com.medbid.enterprise.dto.LegalDocumentUploadRequest;
import com.medbid.enterprise.entity.EnterpriseProfile;
import com.medbid.enterprise.entity.LegalDocument;
import com.medbid.enterprise.mapper.EnterpriseMapper;
import com.medbid.enterprise.repository.EnterpriseProfileRepository;
import com.medbid.enterprise.repository.LegalDocumentRepository;
import com.medbid.exception.BusinessException;
import com.medbid.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LegalDocumentService {

    private final LegalDocumentRepository legalDocumentRepository;
    private final EnterpriseProfileRepository enterpriseProfileRepository;
    private final EnterpriseMapper enterpriseMapper;

    @Transactional(readOnly = true)
    public List<LegalDocumentDto> getByEnterpriseId(UUID enterpriseId) {
        return enterpriseMapper.toLegalDocumentDtoList(
                legalDocumentRepository.findByEnterpriseIdAndDeletedFalse(enterpriseId));
    }

    @Transactional(readOnly = true)
    public LegalDocumentDto getById(UUID id) {
        LegalDocument document = findDocumentById(id);
        return enterpriseMapper.toLegalDocumentDto(document);
    }

    public LegalDocumentDto uploadDocument(UUID enterpriseId, LegalDocumentUploadRequest request,
                                           String filePath, String fileName, Long fileSize) {
        EnterpriseProfile enterprise = enterpriseProfileRepository.findById(enterpriseId)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("EnterpriseProfile", "id", enterpriseId));

        LegalDocument document = enterpriseMapper.toLegalDocumentEntity(request);
        document.setEnterprise(enterprise);
        document.setFilePath(filePath);
        document.setFileName(fileName);
        document.setFileSize(fileSize);
        document.setStatus("ACTIVE");

        document = legalDocumentRepository.save(document);
        log.info("Uploaded legal document: {} for enterprise: {}", document.getDocumentName(), enterpriseId);
        return enterpriseMapper.toLegalDocumentDto(document);
    }

    public LegalDocumentDto updateDocument(UUID id, LegalDocumentUploadRequest request) {
        LegalDocument document = findDocumentById(id);

        if (request.documentType() != null) {
            document.setDocumentType(request.documentType());
        }
        if (request.documentName() != null) {
            document.setDocumentName(request.documentName());
        }
        if (request.issueDate() != null) {
            document.setIssueDate(request.issueDate());
        }
        if (request.expiryDate() != null) {
            document.setExpiryDate(request.expiryDate());
        }
        if (request.issuingAuthority() != null) {
            document.setIssuingAuthority(request.issuingAuthority());
        }
        if (request.notes() != null) {
            document.setNotes(request.notes());
        }

        document = legalDocumentRepository.save(document);
        log.info("Updated legal document: {} (ID: {})", document.getDocumentName(), document.getId());
        return enterpriseMapper.toLegalDocumentDto(document);
    }

    public void delete(UUID id) {
        LegalDocument document = findDocumentById(id);
        document.setDeleted(true);
        document.setStatus("DELETED");
        legalDocumentRepository.save(document);
        log.info("Soft-deleted legal document: {} (ID: {})", document.getDocumentName(), document.getId());
    }

    @Transactional(readOnly = true)
    public List<LegalDocumentDto> getExpiringDocuments() {
        LocalDate now = LocalDate.now();
        LocalDate thirtyDaysLater = now.plusDays(30);
        List<LegalDocument> expiringDocs = legalDocumentRepository
                .findByExpiryDateBetweenAndDeletedFalse(now, thirtyDaysLater);
        log.info("Found {} legal documents expiring within 30 days", expiringDocs.size());
        return enterpriseMapper.toLegalDocumentDtoList(expiringDocs);
    }

    @Transactional(readOnly = true)
    public List<LegalDocumentDto> getExpiredDocuments() {
        LocalDate now = LocalDate.now();
        List<LegalDocument> expiredDocs = legalDocumentRepository
                .findByExpiryDateBeforeAndDeletedFalse(now);
        log.info("Found {} expired legal documents", expiredDocs.size());
        return enterpriseMapper.toLegalDocumentDtoList(expiredDocs);
    }

    private LegalDocument findDocumentById(UUID id) {
        return legalDocumentRepository.findById(id)
                .filter(d -> !d.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("LegalDocument", "id", id));
    }
}
