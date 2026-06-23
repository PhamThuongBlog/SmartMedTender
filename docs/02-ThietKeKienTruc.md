# TÀI LIỆU THIẾT KẾ KIẾN TRÚC
## MedTender System V2.0

**Ngày:** 07/06/2026 | **Phiên bản:** 2.0.0 | **Trạng thái:** Production Ready

---

## Mục lục

1. [Kiến trúc tổng thể](#1-kiến-trúc-tổng-thể)
2. [Cấu trúc dự án](#2-cấu-trúc-dự-án)
3. [Thiết kế cơ sở dữ liệu](#3-thiết-kế-cơ-sở-dữ-liệu)
4. [Kiến trúc package Java](#4-kiến-trúc-package-java)
5. [Design Patterns](#5-design-patterns)
6. [Pipeline xử lý bất đồng bộ](#6-pipeline-xử-lý-bất-đồng-bộ)

---

## 1. Kiến trúc tổng thể

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                                  │
│  Browser (Chrome/Firefox/Edge)                                      │
│  Vue 3 SPA (Vite Dev Server :3000)                                  │
│  Nginx Reverse Proxy (Production) ─── HTTPS termination             │
└────────────────────────────┬────────────────────────────────────────┘
                             │ HTTP/REST (JSON)
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      APPLICATION LAYER                               │
│  Spring Boot 3.4.5 (:8082) — Embedded Tomcat                        │
│                                                                     │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │
│  │JwtFilter │→│RateLimit │→│  CORS    │→│Security  │→│Controller│ │
│  │(Bearer)  │ │(100/min) │ │(Config)  │ │(@PreAuth)│ │(@Rest)   │ │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └────┬─────┘ │
│                                                           │        │
│  ┌────────────────────────────────────────────────────────┘        │
│  │               Service Layer (@Service, @Transactional)           │
│  │  AuthService  EnterpriseService  ProductService  TenderService   │
│  │  HsmtUpload   ProcessingService  MatchingService  HSDTBuilder   │
│  │  ExportService  ExpiryAlertService  OCRService  AIService       │
│  └────────────────────────────────┬────────────────────────────────┘
│                                   │                                 │
│  ┌────────────────────────────────┼────────────────────────────────┐
│  │                    Repository Layer (Spring Data JPA)            │
│  │  25+ Repository interfaces — JPQL, Native Query, Pageable       │
│  └────────────────────────────────┼────────────────────────────────┘
└───────────────────────────────────┼─────────────────────────────────┘
                                    │
         ┌──────────────────────────┼──────────────────────────┐
         ▼                          ▼                          ▼
┌─────────────────┐    ┌─────────────────────┐    ┌──────────────────┐
│  DATA LAYER     │    │  MESSAGING LAYER     │    │  CACHE LAYER     │
│                 │    │                      │    │                  │
│ PostgreSQL 16   │    │ Apache Kafka         │    │ Redis            │
│ (Docker :5432)  │    │ (Docker :9092)       │    │ (Docker :6379)   │
│                 │    │                      │    │                  │
│ • 25+ tables    │    │ Topics:              │    │ • Session cache  │
│ • 30+ indexes   │    │ - hsmt-upload-topic  │    │ • Rate limit     │
│ • JSONB fields  │    │ - ocr-processing     │    │   (in-memory)    │
│ • UUID PKs      │    │ - ai-extraction      │    │                  │
│                 │    │ - notification        │    │                  │
│                 │    │ - retry-topic        │    │                  │
│                 │    │ - dlq-topic          │    │                  │
└─────────────────┘    └─────────────────────┘    └──────────────────┘
```

---

## 2. Cấu trúc dự án

```
SmartMedTender/
├── src/main/java/com/medbid/
│   ├── SmartMedTenderApplication.java    ← Entry point
│   ├── ai/                               ← AI Provider module
│   │   ├── controller/AIConfigController.java
│   │   ├── entity/AiLog.java
│   │   ├── provider/
│   │   │   ├── AIProvider.java           ← Interface (strategy)
│   │   │   ├── AIProviderFactory.java    ← Factory
│   │   │   ├── OpenAIProvider.java       ← OpenAI GPT-4o (ACTIVE)
│   │   │   ├── ClaudeProvider.java       ← Anthropic Claude (STUB)
│   │   │   └── GeminiProvider.java       ← Google Gemini (STUB)
│   │   ├── repository/AiLogRepository.java
│   │   └── service/AIService.java
│   ├── audit/                            ← Audit logging
│   │   ├── entity/AuditLog.java
│   │   ├── repository/AuditLogRepository.java
│   │   └── service/AuditService.java
│   ├── auth/                             ← Authentication & Authorization
│   │   ├── controller/AuthController.java
│   │   ├── dto/ (LoginRequest, LoginResponse, RegisterRequest, ...)
│   │   ├── entity/User.java, Role.java, Permission.java, ...
│   │   ├── mapper/UserMapper.java
│   │   ├── repository/ (UserRepo, RoleRepo, ...)
│   │   ├── security/
│   │   │   ├── SecurityConfig.java       ← Spring Security
│   │   │   ├── JwtFilter.java            ← OncePerRequestFilter
│   │   │   ├── JwtUtil.java              ← JWT token utils
│   │   │   └── CorsConfig.java
│   │   └── service/AuthService.java, UserService.java
│   ├── backup/                           ← Database backup
│   │   ├── controller/BackupController.java
│   │   └── service/BackupService.java
│   ├── common/base/BaseEntity.java       ← Base JPA entity
│   ├── config/                           ← App configs
│   ├── enterprise/                       ← Enterprise profile
│   │   ├── controller/EnterpriseController.java
│   │   ├── entity/EnterpriseProfile.java, LegalDocument.java, BankAccount.java
│   │   ├── dto/
│   │   ├── mapper/EnterpriseMapper.java
│   │   ├── repository/
│   │   └── service/
│   ├── export/                           ← Document export
│   │   ├── controller/ExportController.java
│   │   ├── entity/ExportHistory.java
│   │   ├── repository/
│   │   └── service/
│   │       ├── ExportService.java        ← Orchestrator
│   │       ├── WordExportService.java    ← Apache POI
│   │       ├── PdfExportService.java     ← iText 7
│   │       ├── ZipPackageService.java    ← ZIP builder
│   │       └── WatermarkEventHandler.java
│   ├── expiry/                           ← Expiry alerts
│   │   ├── controller/ExpiryAlertController.java
│   │   ├── dto/ExpiryAlertDto.java
│   │   ├── entity/ExpiryAlert.java
│   │   ├── repository/
│   │   └── service/ExpiryAlertService.java
│   ├── hsdt/                             ← HSDT Builder
│   │   ├── controller/HSDTController.java
│   │   ├── dto/HSDTPreviewResponse.java
│   │   └── service/HSDTBuilderService.java
│   ├── hsmt/                             ← HSMT Processing
│   │   ├── controller/HsmtController.java
│   │   ├── repository/
│   │   └── service/
│   │       ├── HsmtUploadService.java
│   │       ├── HsmtProcessingService.java
│   │       └── OCRReviewService.java
│   ├── matching/                         ← Product matching
│   │   ├── MatchingController.java
│   │   ├── MatchingService.java
│   │   ├── MatchDetail.java, MatchRequest.java, MatchResponse.java
│   │   ├── dto/ (GapAnalysisResponse, SmartMatchResponse, ...)
│   │   └── entity/MatchResult.java
│   ├── monitoring/                       ← Monitoring
│   │   ├── controller/AuditLogController.java
│   │   └── controller/HealthController.java
│   ├── notification/                     ← Notifications
│   ├── ocr/                              ← OCR module
│   │   ├── provider/
│   │   │   ├── OCRProvider.java          ← Interface (strategy)
│   │   │   ├── TesseractOCRProvider.java ← Tess4J (ACTIVE)
│   │   │   ├── GoogleVisionOCRProvider.java (STUB)
│   │   │   └── AzureOCRProvider.java     (STUB)
│   │   └── service/OCRService.java
│   ├── product/                          ← Product catalog
│   │   ├── controller/ProductController.java
│   │   ├── controller/DocumentLibraryController.java
│   │   ├── entity/Product.java, ProductDocument.java
│   │   ├── dto/
│   │   ├── mapper/ProductMapper.java
│   │   ├── repository/
│   │   └── service/DocumentLibraryService.java
│   ├── quotation/                        ← Pricing
│   │   ├── controller/QuotationController.java
│   │   ├── entity/Quotation.java, PriceHistory.java
│   │   ├── repository/
│   │   └── service/QuotationService.java
│   ├── security/                         ← Filters
│   │   └── filter/RateLimitFilter.java
│   ├── tender/                           ← Tender management
│   │   ├── controller/TenderController.java
│   │   ├── entity/Tender.java, TenderItem.java, TenderRequirement.java
│   │   ├── dto/
│   │   ├── repository/
│   │   └── service/TenderService.java
│   └── user/controller/UserController.java
├── src/main/resources/
│   ├── application.yml                   ← Main config
│   ├── application-dev.yml
│   ├── application-prod.yml
│   ├── db/migration/
│   │   ├── V1__init_schema.sql           ← Full schema (25+ tables)
│   │   ├── V2__add_audit_columns.sql
│   │   ├── V3__enhance_enterprise_profile.sql
│   │   ├── V4__expiry_alert_table.sql
│   │   ├── V5__seed_test_data.sql
│   │   └── V6__seed_price_history.sql
│   └── fonts/ (8 Arial font files)
├── frontend/
│   ├── src/
│   │   ├── api/client.js                 ← Axios instance
│   │   ├── router/index.js              ← Vue Router (18 routes)
│   │   ├── stores/auth.js               ← Pinia auth store
│   │   ├── composables/useApi.js
│   │   ├── layouts/MainLayout.vue
│   │   ├── components/ (AppHeader, AppSidebar, StatsCard, ...)
│   │   └── views/
│   │       ├── LoginView.vue
│   │       ├── DashboardView.vue
│   │       ├── ProductListView.vue / ProductDetailView.vue
│   │       ├── TenderListView.vue / TenderDetailView.vue / TenderFormView.vue
│   │       ├── HSMTUploadView.vue / OCRReviewView.vue
│   │       ├── TechnicalComparisonView.vue
│   │       ├── HSDTBuilderView.vue / ExportCenterView.vue
│   │       ├── EnterpriseSetupView.vue / DocumentLibraryView.vue
│   │       ├── ExpiryAlertView.vue / NotificationCenterView.vue
│   │       ├── UserManagementView.vue / SettingsView.vue
│   │       └── ChatbotView.vue
│   ├── package.json, vite.config.js
│   └── UAT_REPORT.md
├── docker-compose.yml (PostgreSQL + Kafka + Zookeeper + Redis)
└── docs/
```

---

## 3. Thiết kế cơ sở dữ liệu

### 3.1 Sơ đồ quan hệ (ERD)

```
┌──────────────────┐       ┌──────────────────┐
│      roles       │       │   permissions     │
│──────────────────│       │──────────────────│
│ id (UUID) PK     │───┐   │ id (UUID) PK     │
│ name              │   │   │ name              │
│ description       │   └───│ description       │
└────────┬─────────┘   ┌───│                  │
         │ 1:N          │   └──────────────────┘
         ▼              │
┌──────────────────┐    │   ┌──────────────────┐
│      users       │    │   │ role_permissions │
│──────────────────│    │   │──────────────────│
│ id (UUID) PK     │    │   │ role_id (FK)     │
│ username (UQ)    │    │   │ permission_id(FK)│
│ password (BCrypt)│    └───│                  │
│ email            │        └──────────────────┘
│ full_name        │
│ role_id (FK)     │────┐
│ enabled          │    │
│ account_locked   │    │   ┌──────────────────┐
│ failed_attempts  │    │   │  refresh_tokens   │
│ last_login_at    │    │   │──────────────────│
└────────┬─────────┘    │   │ id (UUID) PK     │
         │              │   │ user_id (FK)     │
         │              │   │ token (UQ)       │
         ▼              │   │ expires_at       │
┌──────────────────┐    │   │ revoked          │
│ enterprise_profiles│  │   └──────────────────┘
│──────────────────│    │
│ id (UUID) PK     │    │   ┌──────────────────┐
│ company_name     │    │   │   login_history   │
│ company_name_en  │    │   │──────────────────│
│ tax_code         │    │   │ id (UUID) PK     │
│ address          │    │   │ user_id (FK)     │
│ business_license │    │   │ username         │
│ legal_rep...     │    │   │ ip_address       │
└────────┬─────────┘    │   │ user_agent       │
         │ 1:N          │   │ success          │
         ▼              │   └──────────────────┘
┌──────────────────┐    │
│ legal_documents  │    │
│──────────────────│    │
│ id (UUID) PK     │    │
│ enterprise_id(FK)│    │
│ document_type    │    │
│ document_name    │    │
│ issue_date       │    │
│ expiry_date ★    │    │   ┌──────────────────┐      ┌──────────────────┐
└──────────────────┘    │   │     tenders      │      │   tender_items   │
                        │   │──────────────────│      │──────────────────│
┌──────────────────┐    │   │ id (UUID) PK     │──┐   │ id (UUID) PK     │
│    products      │    │   │ name             │  │   │ tender_id (FK)   │
│──────────────────│    │   │ bid_package_code │  └───│ item_number      │
│ id (UUID) PK     │    │   │ procuring_entity │      │ name             │
│ name             │    │   │ submission_dead  │      │ quantity         │
│ manufacturer     │    │   │ estimated_value  │      │ unit             │
│ brand, model     │    │   │ status           │      │ estimated_price  │
│ origin_country   │    │   │ cloned_from_id   │      └──────────────────┘
│ category         │    │   │ notes            │
│ technical_specs  │    │   └────────┬─────────┘      ┌──────────────────┐
│ registration_no  │    │          │ 1:N              │tender_requirements│
│ registration_exp │    │          ▼                  │──────────────────│
│ has_iso/ce/fda/  │    │   ┌──────────────────┐      │ id (UUID) PK     │
│   co_cq          │    │   │tender_documents  │      │ tender_id (FK)   │
│ status           │    │   │──────────────────│      │ description      │
└────────┬─────────┘    │   │ id (UUID) PK     │      │ type             │
         │ 1:N          │   │ tender_id (FK)   │      │ operator         │
         ▼              │   │ file_path        │      │ value            │
┌──────────────────┐    │   │ file_name        │      │ unit             │
│product_documents │    │   │ file_size        │      │ mandatory        │
│──────────────────│    │   │ ocr_status       │      │ priority         │
│ id (UUID) PK     │    │   │ page_count       │      │ status           │
│ product_id (FK)  │    │   └──────────────────┘      └──────────────────┘
│ document_type    │    │
│ document_name    │    │   ┌──────────────────┐      ┌──────────────────┐
│ expiry_date ★    │    │   │  match_results   │      │   expiry_alerts  │
└──────────────────┘    │   │──────────────────│      │──────────────────│
                        │   │ id (UUID) PK     │      │ id (UUID) PK     │
┌──────────────────┐    │   │ tender_id (FK)   │      │ alert_type       │
│  price_history   │    │   │ requirement_id   │      │ reference_type   │
│──────────────────│    │   │ product_id (FK)  │      │ reference_id     │
│ id (UUID) PK     │    │   │ passed           │      │ title            │
│ product_id (FK)  │    │   │ score            │      │ message          │
│ price            │    │   │ is_manual_override│     │ days_remaining   │
│ price_type       │    │   │ override_reason  │      │ severity          │
│ recorded_date    │    │   └──────────────────┘      └──────────────────┘
│ source           │    │
└──────────────────┘    │   ┌──────────────────┐      ┌──────────────────┐
                        │   │   quotations     │      │    audit_logs    │
┌──────────────────┐    │   │──────────────────│      │──────────────────│
│ export_histories │    │   │ id (UUID) PK     │      │ id (UUID) PK     │
│──────────────────│    │   │ tender_id (FK)   │      │ user_id          │
│ id (UUID) PK     │    │   │ product_id (FK)  │      │ action           │
│ tender_id (FK)   │    │   │ selling_price    │      │ entity_type      │
│ export_type      │    │   │ winning_price    │      │ entity_id        │
│ file_format      │    │   │ bid_date         │      │ old_value (JSONB)│
│ file_size        │    │   │ is_winning       │      │ new_value (JSONB)│
│ status           │    │   └──────────────────┘      │ ip_address       │
└──────────────────┘    │                            └──────────────────┘
```

### 3.2 Các bảng chính — 25+ tables, 30+ indexes

| Nhóm | Bảng | Mục đích |
|------|------|----------|
| Auth | users, roles, permissions, role_permissions, refresh_tokens, login_history | Xác thực & phân quyền |
| Enterprise | enterprise_profiles, legal_documents, bank_accounts | Hồ sơ doanh nghiệp |
| Product | products, product_documents, product_images | Thư viện sản phẩm & chứng chỉ |
| Tender | tenders, tender_items, tender_documents, tender_requirements | Gói thầu & HSMT |
| Matching | match_results | Kết quả đối chiếu |
| Quotation | quotations, price_history | Báo giá & lịch sử giá |
| Export | export_histories | Lịch sử xuất file |
| Expiry | expiry_alerts | Cảnh báo hết hạn |
| Audit | audit_logs | Nhật ký thao tác |
| AI/OCR | ai_logs, ocr_logs | Log xử lý AI/OCR |
| Others | notifications, chatbot_faq, backup_histories | Thông báo, FAQ, backup |

### 3.3 Đặc điểm thiết kế

- **UUID primary keys** — bảo mật, phân tán, không đoán được ID
- **Soft delete** — `deleted BOOLEAN DEFAULT FALSE` trên hầu hết bảng
- **Optimistic locking** — `version INT` trên BaseEntity (@Version)
- **JSONB** — `technical_specs`, `old_value`, `new_value`
- **Audit columns** — `created_at`, `updated_at`, `created_by`, `updated_by`
- **Flyway migration** — 6 scripts, version-controlled schema

---

## 4. Kiến trúc package Java

```
com.medbid
├── SmartMedTenderApplication.java        ← @SpringBootApplication
├── common/
│   ├── base/BaseEntity.java              ← @MappedSuperclass (UUID, timestamps, version)
│   └── constant/AppConstants.java        ← Role names, status enums, Kafka topics
├── config/                               ← @Configuration classes
│   ├── JpaConfig.java, AsyncConfig.java, RedisConfig.java
│   ├── OpenApiConfig.java, FileStorageConfig.java
└── {domain}/                              ← Domain-driven packages
    ├── controller/                        ← @RestController
    ├── service/                           ← @Service + @Transactional
    ├── repository/                        ← Spring Data JPA interfaces
    ├── entity/                            ← @Entity classes
    ├── dto/                               ← Request/Response records
    └── mapper/                            ← MapStruct interfaces
```

**Quy tắc:** Mỗi domain package có đầy đủ controller → service → repository → entity → dto. Không có circular dependency.

---

## 5. Design Patterns

| Pattern | Áp dụng | File |
|---------|---------|------|
| **Strategy** | AI Provider (OpenAI/Claude/Gemini) | AIProvider.java + Factory |
| **Strategy** | OCR Provider (Tesseract/Google/Azure) | OCRProvider.java + Factory |
| **Template Method** | PDF/Word generation (shared structure) | PdfExportService, WordExportService |
| **Builder** | ZIP package assembly | ZipPackageService.java |
| **Facade** | HSDT export orchestration | ExportService.java |
| **Factory** | Provider selection | AIProviderFactory, OCRProviderFactory |
| **Observer** | Kafka consumers | HsmtUploadConsumer, OcrProcessingConsumer |
| **Singleton** | Spring beans | @Service, @Repository |
| **DTO/Record** | API contracts | 36+ record classes |
| **Repository** | Data access | 26+ Spring Data JPA interfaces |

---

## 6. Pipeline xử lý bất đồng bộ

```
POST /api/hsmt/upload
    │
    ▼
HsmtUploadService
    ├── Validate file (MIME, size)
    ├── Save to disk ({uploadDir}/{tenderId}/{UUID}.ext)
    ├── Create TenderDocument (PENDING)
    ├── Synchronous processing
    │   ├── OCRService.processFile() → @Async + @Retryable
    │   │   └── TesseractOCRProvider.extractText()
    │   └── AIService.extractRequirements() → @Async + @Retryable
    │       └── OpenAIProvider.extractRequirements()
    │           ├── API call → GPT-4o
    │           └── Fallback: Rule-based parser
    └── Kafka message → hsmt-upload-topic (retry/audit)

Kafka Consumers (background):
    ├── HsmtUploadConsumer → hsmt-upload-topic → DLQ on failure
    ├── OcrProcessingConsumer → ocr-processing-topic
    ├── AiExtractionConsumer → ai-extraction-topic
    └── NotificationConsumer → notification-topic
```

**Status transitions:**
```
TenderDocument: PENDING → PROCESSING → OCR_COMPLETED → AI_EXTRACTING → COMPLETED
                                                                      → FAILED
TenderRequirement: EXTRACTED → VERIFIED → MATCHED
                            → REJECTED
Tender: DRAFT → SUBMITTED → WON/LOST
                           → CANCELED
```
