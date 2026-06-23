package com.medbid.enterprise.dto;

import java.time.LocalDate;

public record EnterpriseUpdateRequest(
        String companyName,
        String taxCode,
        String address,
        String phone,
        String email,
        String website,
        String legalRepresentative,
        LocalDate establishedDate,
        String businessLicenseNumber,
        LocalDate businessLicenseIssueDate,
        LocalDate businessLicenseExpiryDate
) {}
