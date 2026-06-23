package com.medbid.quotation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record QuotationDto(
        UUID id,
        UUID tenderId,
        UUID productId,
        BigDecimal importPrice,
        BigDecimal sellingPrice,
        BigDecimal winningPrice,
        LocalDate bidDate,
        Boolean isWinning,
        String source,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
