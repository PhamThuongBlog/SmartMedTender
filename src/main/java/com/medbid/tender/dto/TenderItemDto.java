package com.medbid.tender.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TenderItemDto(
        UUID id,
        UUID tenderId,
        Integer itemNumber,
        String name,
        String description,
        BigDecimal quantity,
        String unit,
        BigDecimal estimatedPrice,
        String notes
) {}
