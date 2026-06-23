-- ============================================
-- V3: Enhance Enterprise Profile & Add Indexes
-- ============================================

-- Add missing columns to enterprise_profiles
ALTER TABLE enterprise_profiles
    ADD COLUMN IF NOT EXISTS company_name_en VARCHAR(500),
    ADD COLUMN IF NOT EXISTS legal_rep_position VARCHAR(255),
    ADD COLUMN IF NOT EXISTS issuing_authority VARCHAR(255);

-- Add index on product_documents.expiry_date for expiry scanning
CREATE INDEX IF NOT EXISTS idx_product_documents_expiry ON product_documents(expiry_date);
CREATE INDEX IF NOT EXISTS idx_product_documents_deleted ON product_documents(deleted);

-- Add index on products.registration_expiry_date for expiry scanning
CREATE INDEX IF NOT EXISTS idx_products_registration_expiry ON products(registration_expiry_date);
