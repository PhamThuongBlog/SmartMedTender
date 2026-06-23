package com.medbid.auth.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDto(
        UUID id,
        String username,
        String email,
        String fullName,
        String phone,
        String avatarUrl,
        String roleName,
        UUID roleId,
        Boolean enabled,
        Boolean accountLocked,
        Integer failedAttempts,
        LocalDateTime lastLoginAt,
        LocalDateTime passwordChangedAt,
        Boolean mfaEnabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
