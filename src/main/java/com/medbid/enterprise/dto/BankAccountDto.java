package com.medbid.enterprise.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BankAccountDto(
        UUID id,
        UUID enterpriseId,
        String bankName,
        String branch,
        String accountNumber,
        String accountHolder,
        String swiftCode,
        String currency,
        Boolean isPrimary,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
