package com.medbid.tender.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TenderOutcomeRequest(
        boolean won,
        BigDecimal winningPrice,
        String currency,
        String notes
) {}
