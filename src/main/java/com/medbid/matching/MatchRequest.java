package com.medbid.matching;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MatchRequest(
        @NotNull(message = "Mã gói thầu không được để trống")
        UUID tenderId,

        UUID productId
) {}
