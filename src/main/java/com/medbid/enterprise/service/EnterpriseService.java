package com.medbid.enterprise.service;

import com.medbid.enterprise.dto.EnterpriseCreateRequest;
import com.medbid.enterprise.dto.EnterpriseProfileDto;
import com.medbid.enterprise.dto.EnterpriseUpdateRequest;
import com.medbid.enterprise.entity.EnterpriseProfile;
import com.medbid.enterprise.entity.LegalDocument;
import com.medbid.enterprise.mapper.EnterpriseMapper;
import com.medbid.enterprise.repository.EnterpriseProfileRepository;
import com.medbid.enterprise.repository.LegalDocumentRepository;
import com.medbid.exception.BusinessException;
import com.medbid.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EnterpriseService {

    private final EnterpriseProfileRepository enterpriseProfileRepository;
    private final LegalDocumentRepository legalDocumentRepository;
    private final EnterpriseMapper enterpriseMapper;

    @Transactional(readOnly = true)
    public EnterpriseProfileDto getById(UUID id) {
        EnterpriseProfile enterprise = findEnterpriseById(id);
        return enterpriseMapper.toDto(enterprise);
    }

    @Transactional(readOnly = true)
    public Page<EnterpriseProfileDto> getAll(Pageable pageable) {
        return enterpriseProfileRepository.findByDeletedFalse(pageable)
                .map(enterpriseMapper::toDto);
    }

    public EnterpriseProfileDto create(EnterpriseCreateRequest request) {
        if (request.taxCode() != null && enterpriseProfileRepository.existsByTaxCode(request.taxCode())) {
            throw new BusinessException("Enterprise with tax code " + request.taxCode() + " already exists");
        }
        EnterpriseProfile enterprise = enterpriseMapper.toEntity(request);
        enterprise = enterpriseProfileRepository.save(enterprise);
        log.info("Created enterprise profile: {} (ID: {})", enterprise.getCompanyName(), enterprise.getId());
        return enterpriseMapper.toDto(enterprise);
    }

    public EnterpriseProfileDto update(UUID id, EnterpriseUpdateRequest request) {
        EnterpriseProfile enterprise = findEnterpriseById(id);
        enterpriseMapper.updateEntity(request, enterprise);
        enterprise = enterpriseProfileRepository.save(enterprise);
        log.info("Updated enterprise profile: {} (ID: {})", enterprise.getCompanyName(), enterprise.getId());
        return enterpriseMapper.toDto(enterprise);
    }

    public void delete(UUID id) {
        EnterpriseProfile enterprise = findEnterpriseById(id);
        enterprise.setDeleted(true);
        enterpriseProfileRepository.save(enterprise);
        log.info("Soft-deleted enterprise profile: {} (ID: {})", enterprise.getCompanyName(), enterprise.getId());
    }

    @Transactional(readOnly = true)
    public List<LegalDocument> checkExpiringDocuments() {
        LocalDate now = LocalDate.now();
        LocalDate thirtyDaysLater = now.plusDays(30);
        List<LegalDocument> expiringDocs = legalDocumentRepository
                .findByExpiryDateBetweenAndDeletedFalse(now, thirtyDaysLater);
        log.info("Found {} legal documents expiring within 30 days", expiringDocs.size());
        return expiringDocs;
    }

    /**
     * Get the primary (first non-deleted) enterprise profile.
     * For single-company users, this returns their company profile.
     */
    @Transactional(readOnly = true)
    public EnterpriseProfileDto getPrimaryProfile() {
        Page<EnterpriseProfile> page = enterpriseProfileRepository.findByDeletedFalse(Pageable.ofSize(1));
        if (page.isEmpty()) {
            throw new ResourceNotFoundException("EnterpriseProfile", "primary", "none");
        }
        return enterpriseMapper.toDto(page.getContent().get(0));
    }

    /**
     * Create or update the primary enterprise profile.
     * If a profile already exists with the given ID, update it; otherwise create new.
     */
    public EnterpriseProfileDto saveProfile(EnterpriseProfileDto request) {
        EnterpriseProfile enterprise;
        if (request.id() != null) {
            // Try to find existing
            var existing = enterpriseProfileRepository.findById(request.id())
                    .filter(e -> !e.getDeleted());
            if (existing.isPresent()) {
                enterprise = existing.get();
                enterpriseMapper.updateFromDto(request, enterprise);
            } else {
                enterprise = enterpriseMapper.fromDto(request);
            }
        } else {
            // Find first existing or create
            Page<EnterpriseProfile> page = enterpriseProfileRepository.findByDeletedFalse(Pageable.ofSize(1));
            if (!page.isEmpty()) {
                enterprise = page.getContent().get(0);
                enterpriseMapper.updateFromDto(request, enterprise);
            } else {
                enterprise = enterpriseMapper.fromDto(request);
            }
        }
        enterprise = enterpriseProfileRepository.save(enterprise);
        log.info("Saved enterprise profile: {} (ID: {})", enterprise.getCompanyName(), enterprise.getId());
        return enterpriseMapper.toDto(enterprise);
    }

    private EnterpriseProfile findEnterpriseById(UUID id) {
        return enterpriseProfileRepository.findById(id)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("EnterpriseProfile", "id", id));
    }
}
