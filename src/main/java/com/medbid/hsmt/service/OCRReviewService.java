package com.medbid.hsmt.service;

import com.medbid.common.constant.AppConstants;
import com.medbid.exception.ResourceNotFoundException;
import com.medbid.tender.entity.TenderRequirement;
import com.medbid.hsmt.repository.HsmtRequirementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for OCR review workflow: approve, reject, and update extracted requirements.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OCRReviewService {

    private final HsmtRequirementRepository requirementRepository;

    /**
     * Approve a single requirement (EXTRACTED → VERIFIED).
     */
    public TenderRequirement approveRequirement(UUID requirementId) {
        TenderRequirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("TenderRequirement", "id", requirementId));

        requirement.setStatus(AppConstants.REQ_STATUS_VERIFIED);
        TenderRequirement saved = requirementRepository.save(requirement);
        log.info("Requirement approved: id={}, tenderId={}", requirementId, requirement.getTenderId());
        return saved;
    }

    /**
     * Reject a single requirement (EXTRACTED → REJECTED).
     */
    public TenderRequirement rejectRequirement(UUID requirementId, String reason) {
        TenderRequirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("TenderRequirement", "id", requirementId));

        requirement.setStatus(AppConstants.REQ_STATUS_REJECTED);
        TenderRequirement saved = requirementRepository.save(requirement);
        log.info("Requirement rejected: id={}, tenderId={}, reason={}", requirementId, requirement.getTenderId(), reason);
        return saved;
    }

    /**
     * Batch approve all EXTRACTED requirements for a tender.
     */
    public int batchApprove(UUID tenderId) {
        List<TenderRequirement> extracted = requirementRepository.findByTenderId(tenderId).stream()
                .filter(r -> AppConstants.REQ_STATUS_EXTRACTED.equals(r.getStatus()))
                .toList();

        for (TenderRequirement r : extracted) {
            r.setStatus(AppConstants.REQ_STATUS_VERIFIED);
        }
        requirementRepository.saveAll(extracted);
        log.info("Batch approved {} requirements for tender {}", extracted.size(), tenderId);
        return extracted.size();
    }

    /**
     * Batch reject all EXTRACTED requirements for a tender.
     */
    public int batchReject(UUID tenderId) {
        List<TenderRequirement> extracted = requirementRepository.findByTenderId(tenderId).stream()
                .filter(r -> AppConstants.REQ_STATUS_EXTRACTED.equals(r.getStatus()))
                .toList();

        for (TenderRequirement r : extracted) {
            r.setStatus(AppConstants.REQ_STATUS_REJECTED);
        }
        requirementRepository.saveAll(extracted);
        log.info("Batch rejected {} requirements for tender {}", extracted.size(), tenderId);
        return extracted.size();
    }

    /**
     * Update requirement fields and optionally verify it.
     */
    public TenderRequirement updateRequirement(UUID requirementId, Map<String, Object> updates) {
        TenderRequirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("TenderRequirement", "id", requirementId));

        if (updates.containsKey("description")) {
            requirement.setDescription((String) updates.get("description"));
        }
        if (updates.containsKey("type")) {
            requirement.setType((String) updates.get("type"));
        }
        if (updates.containsKey("operator")) {
            requirement.setOperator((String) updates.get("operator"));
        }
        if (updates.containsKey("value")) {
            requirement.setValue((String) updates.get("value"));
        }
        if (updates.containsKey("unit")) {
            requirement.setUnit((String) updates.get("unit"));
        }
        if (updates.containsKey("mandatory")) {
            Object val = updates.get("mandatory");
            if (val instanceof Boolean b) requirement.setMandatory(b);
            else if (val instanceof String s) requirement.setMandatory(Boolean.parseBoolean(s));
        }
        if (updates.containsKey("priority")) {
            Object val = updates.get("priority");
            if (val instanceof Number n) requirement.setPriority(n.intValue());
        }
        if (updates.containsKey("status")) {
            String newStatus = (String) updates.get("status");
            if (List.of(AppConstants.REQ_STATUS_EXTRACTED, AppConstants.REQ_STATUS_VERIFIED,
                    AppConstants.REQ_STATUS_MATCHED, AppConstants.REQ_STATUS_REJECTED).contains(newStatus)) {
                requirement.setStatus(newStatus);
            }
        }

        TenderRequirement saved = requirementRepository.save(requirement);
        log.info("Requirement updated: id={}, fields={}", requirementId, updates.keySet());
        return saved;
    }

    /**
     * Get requirements for a tender with optional status filter.
     */
    @Transactional(readOnly = true)
    public List<TenderRequirement> getRequirements(UUID tenderId, String status) {
        List<TenderRequirement> all = requirementRepository.findByTenderId(tenderId);
        if (status != null && !status.isBlank()) {
            return all.stream()
                    .filter(r -> status.equals(r.getStatus()))
                    .toList();
        }
        return all;
    }
}
