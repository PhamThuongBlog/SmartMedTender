package com.medbid.enterprise.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record EnterpriseCreateRequest(
        @NotBlank(message = "Company name is required")
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
