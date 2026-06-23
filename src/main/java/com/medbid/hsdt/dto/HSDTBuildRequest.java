package com.medbid.hsdt.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record HSDTBuildRequest(
        @NotNull UUID tenderId,
        @NotNull List<UUID> productIds
) {}
