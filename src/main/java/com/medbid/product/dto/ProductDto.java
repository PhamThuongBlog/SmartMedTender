package com.medbid.product.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record ProductDto(
        UUID id,
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
        Boolean hasCoCq,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
