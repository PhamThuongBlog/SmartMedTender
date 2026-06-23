# Database Schema Summary

**Source:** `V1__init_schema.sql` (Flyway migration)
**Verified:** 26 `CREATE TABLE` statements
**Date:** June 2026

---

## Table Inventory (10 Functional Groups)

### Group 1: Authentication (6 tables)

| # | Table | Primary Key | Foreign Keys | Description |
|---|-------|-------------|--------------|-------------|
| 1 | `users` | UUID | role_id → roles | User accounts with credentials |
| 2 | `roles` | UUID | — | 7 predefined roles |
| 3 | `permissions` | UUID | — | 8 granular permissions |
| 4 | `role_permissions` | composite (role_id, permission_id) | roles, permissions | Many-to-many join |
| 5 | `refresh_tokens` | UUID | user_id → users | JWT refresh token store |
| 6 | `login_history` | UUID | user_id → users | Login attempt audit trail |

### Group 2: Enterprise (3 tables)

| # | Table | Primary Key | Foreign Keys | Description |
|---|-------|-------------|--------------|-------------|
| 7 | `enterprise_profiles` | UUID | — | Company legal info, tax code |
| 8 | `legal_documents` | UUID | enterprise_id → enterprise_profiles | Business license, ISO 9001, GMP |
| 9 | `bank_accounts` | UUID | enterprise_id → enterprise_profiles | Banking information |

### Group 3: Product Catalog (3 tables)

| # | Table | Primary Key | Foreign Keys | Description |
|---|-------|-------------|--------------|-------------|
| 10 | `products` | UUID | — | Medical device catalog, JSONB tech specs |
| 11 | `product_documents` | UUID | product_id → products | ISO/CE/FDA/CO/CQ certificates |
| 12 | `product_images` | UUID | product_id → products | Product image storage paths |

### Group 4: Tender Management (4 tables)

| # | Table | Primary Key | Foreign Keys | Description |
|---|-------|-------------|--------------|-------------|
| 13 | `tenders` | UUID | — | Bid packages with status workflow |
| 14 | `tender_items` | UUID | tender_id → tenders | Individual line items |
| 15 | `tender_documents` | UUID | tender_id → tenders | Uploaded HSMT files, OCR status |
| 16 | `tender_requirements` | UUID | tender_id → tenders | AI-extracted or manually entered |

### Group 5: Matching & Pricing (3 tables)

| # | Table | Primary Key | Foreign Keys | Description |
|---|-------|-------------|--------------|-------------|
| 17 | `match_results` | UUID | tender_id → tenders, product_id → products | Match scores and compliance |
| 18 | `quotations` | UUID | tender_id → tenders, product_id → products | Bid pricing |
| 19 | `price_history` | UUID | product_id → products | Historical winning/market prices |

### Group 6: Export, Audit & Infrastructure (7 tables)

| # | Table | Primary Key | Foreign Keys | Description |
|---|-------|-------------|--------------|-------------|
| 20 | `export_histories` | UUID | tender_id → tenders | Export job tracking |
| 21 | `expiry_alerts` | UUID | — | Certification/document expiry |
| 22 | `audit_logs` | UUID | user_id → users | CRUD audit with JSONB snapshots |
| 23 | `notifications` | UUID | user_id → users | In-app notification system |
| 24 | `chatbot_faq` | UUID | — | FAQ knowledge base |
| 25 | `ai_logs` | UUID | — | AI provider call logging |
| 26 | `ocr_logs` | UUID | — | OCR processing logging |

---

## Common Schema Patterns

All tables inherit from `BaseEntity` mapped superclass:
- `id` — UUID v4 primary key
- `deleted` — boolean (soft delete)
- `created_at` — timestamp
- `updated_at` — timestamp
- `created_by` — varchar(255)
- `updated_by` — varchar(255)
- `version` — integer (@Version for optimistic locking)

## RBAC Roles (7 roles)

| ID | Name | Description |
|----|------|-------------|
| 000...000100 | SUPER_ADMIN | Full system access |
| 000...000200 | ADMIN | Enterprise-level admin |
| 000...000003 | MANAGER | Tender management |
| 000...000004 | STAFF | Data entry, document upload |
| 000...000005 | REVIEWER | Requirement review |
| 000...000006 | LEGAL | Legal document verification |
| 000...000007 | SALES | Quotation and pricing |

## RBAC Permissions (8 permissions)

| Name | Applies To |
|------|-----------|
| VIEW | All entities |
| CREATE | Products, tenders, documents |
| UPDATE | Products, enterprise, tenders |
| DELETE | Products, tenders (soft delete) |
| EXPORT | HSDT export (Word, PDF, Excel, ZIP) |
| APPROVE | Requirements, match results |
| UPLOAD | Documents, images |
| CONFIGURE | System settings, AI providers |
