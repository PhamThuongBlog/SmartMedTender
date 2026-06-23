# 🏥 SmartMedTender V2

**AI-Powered Medical Tender Management System**

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3-4FC08D.svg)](https://vuejs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Kafka](https://img.shields.io/badge/Kafka-3.x-231F20.svg)](https://kafka.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> **Hệ thống Quản lý và Chuẩn bị Hồ sơ Dự thầu Thiết bị Y tế Thông minh**
>
> Automates the entire medical equipment bidding process in Vietnam — from tender document OCR to AI-powered requirement extraction, intelligent product matching, and automated bid dossier generation.

---

## ✨ Key Features

| Module | Description |
|--------|-------------|
| 📄 **HSMT Processing** | Upload, OCR, and AI-based requirement extraction from tender documents |
| 🔬 **Product Matching** | Intelligent matching of medical equipment against tender requirements |
| 📊 **Quotation Management** | Price quotation management and comparison |
| 📝 **Bid Dossier Export** | Automated generation of Word, PDF, and ZIP bid packages |
| 🤖 **AI Chatbot** | Built-in assistant for tender-related queries |
| 📈 **Dashboard** | Real-time analytics, reports, and statistics |
| 🔔 **Notifications** | Real-time alerts on tender status changes |
| 🔒 **Auth & Security** | JWT-based authentication with role-based access control |
| 💾 **Backup & Restore** | Automated database backup and restore scripts |

---

## 🛠️ Tech Stack

### Backend
- **Java 17** + **Spring Boot 4.0.6**
- **Spring Security** + JWT (jjwt 0.11.5)
- **Spring Data JPA** (Hibernate) + PostgreSQL 16
- **Apache Kafka** — async OCR/AI processing pipeline
- **Apache POI 5.2.5** — Word (.docx) generation
- **iText 7.1.17** — PDF generation
- **Lombok** — boilerplate reduction

### Frontend
- **Vue.js 3** + **Vite** + **PrimeVue**
- Responsive SPA with modern UI components

### Infrastructure
- **Docker** + **Docker Compose** — full-stack containerization
- **Nginx** — reverse proxy
- **Redis 7** — caching layer
- **Prometheus + Grafana** — monitoring (optional)

---

## 🚀 Quick Start

### Prerequisites
- JDK 17+
- Docker & Docker Compose
- Maven 3.9+ (or use the included Maven Wrapper)
- Node.js 20+ (for frontend development)

### 1. Clone & Start Infrastructure

```bash
git clone https://github.com/PhamThuongBlog/SmartMedTender.git
cd SmartMedTender

# Start PostgreSQL, Zookeeper, Kafka, Redis
docker compose up -d postgres zookeeper kafka redis
```

### 2. Build & Run Backend

```bash
# Build (skip tests for quick start)
./mvnw clean package -DskipTests

# Run on port 8082
./mvnw spring-boot:run
```

### 3. Build & Run Frontend

```bash
cd frontend
npm install
npm run dev
```

### 4. Access the System

| Service | URL |
|---------|-----|
| Frontend App | http://localhost:5173 |
| Backend API | http://localhost:8082 |
| Swagger Docs | http://localhost:8082/swagger-ui.html |

---

## 📦 Production Deployment

```bash
# Start the full stack
docker compose up -d

# Health check
curl http://localhost:8082/actuator/health
```

---

## 🔑 Default Account

| Username | Password | Role |
|----------|----------|------|
| `admin` | `12345678@Abc` | SUPER_ADMIN |

> ⚠️ **Important:** Change the default password immediately after deployment.

---

## 📂 Project Structure

```
src/main/java/com/medbid/
├── auth/           # Authentication & authorization
├── user/           # User management
├── enterprise/     # Enterprise profiles
├── tender/         # Tender package management
├── hsmt/           # Tender document processing (OCR + AI)
├── product/        # Medical equipment library
├── matching/       # Product-to-requirement matching
├── quotation/      # Quotation management
├── export/         # Document export (Word / PDF / ZIP)
├── notification/   # Real-time notifications
├── chatbot/        # AI assistant chatbot
├── dashboard/      # Reports & statistics
├── audit/          # Audit logging
├── ocr/            # OCR abstraction layer
├── ai/             # AI abstraction layer
├── kafka/          # Kafka producers & consumers
├── backup/         # Backup & restore
├── monitoring/     # Health checks & metrics
├── config/         # Application configuration
├── common/         # Base classes & utilities
├── security/       # Security filters & config
└── exception/      # Exception handling
```

---

## 🔄 Data Pipeline

```
POST /api/bids/upload
        │
        ▼
   Kafka "hsmt-topic"
        │
        ├──► OCR Consumer (text extraction)
        │            │
        │            ▼
        └──► AI Consumer (requirement parsing)
                     │
                     ▼
              PostgreSQL (persist)
                     │
        ┌────────────┼────────────┐
        ▼            ▼            ▼
   Matching      Export       Dashboard
```

---

## 📡 API Overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | Health check |
| `POST` | `/api/auth/login` | JWT login |
| `POST` | `/api/bids/upload` | Upload bid file (multipart) |
| `GET` | `/api/match/{bidId}` | Requirement-to-bid matching |
| `GET` | `/api/export/word/{bidId}` | Export bid as Word |
| `GET` | `/api/export/pdf/{bidId}` | Export bid as PDF |
| `GET` | `/api/hsdt/export/full/{bidId}` | Full ZIP export |

Full API documentation available at Swagger UI.

---

## 🧪 Experimental Evidence & Replication Package

This repository includes a complete **replication package** in the [`evidence/`](evidence/) directory.

```
evidence/
├── 01-uat-test-suite/          # User Acceptance Test scripts & sample outputs
│   ├── uat-v2.mjs              (Node.js, 57 API tests)
│   ├── uat-api-test.mjs        (Node.js, 46 API tests)
│   ├── uat-ui.mjs              (Playwright, 17 UI tests)
│   ├── uat-v2-output.log       (Console output — all 57 tests passing)
│   ├── sample-output.log       (Sample execution log)
│   └── test-coverage-summary.md
├── 02-database-schema/         # Flyway migrations (V1 → V6)
│   ├── schema-summary.md       (26 tables across 10 groups)
│   ├── V1__init_schema.sql     (Full initial schema)
│   ├── V2__add_audit_columns.sql
│   ├── V3__enhance_enterprise_profile.sql
│   ├── V4__expiry_alert_table.sql
│   ├── V5__seed_test_data.sql  (Seed data: 5 products, 10 documents, 1 tender)
│   └── V6__seed_price_history.sql (27 price history records)
├── 03-seed-data/               # Test dataset manifest & details
│   └── dataset-manifest.md     (5 products, 27 prices, 10 docs, 1 tender)
├── 04-performance-benchmarks/  # Endpoint performance benchmarks
│   ├── benchmark-30iter.mjs    (30-iteration benchmark script)
│   ├── benchmark-30iter.csv    (Raw CSV results)
│   ├── benchmark-30iter.json   (Raw JSON results)
│   └── performance-results.md  (Tabulated performance metrics)
├── 05-unit-tests/              # JUnit 5 test outputs (8 test classes)
│   ├── test-classes.md
│   ├── sample-junit-output.txt
│   ├── AuthControllerIntegrationTest.txt
│   ├── AuthServiceTest.txt
│   ├── JwtUtilTest.txt
│   ├── ChatbotServiceTest.txt
│   ├── EnterpriseServiceTest.txt
│   ├── ProductServiceTest.txt
│   └── SmartMedTenderApplicationTests.txt
├── 06-source-code-metrics/     # Codebase statistics
│   └── code-metrics.md         (186 Java files, 19 Vue components, etc.)
├── DATA_TRANSPARENCY_STATEMENT.md  # Real vs. simulated data breakdown
├── REPLICATION_GUIDE.md            # Step-by-step reproducibility instructions
├── token-usage-report.md           # AI token usage report
└── README.md                       # Evidence package overview
```

### Key Results at a Glance

| Metric | Value | Evidence |
|--------|-------|----------|
| UAT test cases | 57 (API v2) + 46 (API v1) + 17 (UI) | `evidence/01-uat-test-suite/` |
| Database tables | 26 | `evidence/02-database-schema/V1__init_schema.sql` |
| Flyway migrations | 6 (V1 → V6) | `evidence/02-database-schema/` |
| Seed products | 5 | `evidence/02-database-schema/V5__seed_test_data.sql` |
| Price history records | 27 | `evidence/02-database-schema/V6__seed_price_history.sql` |
| Unit test classes | 8 (all passing) | `evidence/05-unit-tests/` |
| Java source files | 186 | `evidence/06-source-code-metrics/code-metrics.md` |
| Vue components | 19 | `evidence/06-source-code-metrics/code-metrics.md` |

### Reproducing the Experiments

```bash
# 1. Start the application
docker compose up -d
./mvnw spring-boot:run

# 2. Run UAT test suite (in another terminal)
cd frontend
node uat-v2.mjs

# 3. Run performance benchmarks
node benchmark-script.mjs

# 4. Run unit tests
./mvnw test
```

> 📖 See [`evidence/REPLICATION_GUIDE.md`](evidence/REPLICATION_GUIDE.md) for detailed step-by-step instructions.
>

---

## 💾 Backup & Restore

```bash
# Create backup
bash scripts/backup.sh

# Restore from backup
bash scripts/restore.sh backups/backup_YYYYMMDD_HHMMSS.sql
```

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

## 👥 Authors & Acknowledgments

Developed by the **MedTender Team** as part of the empirical software engineering research project.

---

<p align="center">
  <sub>Made with ❤️ for Vietnam's healthcare procurement digitization</sub>
</p>
