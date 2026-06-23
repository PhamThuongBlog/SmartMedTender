package com.medbid.tender.entity;

public enum TenderStatus {
    DRAFT,
    REVIEWING,
    APPROVED,
    SUBMITTED,
    WON,
    LOST,
    CANCELED;

    public boolean canTransitionTo(TenderStatus target) {
        return switch (this) {
            case DRAFT -> target == REVIEWING || target == CANCELED;
            case REVIEWING -> target == APPROVED || target == DRAFT || target == CANCELED;
            case APPROVED -> target == SUBMITTED || target == REVIEWING || target == CANCELED;
            case SUBMITTED -> target == WON || target == LOST || target == CANCELED;
            case WON, LOST, CANCELED -> false;
        };
    }
}
