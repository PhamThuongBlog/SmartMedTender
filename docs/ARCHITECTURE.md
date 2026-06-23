# Tài Liệu Kiến Trúc Hệ Thống

## Kiến Trúc Tổng Thể

```
┌─────────────────────────────────────────────────┐
│                   Nginx (Port 80)                │
│  ├─ Static files (VueJS frontend)               │
│  └─ Reverse proxy → Backend (Port 8082)         │
├─────────────────────────────────────────────────┤
│              Spring Boot Backend                 │
│  ┌──────────────────────────────────────────┐   │
│  │  Controllers (REST API)                   │   │
│  │  ├─ Auth: /api/auth/*                     │   │
│  │  ├─ Tenders: /api/tenders/*               │   │
│  │  ├─ Products: /api/products/*             │   │
│  │  ├─ HSMT: /api/hsmt/*                     │   │
│  │  ├─ Match: /api/match/*                   │   │
│  │  ├─ Export: /api/export/*                 │   │
│  │  ├─ Users: /api/users/*                   │   │
│  │  ├─ Chatbot: /api/chatbot/*               │   │
│  │  ├─ Dashboard: /api/dashboard/*           │   │
│  │  └─ Notifications: /api/notifications/*   │   │
│  ├──────────────────────────────────────────┤   │
│  │  Services (Business Logic)                │   │
│  │  ├─ AuthService, UserService              │   │
│  │  ├─ TenderService, MatchingService        │   │
│  │  ├─ ProductService, QuotationService      │   │
│  │  ├─ ExportService, OCRService, AIService  │   │
│  │  └─ NotificationService, AuditService     │   │
│  ├──────────────────────────────────────────┤   │
│  │  Repositories (Spring Data JPA)           │   │
│  └──────────────────────────────────────────┘   │
├─────────────────────────────────────────────────┤
│              Infrastructure                      │
│  ┌──────────┐ ┌───────┐ ┌───────┐ ┌─────────┐  │
│  │PostgreSQL│ │ Kafka │ │ Redis │ │ File    │  │
│  │   :5432  │ │ :9092 │ │ :6379 │ │ Storage │  │
│  └──────────┘ └───────┘ └───────┘ └─────────┘  │
└─────────────────────────────────────────────────┘
```

## Mô Hình Kiến Trúc

### Layered Architecture

```
Controller → Service → Repository → Entity → Database
    ↓           ↓
   DTO        Mapper (MapStruct)
```

### Design Patterns

- **Strategy Pattern**: OCRProvider, AIProvider — dễ dàng đổi implementation
- **Factory Pattern**: OCRProviderFactory, AIProviderFactory — tự động chọn provider
- **Facade Pattern**: HSDTExportFacadeService — orchestration xuất HSDT
- **Builder Pattern**: PackageBuilderService — xây dựng ZIP package
- **Template Method**: Word/PdfExportService — template cho xuất tài liệu

### Event-Driven Architecture (Kafka)

```
Upload HSMT → hsmt-upload-topic → OCR Consumer → ocr-processing-topic
    → AI Consumer → ai-extraction-topic → Save to DB → notification-topic
```

## Bảo Mật

- **Authentication**: JWT (access token 24h, refresh token 7d)
- **Authorization**: RBAC với 7 roles + permissions
- **Password**: BCrypt strength 10
- **API Security**: Rate limiting (100 req/min/IP), secure headers
- **File Security**: MIME type validation, file extension whitelist, size limit 50MB

## Database

- **Primary Key**: UUID v4 cho tất cả bảng
- **Soft Delete**: deleted flag trên hầu hết entities
- **Optimistic Locking**: @Version field
- **Audit Trail**: created_at, updated_at, created_by, updated_by
- **Migration**: Flyway (V1__init_schema.sql)
- **JSONB**: Technical specs lưu dạng JSONB trên PostgreSQL
