-- ============================================
-- V1: Initial Schema - MedTender System V2
-- ============================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- AUTH & USER MANAGEMENT
-- ============================================

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id),
    permission_id UUID NOT NULL REFERENCES permissions(id),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    full_name VARCHAR(255),
    phone VARCHAR(20),
    avatar_url VARCHAR(500),
    role_id UUID REFERENCES roles(id),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    failed_attempts INT NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP,
    password_changed_at TIMESTAMP,
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_secret VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version INT NOT NULL DEFAULT 0
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE login_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    username VARCHAR(100),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    success BOOLEAN NOT NULL,
    failure_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================
-- ENTERPRISE PROFILE
-- ============================================

CREATE TABLE enterprise_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_name VARCHAR(500) NOT NULL,
    tax_code VARCHAR(20),
    address TEXT,
    phone VARCHAR(20),
    email VARCHAR(255),
    website VARCHAR(500),
    legal_representative VARCHAR(255),
    established_date DATE,
    business_license_number VARCHAR(100),
    business_license_issue_date DATE,
    business_license_expiry_date DATE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version INT NOT NULL DEFAULT 0
);

CREATE TABLE legal_documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    enterprise_id UUID NOT NULL REFERENCES enterprise_profiles(id),
    document_type VARCHAR(100) NOT NULL,
    document_name VARCHAR(500) NOT NULL,
    file_path VARCHAR(1000),
    file_name VARCHAR(500),
    file_size BIGINT,
    issue_date DATE,
    expiry_date DATE,
    issuing_authority VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    notes TEXT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version INT NOT NULL DEFAULT 0
);

CREATE TABLE bank_accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    enterprise_id UUID NOT NULL REFERENCES enterprise_profiles(id),
    bank_name VARCHAR(255) NOT NULL,
    branch VARCHAR(255),
    account_number VARCHAR(50) NOT NULL,
    account_holder VARCHAR(255) NOT NULL,
    swift_code VARCHAR(20),
    currency VARCHAR(3) DEFAULT 'VND',
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- ============================================
-- MEDICAL DEVICE LIBRARY
-- ============================================

CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(500) NOT NULL,
    manufacturer VARCHAR(255),
    brand VARCHAR(255),
    model VARCHAR(255),
    origin_country VARCHAR(100),
    category VARCHAR(255),
    description TEXT,
    technical_specs JSONB,
    registration_number VARCHAR(100),
    registration_issue_date DATE,
    registration_expiry_date DATE,
    has_iso BOOLEAN NOT NULL DEFAULT FALSE,
    has_fda BOOLEAN NOT NULL DEFAULT FALSE,
    has_ce BOOLEAN NOT NULL DEFAULT FALSE,
    has_co_cq BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version INT NOT NULL DEFAULT 0
);

CREATE TABLE product_documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(id),
    document_type VARCHAR(100) NOT NULL,
    document_name VARCHAR(500) NOT NULL,
    file_path VARCHAR(1000),
    file_name VARCHAR(500),
    file_size BIGINT,
    issue_date DATE,
    expiry_date DATE,
    notes TEXT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE TABLE product_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(id),
    file_path VARCHAR(1000) NOT NULL,
    file_name VARCHAR(500),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================
-- TENDER / BID PACKAGE MANAGEMENT
-- ============================================

CREATE TABLE tenders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(500) NOT NULL,
    description TEXT,
    bid_package_code VARCHAR(100),
    procuring_entity VARCHAR(500),
    submission_deadline TIMESTAMP,
    opening_date TIMESTAMP,
    estimated_value DECIMAL(18,2),
    currency VARCHAR(3) DEFAULT 'VND',
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    cloned_from_id UUID REFERENCES tenders(id),
    notes TEXT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version INT NOT NULL DEFAULT 0
);

CREATE TABLE tender_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tender_id UUID NOT NULL REFERENCES tenders(id),
    item_number INT NOT NULL,
    name VARCHAR(500) NOT NULL,
    description TEXT,
    quantity DECIMAL(18,2),
    unit VARCHAR(50),
    estimated_price DECIMAL(18,2),
    notes TEXT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE TABLE tender_documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tender_id UUID NOT NULL REFERENCES tenders(id),
    document_type VARCHAR(100) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    file_name VARCHAR(500),
    file_size BIGINT,
    page_count INT,
    ocr_status VARCHAR(50) DEFAULT 'PENDING',
    uploaded_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE tender_requirements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tender_id UUID NOT NULL REFERENCES tenders(id),
    description TEXT NOT NULL,
    type VARCHAR(100),
    operator VARCHAR(20),
    value VARCHAR(500),
    unit VARCHAR(50),
    mandatory BOOLEAN NOT NULL DEFAULT TRUE,
    priority INT NOT NULL DEFAULT 3,
    source VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
    source_document_id UUID REFERENCES tender_documents(id),
    confidence_score DOUBLE PRECISION,
    status VARCHAR(50) NOT NULL DEFAULT 'EXTRACTED',
    version INT NOT NULL DEFAULT 1,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

