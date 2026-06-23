-- V2: Add missing audit columns to tables that lack them
-- These columns are required by BaseEntity (@MappedSuperclass)

-- product_documents
ALTER TABLE product_documents
    ADD COLUMN IF NOT EXISTS created_by UUID,
    ADD COLUMN IF NOT EXISTS updated_by UUID,
    ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 0;

-- product_images
ALTER TABLE product_images
    ADD COLUMN IF NOT EXISTS created_by UUID,
    ADD COLUMN IF NOT EXISTS updated_by UUID,
    ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 0;

-- bank_accounts
ALTER TABLE bank_accounts
    ADD COLUMN IF NOT EXISTS created_by UUID,
    ADD COLUMN IF NOT EXISTS updated_by UUID,
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 0;

-- tender_documents (missing created_by, updated_by, deleted, version)
ALTER TABLE tender_documents
    ADD COLUMN IF NOT EXISTS created_by UUID,
    ADD COLUMN IF NOT EXISTS updated_by UUID,
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 0;

-- refresh_tokens (missing audit columns)
ALTER TABLE refresh_tokens
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- chatbot_faq (missing updated_by, created_by)
ALTER TABLE chatbot_faq
    ADD COLUMN IF NOT EXISTS created_by UUID,
    ADD COLUMN IF NOT EXISTS updated_by UUID;
