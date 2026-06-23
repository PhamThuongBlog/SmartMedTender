package com.medbid.expiry.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpiryCheckResponse {
    private int totalAlerts;
    private int criticalCount;
    private int warningCount;
    private int infoCount;
    private int expiredCount;
    private String message;
    private List<ExpiryAlertDto> newAlerts;
}
