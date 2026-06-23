package com.medbid.matching.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OverrideRequest(
        @NotNull UUID matchResultId,
        @NotNull boolean passed,
        String reason
) {}