-- ============================================
-- MATCHING
-- ============================================

CREATE TABLE match_results (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tender_id UUID NOT NULL REFERENCES tenders(id),
    tender_requirement_id UUID NOT NULL REFERENCES tender_requirements(id),
    product_id UUID REFERENCES products(id),
    passed BOOLEAN NOT NULL DEFAULT FALSE,
    missing_criteria TEXT,
    score DOUBLE PRECISION,
    is_manual_override BOOLEAN NOT NULL DEFAULT FALSE,
    override_reason TEXT,
    override_by UUID,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- ============================================
-- QUOTATION / PRICING
-- ============================================

CREATE TABLE quotations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tender_id UUID NOT NULL REFERENCES tenders(id),
    product_id UUID NOT NULL REFERENCES products(id),
    import_price DECIMAL(18,2),
    selling_price DECIMAL(18,2),
    winning_price DECIMAL(18,2),
    bid_date DATE,
    is_winning BOOLEAN NOT NULL DEFAULT FALSE,
    source VARCHAR(255),
    notes TEXT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version INT NOT NULL DEFAULT 0
);

CREATE TABLE price_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(id),
    price DECIMAL(18,2) NOT NULL,
    price_type VARCHAR(50) NOT NULL,
    recorded_date DATE NOT NULL,
    source VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================
-- HSDT EXPORT
-- ============================================

CREATE TABLE export_histories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tender_id UUID NOT NULL REFERENCES tenders(id),
    export_type VARCHAR(50) NOT NULL,
    file_format VARCHAR(20) NOT NULL,
    file_path VARCHAR(1000),
    file_size BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'PROCESSING',
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID,
    completed_at TIMESTAMP
);

-- ============================================
-- NOTIFICATIONS
-- ============================================

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    title VARCHAR(500) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    link VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================
-- CHATBOT FAQ
-- ============================================

CREATE TABLE chatbot_faq (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    category VARCHAR(255),
    keywords TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- ============================================
-- AUDIT LOGS
-- ============================================

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID,
    username VARCHAR(100),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id VARCHAR(100),
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================
-- AI / OCR LOGS
-- ============================================

CREATE TABLE ai_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    provider VARCHAR(50) NOT NULL,
    model VARCHAR(100),
    request_type VARCHAR(100) NOT NULL,
    request_prompt TEXT,
    response_text TEXT,
    tokens_used INT,
    latency_ms BIGINT,
    cost DECIMAL(10,6),
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE ocr_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    provider VARCHAR(50) NOT NULL,
    source_file VARCHAR(1000),
    result_text TEXT,
    confidence DOUBLE PRECISION,
    processing_time_ms BIGINT,
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================
-- BACKUP
-- ============================================

CREATE TABLE backup_histories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    backup_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(1000),
    file_size BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    error_message TEXT,
    started_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP,
    created_by UUID
);

-- ============================================
-- INDEXES
-- ============================================

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role_id);
CREATE INDEX idx_users_deleted ON users(deleted);

CREATE INDEX idx_tenders_status ON tenders(status);
CREATE INDEX idx_tenders_deadline ON tenders(submission_deadline);
CREATE INDEX idx_tenders_deleted ON tenders(deleted);

CREATE INDEX idx_tender_requirements_tender ON tender_requirements(tender_id);
CREATE INDEX idx_tender_requirements_status ON tender_requirements(status);

CREATE INDEX idx_products_manufacturer ON products(manufacturer);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_deleted ON products(deleted);

CREATE INDEX idx_legal_documents_enterprise ON legal_documents(enterprise_id);
CREATE INDEX idx_legal_documents_expiry ON legal_documents(expiry_date);

CREATE INDEX idx_quotations_tender ON quotations(tender_id);
CREATE INDEX idx_quotations_product ON quotations(product_id);

CREATE INDEX idx_match_results_tender ON match_results(tender_id);
CREATE INDEX idx_match_results_product ON match_results(product_id);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(is_read);

CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at);

CREATE INDEX idx_ai_logs_provider ON ai_logs(provider);
CREATE INDEX idx_ai_logs_created ON ai_logs(created_at);

CREATE INDEX idx_login_history_user ON login_history(user_id);
CREATE INDEX idx_login_history_created ON login_history(created_at);

-- ============================================
-- DEFAULT DATA
-- ============================================

INSERT INTO roles (id, name, description) VALUES
    ('00000000-0000-0000-0000-000000000001', 'SUPER_ADMIN', 'Quản trị viên cao nhất - toàn quyền hệ thống'),
    ('00000000-0000-0000-0000-000000000002', 'ADMIN', 'Quản trị viên hệ thống'),
    ('00000000-0000-0000-0000-000000000003', 'MANAGER', 'Quản lý nghiệp vụ'),
    ('00000000-0000-0000-0000-000000000004', 'STAFF', 'Nhân viên xử lý hồ sơ'),
    ('00000000-0000-0000-0000-000000000005', 'REVIEWER', 'Người kiểm duyệt hồ sơ'),
    ('00000000-0000-0000-0000-000000000006', 'LEGAL', 'Chuyên viên pháp lý'),
    ('00000000-0000-0000-0000-000000000007', 'SALES', 'Nhân viên kinh doanh');

