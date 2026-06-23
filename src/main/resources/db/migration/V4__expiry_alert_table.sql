-- ============================================
-- V4: Expiry Alert System
-- ============================================

CREATE TABLE expiry_alerts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    alert_type VARCHAR(100) NOT NULL,
    reference_type VARCHAR(100) NOT NULL,
    reference_id UUID NOT NULL,
    title VARCHAR(500) NOT NULL,
    message TEXT NOT NULL,
    days_remaining INT NOT NULL DEFAULT 0,
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_dismissed BOOLEAN NOT NULL DEFAULT FALSE,
    dismissed_at TIMESTAMP,
    dismissed_by UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for efficient querying
CREATE INDEX idx_expiry_alerts_severity ON expiry_alerts(severity);
CREATE INDEX idx_expiry_alerts_is_read ON expiry_alerts(is_read);
CREATE INDEX idx_expiry_alerts_is_dismissed ON expiry_alerts(is_dismissed);
CREATE INDEX idx_expiry_alerts_created_at ON expiry_alerts(created_at DESC);
CREATE INDEX idx_expiry_alerts_reference ON expiry_alerts(reference_type, reference_id);
