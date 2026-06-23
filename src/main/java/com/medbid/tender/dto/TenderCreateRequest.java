package com.medbid.tender.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TenderCreateRequest(
        @NotBlank(message = "Tên gói thầu không được để trống")
        @Size(max = 500, message = "Tên gói thầu không được vượt quá 500 ký tự")
        String name,

        String description,

        @Size(max = 100, message = "Mã gói thầu không được vượt quá 100 ký tự")
        String bidPackageCode,

        @Size(max = 500, message = "Tên chủ đầu tư không được vượt quá 500 ký tự")
        String procuringEntity,

        LocalDateTime submissionDeadline,

        LocalDateTime openingDate,

        BigDecimal estimatedValue,

        String currency,

        String notes
) {}
