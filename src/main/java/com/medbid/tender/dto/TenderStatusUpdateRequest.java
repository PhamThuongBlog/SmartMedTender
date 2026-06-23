package com.medbid.tender.dto;

import jakarta.validation.constraints.NotBlank;

public record TenderStatusUpdateRequest(
        @NotBlank(message = "Trạng thái không được để trống")
        String status,

        String notes
) {}