INSERT INTO permissions (id, name, description) VALUES
    ('00000000-0000-0000-0000-000000000011', 'VIEW', 'Xem dữ liệu'),
    ('00000000-0000-0000-0000-000000000012', 'CREATE', 'Tạo mới dữ liệu'),
    ('00000000-0000-0000-0000-000000000013', 'UPDATE', 'Cập nhật dữ liệu'),
    ('00000000-0000-0000-0000-000000000014', 'DELETE', 'Xóa dữ liệu'),
    ('00000000-0000-0000-0000-000000000015', 'EXPORT', 'Xuất báo cáo'),
    ('00000000-0000-0000-0000-000000000016', 'APPROVE', 'Phê duyệt'),
    ('00000000-0000-0000-0000-000000000017', 'UPLOAD', 'Tải lên tài liệu'),
    ('00000000-0000-0000-0000-000000000018', 'CONFIGURE', 'Cấu hình hệ thống');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'SUPER_ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN' AND p.name IN ('VIEW', 'CREATE', 'UPDATE', 'DELETE', 'EXPORT', 'APPROVE', 'UPLOAD', 'CONFIGURE');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'MANAGER' AND p.name IN ('VIEW', 'CREATE', 'UPDATE', 'EXPORT', 'APPROVE', 'UPLOAD');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'STAFF' AND p.name IN ('VIEW', 'CREATE', 'UPDATE', 'EXPORT', 'UPLOAD');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'REVIEWER' AND p.name IN ('VIEW', 'APPROVE', 'EXPORT');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'LEGAL' AND p.name IN ('VIEW', 'UPDATE', 'APPROVE', 'EXPORT');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'SALES' AND p.name IN ('VIEW', 'CREATE', 'EXPORT', 'UPLOAD');

-- Default admin user (password: 12345678@Abc, BCrypt hashed)
INSERT INTO users (id, username, password, email, full_name, role_id, enabled, account_locked)
VALUES (
    '00000000-0000-0000-0000-000000000100',
    'admin',
    '$2b$10$ZyQIZ53zKQrpewV9yiiG0uj7Fb5.Sh4keycTXbNezS4neqL.UQGaO',
    'admin@medtender.vn',
    'System Administrator',
    '00000000-0000-0000-0000-000000000001',
    TRUE,
    FALSE
);

-- Sample FAQ entries
INSERT INTO chatbot_faq (question, answer, category, keywords, sort_order) VALUES
    ('Làm thế nào để upload hồ sơ mời thầu?', 'Vào menu "HSMT" → "Upload HSMT", chọn file PDF/DOCX (tối đa 50MB) và nhấn "Tải lên". Hệ thống sẽ tự động OCR và trích xuất yêu cầu kỹ thuật.', 'HSMT', 'upload, tải lên, hồ sơ mời thầu', 1),
    ('Làm thế nào để tạo hồ sơ dự thầu?', 'Vào menu "HSDT" → "Tạo HSDT", chọn gói thầu cần dự thầu. Hệ thống sẽ tự động đối chiếu sản phẩm và sinh bộ hồ sơ dự thầu hoàn chỉnh.', 'HSDT', 'tạo, hồ sơ dự thầu, hsdt', 2),
    ('Hệ thống hỗ trợ những định dạng file nào?', 'Hệ thống hỗ trợ upload các định dạng: PDF, DOCX, DOC, XLSX, XLS, ZIP, PNG, JPG, JPEG. Dung lượng tối đa mỗi file là 50MB.', 'CHUNG', 'định dạng, file, format, pdf, docx', 3),
    ('Quên mật khẩu phải làm sao?', 'Vui lòng liên hệ quản trị viên hệ thống để được cấp lại mật khẩu. Email: admin@medtender.vn', 'TAI_KHOAN', 'quên mật khẩu, reset, password', 4),
    ('Làm thế nào để xem lịch sử giá trúng thầu?', 'Vào menu "Báo giá" → "Lịch sử giá", tìm kiếm theo thiết bị hoặc gói thầu. Hệ thống hiển thị biểu đồ giá theo thời gian và gợi ý giá chào.', 'BAO_GIA', 'giá, lịch sử, trúng thầu, báo giá', 5),
    ('Hệ thống có hỗ trợ xuất file Word/PDF không?', 'Có. Hệ thống hỗ trợ xuất HSDT ra định dạng Word (.docx), PDF, và ZIP (bao gồm cả Word + PDF + metadata). Định dạng phù hợp với chuẩn hành chính Việt Nam.', 'XUAT', 'xuất, export, word, pdf, zip', 6);
