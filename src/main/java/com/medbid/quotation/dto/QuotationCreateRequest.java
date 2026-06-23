package com.medbid.quotation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record QuotationCreateRequest(
        @NotNull(message = "Mã gói thầu không được để trống")
        UUID tenderId,

        @NotNull(message = "Mã sản phẩm không được để trống")
        UUID productId,

        @PositiveOrZero(message = "Giá nhập không được âm")
        BigDecimal importPrice,

        @PositiveOrZero(message = "Giá bán không được âm")
        BigDecimal sellingPrice,

        BigDecimal winningPrice,

        LocalDate bidDate,

        Boolean isWinning,

        String source,

        String notes
) {}
