package com.medbid.expiry.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpiryAlertDto {
    private UUID id;
    private String alertType;
    private String referenceType;
    private UUID referenceId;
    private String title;
    private String message;
    private Integer daysRemaining;
    private String severity;
    private Boolean isRead;
    private Boolean isDismissed;
    private LocalDateTime createdAt;
}
