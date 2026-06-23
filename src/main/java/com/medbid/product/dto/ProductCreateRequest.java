package com.medbid.product.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.Map;

public record ProductCreateRequest(
        @NotBlank(message = "Product name is required")
        String name,

        String manufacturer,
        String brand,
        String model,
        String originCountry,
        String category,
        String description,
        Map<String, Object> technicalSpecs,
        String registrationNumber,
        LocalDate registrationIssueDate,
        LocalDate registrationExpiryDate,
        Boolean hasIso,
        Boolean hasFda,
        Boolean hasCe,
        Boolean hasCoCq
) {}
