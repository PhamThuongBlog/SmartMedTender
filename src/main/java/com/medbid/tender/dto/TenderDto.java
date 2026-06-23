package com.medbid.tender.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TenderDto(
        UUID id,
        String name,
        String description,
        String bidPackageCode,
        String procuringEntity,
        LocalDateTime submissionDeadline,
        LocalDateTime openingDate,
        BigDecimal estimatedValue,
        String currency,
        String status,
        UUID clonedFromId,
        String notes,
        Integer version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
