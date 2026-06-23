package com.medbid.tender.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TenderItemRequest(
        @NotNull(message = "Số thứ tự không được để trống")
        @Positive(message = "Số thứ tự phải là số dương")
        Integer itemNumber,

        @NotBlank(message = "Tên thiết bị không được để trống")
        String name,

        String description,

        @Positive(message = "Số lượng phải là số dương")
        BigDecimal quantity,

        String unit,

        @Positive(message = "Đơn giá dự kiến phải là số dương")
        BigDecimal estimatedPrice,

        String notes
) {}
