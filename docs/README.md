# MEDTENDER SYSTEM V2.0
## Hệ thống quản lý hồ sơ dự thầu thiết bị y tế

**Phiên bản:** 2.0.0-SNAPSHOT | **Ngày:** 07/06/2026 | **Trạng thái:** ✅ PRODUCTION READY

---

## 🚀 Quick Start

```bash
# 1. Start infrastructure
docker compose up -d

# 2. Start backend
./mvnw spring-boot:run

# 3. Start frontend
cd frontend && npm install && npm run dev

# 4. Open browser
# URL: http://localhost:3000
# Username: admin
# Password: 12345678@Abc
```

---

## 📊 System Overview

```
STATUS: All 85/85 UAT tests passed (100%)
API Response: Avg < 15ms, Max < 500ms
Database: 22 products, 31 tenders, 11 users, 10 documents
Migrations: 6 Flyway scripts
Stack: Java 17 · Spring Boot 3.4.5 · PostgreSQL 16 · Vue 3 · Kafka · Redis
```

---

## 📚 Tài liệu

| # | Tài liệu | Mô tả |
|---|----------|-------|
| 1 | [Phân tích hệ thống](docs/01-PhanTichHeThong.md) | Tổng quan, phạm vi, actors, yêu cầu chức năng/phi chức năng, use case |
| 2 | [Thiết kế kiến trúc](docs/02-ThietKeKienTruc.md) | Kiến trúc 3-tier, cấu trúc dự án, ERD, design patterns, pipeline |
| 3 | [Đặc tả API](docs/03-DacTaAPI.md) | 120+ REST endpoints, request/response, Swagger |
| 4 | [Triển khai & Đóng gói](docs/04-HuongDanTrienKhai.md) | Docker, Nginx HTTPS, build script, systemd |
| 5 | [Vận hành](docs/05-HuongDanVanHanh.md) | Backup/restore, monitoring, xử lý sự cố, SLA |
| 6 | [Hướng dẫn sử dụng](docs/06-HuongDanSuDung.md) | User manual cho tất cả 7 nhóm tính năng |

---

## 🏗️ Kiến trúc

```
Vue 3 SPA (:3000) ──→ Nginx (HTTPS) ──→ Spring Boot (:8082)
                                              ├── PostgreSQL 16 (:5432)
                                              ├── Kafka (:9092) — async pipeline
                                              ├── Redis (:6379) — cache
                                              ├── Tesseract OCR — text extraction
                                              └── OpenAI GPT-4o — requirement parsing
```

---

## 🎯 7 Nhóm tính năng

| # | Nhóm | Key endpoints |
|---|------|---------------|
| 1 | **Thiết lập ban đầu** | `/api/enterprise`, `/api/products`, `/api/documents`, `/api/expiry` |
| 2 | **Đọc HSMT** | `/api/hsmt/upload`, `/api/hsmt/{id}/requirements` |
| 3 | **Đối chiếu thông minh** | `/api/match/smart-suggest`, `/api/match/gap-analysis` |
| 4 | **Tạo HSDT** | `/api/hsdt/preview`, `/api/hsdt/export/{word/pdf/zip/excel}` |
| 5 | **Lịch sử & Tái SD** | `/api/tenders/clone`, `/api/tenders/outcome`, `/api/tenders/history` |
| 6 | **Quản lý TK** | `/api/users`, `/api/auth` (JWT + 7 roles) |
| 7 | **Bảo mật & HT** | `/api/audit`, `/api/backup/sla`, `/api/admin/ai-config` |

---

## 📋 Test Results (UAT)

```
API Tests:     61/61 PASS (100%)
UI Tests:      17/17 PASS (100%)
Performance:    7/7  PASS (100%)
────────────────────────────────
TOTAL:         85/85 PASS (100%)
```

Xem báo cáo chi tiết: `frontend/UAT_REPORT.md`

---

## ⚙️ Cấu hình môi trường

```bash
# Required
export JWT_SECRET="your-256-bit-secret-key-change-in-production"
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/medbid_db
export SPRING_DATASOURCE_USERNAME=medbid
export SPRING_DATASOURCE_PASSWORD=your_password

# Optional (for AI features)
export OPENAI_API_KEY="sk-your-key"

# Backup
export BACKUP_DIR=/var/medtender/backups
export BACKUP_RTO=60
export BACKUP_RPO=1440
```
