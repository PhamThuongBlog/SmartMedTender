package com.medbid.enterprise.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record EnterpriseProfileDto(
        UUID id,
        String companyName,
        String companyNameEn,
        String taxCode,
        String address,
        String phone,
        String email,
        String website,
        String legalRepresentative,
        String legalRepPosition,
        LocalDate establishedDate,
        String businessLicenseNumber,
        LocalDate businessLicenseIssueDate,
        LocalDate businessLicenseExpiryDate,
        String issuingAuthority,
        Boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer version
) {}
