# TÀI LIỆU PHÂN TÍCH HỆ THỐNG
## MedTender System V2.0 — Hệ thống quản lý hồ sơ dự thầu thiết bị y tế

**Phiên bản:** 2.0.0-SNAPSHOT  
**Ngày:** 07/06/2026  
**Trạng thái:** READY FOR PRODUCTION

---

## Mục lục

1. [Tổng quan hệ thống](#1-tổng-quan-hệ-thống)
2. [Phạm vi dự án](#2-phạm-vi-dự-án)
3. [Đối tượng người dùng (Actors)](#3-đối-tượng-người-dùng-actors)
4. [Yêu cầu chức năng (Functional Requirements)](#4-yêu-cầu-chức-năng-functional-requirements)
5. [Yêu cầu phi chức năng (Non-Functional Requirements)](#5-yêu-cầu-phi-chức-năng-non-functional-requirements)
6. [Ma trận use case](#6-ma-trận-use-case)
7. [Sơ đồ use case tổng quát](#7-sơ-đồ-use-case-tổng-quát)

---

## 1. Tổng quan hệ thống

MedTender là hệ thống phần mềm tự động hóa quy trình đấu thầu thiết bị y tế tại Việt Nam. Hệ thống hỗ trợ doanh nghiệp cung cấp thiết bị y tế trong toàn bộ vòng đời dự thầu: từ thiết lập hồ sơ ban đầu, đọc và phân tích hồ sơ mời thầu, đối chiếu sản phẩm, tạo hồ sơ dự thầu, đến xuất bộ hồ sơ hoàn chỉnh và quản lý lịch sử trúng thầu.

**Kiến trúc:** 3-tier (Spring Boot REST API + PostgreSQL + Vue 3 SPA)  
**Pipeline xử lý:** File upload → OCR (Tesseract) → AI Extraction (OpenAI GPT-4o) → Review → Matching → HSDT Builder → Export (Word/PDF/ZIP/Excel)

---

## 2. Phạm vi dự án

### 2.1 Phạm vi bao gồm (In Scope)

| Module | Mô tả |
|--------|-------|
| Thiết lập ban đầu | Quản lý hồ sơ doanh nghiệp, thư viện sản phẩm, thư viện chứng chỉ (CO/CQ/ISO/CE/FDA/Catalogue) |
| Đọc HSMT | Upload file PDF/DOCX/XLSX/ZIP/ảnh, OCR trích xuất văn bản, AI trích xuất yêu cầu kỹ thuật |
| OCR Review | Xem xét, chỉnh sửa, phê duyệt/từ chối yêu cầu đã trích xuất (batch + single) |
| Đối chiếu thông minh | Gợi ý sản phẩm, so sánh đạt/không đạt, cảnh báo thiếu chứng chỉ/tài liệu |
| Gợi ý giá | Dựa trên lịch sử giá trúng thầu (PriceHistory) |
| Ghi đè thủ công | Cho phép người dùng ghi đè kết quả đối chiếu |
| Tạo HSDT | Tổng hợp bảng so sánh kỹ thuật + bảng giá + checklist + hồ sơ pháp lý |
| Xuất hồ sơ | Word (.docx), PDF (.pdf), Excel (.xlsx), ZIP (bộ đầy đủ) |
| Lịch sử & Tái sử dụng | Clone gói thầu, ghi nhận kết quả trúng/trượt + giá |
| Quản lý tài khoản | Tạo/sửa/khóa tài khoản, 7 vai trò với phân quyền chi tiết |
| Bảo mật | JWT authentication, RBAC, rate limiting, audit log |
| Sao lưu & Phục hồi | Backup hàng ngày (pg_dump), off-site, RTO/RPO SLA |
| AI Provider Swap | Strategy pattern — 3 providers (OpenAI/Claude/Gemini), chuyển đổi không cần viết lại code |

### 2.2 Phạm vi không bao gồm (Out of Scope)

- Tích hợp cổng đấu thầu quốc gia (muasamcong.mpi.gov.vn)
- Mobile app (mobile responsive web only)
- Thanh toán/chữ ký số
- Multi-tenant (single enterprise)

---

## 3. Đối tượng người dùng (Actors)

| Vai trò | Mã | Mô tả | Quyền chính |
|--------|-----|-------|------------|
| Super Admin | SUPER_ADMIN | Quản trị viên cao nhất | Toàn quyền hệ thống |
| Admin | ADMIN | Quản trị viên | Quản lý users, backup, AI config, audit |
| Manager | MANAGER | Quản lý nghiệp vụ | Xem, tạo, sửa, xuất, phê duyệt, upload |
| Staff | STAFF | Nhân viên xử lý hồ sơ | Xem, tạo, sửa, xuất, upload |
| Reviewer | REVIEWER | Người kiểm duyệt | Xem, phê duyệt, xuất |
| Legal | LEGAL | Chuyên viên pháp lý | Xem, sửa, phê duyệt, xuất |
| Sales | SALES | Nhân viên kinh doanh | Xem, tạo, xuất, upload |

---

## 4. Yêu cầu chức năng (Functional Requirements)

### FR-01: Thiết lập ban đầu
- **FR-01.1:** Hệ thống cho phép tạo/sửa hồ sơ pháp lý doanh nghiệp (tên, MST, địa chỉ, GPKD, người đại diện)
- **FR-01.2:** Hệ thống hỗ trợ quản lý thư viện sản phẩm (tên, hãng, model, xuất xứ, thông số kỹ thuật, số đăng ký)
- **FR-01.3:** Hệ thống cho phép upload tài liệu chứng chỉ (CO, CQ, ISO 13485, ISO 9001, CE, FDA, Catalogue)
- **FR-01.4:** Hệ thống tự động cảnh báo chứng chỉ/tài liệu sắp hết hạn (0-30 ngày: CRITICAL, 31-60: WARNING, 61-90: INFO)
- **FR-01.5:** Scheduled check hàng ngày vào 8:00 AM

### FR-02: Đọc hồ sơ mời thầu (HSMT)
- **FR-02.1:** Upload file HSMT định dạng PDF, DOCX, DOC, XLSX, XLS, ZIP, PNG, JPG (tối đa 50MB/file)
- **FR-02.2:** OCR trích xuất văn bản bằng Tesseract (hỗ trợ tiếng Việt + tiếng Anh)
- **FR-02.3:** AI trích xuất yêu cầu kỹ thuật (thông số, số lượng, đơn vị, loại, toán tử, giá trị)
- **FR-02.4:** Phân loại yêu cầu: TECHNICAL, CERTIFICATION, EXPERIENCE, FINANCIAL, OTHER
- **FR-02.5:** Phân loại bắt buộc (mandatory=true) và ưu tiên (priority 1-3)
- **FR-02.6:** Người dùng review, chỉnh sửa, phê duyệt/từ chối từng yêu cầu
- **FR-02.7:** Batch approve/reject

### FR-03: Đối chiếu sản phẩm thông minh
- **FR-03.1:** Gợi ý sản phẩm phù hợp với gói thầu (smart-suggest)
- **FR-03.2:** So sánh chi tiết từng tiêu chí (numeric >=, <=, >, <; equality =; keyword contains)
- **FR-03.3:** Kiểm tra chứng chỉ (ISO/CE/FDA/CO/CQ) — Compliance check
- **FR-03.4:** Phân tích khoảng trống (Gap Analysis) — tiêu chí thiếu, tài liệu thiếu, chứng chỉ hết hạn
- **FR-03.5:** Gợi ý giá dựa trên lịch sử trúng thầu (PriceHistory)
- **FR-03.6:** Cho phép ghi đè thủ công (manual override) với lý do

### FR-04: Tạo hồ sơ dự thầu (HSDT)
- **FR-04.1:** Tổng hợp bảng so sánh kỹ thuật cho các sản phẩm đã chọn
- **FR-04.2:** Bảng giá chào (dựa trên giá gợi ý từ lịch sử)
- **FR-04.3:** Gom tài liệu kỹ thuật và chứng chỉ sản phẩm
- **FR-04.4:** Đính kèm hồ sơ pháp lý doanh nghiệp
- **FR-04.5:** Smart checklist kiểm tra hồ sơ (41+ items, 5 sections)
- **FR-04.6:** Xuất Word (.docx) — bảng so sánh + checklist + thông tin doanh nghiệp
- **FR-04.7:** Xuất PDF (.pdf) — có watermark "TÀI LIỆU NỘI BỘ"
- **FR-04.8:** Xuất Excel (.xlsx) — sheet thông số + sheet giá
- **FR-04.9:** Xuất ZIP — bộ đầy đủ (DOCX + PDF + checklist.txt + metadata.json)

### FR-05: Lịch sử & Tái sử dụng
- **FR-05.1:** Sao chép (clone) gói thầu cũ thành bản mới (DRAFT)
- **FR-05.2:** Ghi nhận kết quả trúng/trượt kèm giá trúng thầu
- **FR-05.3:** Lưu giá trúng vào PriceHistory để tái sử dụng
- **FR-05.4:** Xem lịch sử gói thầu đã có kết quả (WON/LOST)

### FR-06: Đăng nhập & Quản lý tài khoản
- **FR-06.1:** JWT authentication (24h access token + 7d refresh token)
- **FR-06.2:** BCrypt password hashing (strength 10)
- **FR-06.3:** Tạo/sửa/khóa/mở khóa tài khoản (admin only)
- **FR-06.4:** Reset password (admin initiated)
- **FR-06.5:** Đổi mật khẩu (self-service, yêu cầu old password)
- **FR-06.6:** 7 vai trò với phân quyền RBAC
- **FR-06.7:** Tự động khóa sau 5 lần đăng nhập sai
- **FR-06.8:** Login history logging

### FR-07: Bảo mật & Hạ tầng
- **FR-07.1:** HTTPS ready (cấu hình qua reverse proxy)
- **FR-07.2:** JWT stateless authentication
- **FR-07.3:** File upload validation (MIME type + 50MB limit + extension whitelist)
- **FR-07.4:** Rate limiting (100 requests/phút/IP)
- **FR-07.5:** Backup PostgreSQL hàng ngày 2:00 AM (pg_dump -Fc)
- **FR-07.6:** Off-site backup (copy đến thư mục riêng)
- **FR-07.7:** RTO = 60 phút, RPO = 1440 phút
- **FR-07.8:** Phục hồi dữ liệu (pg_restore)
- **FR-07.9:** Audit log (user, entity, action, IP, timestamp)
- **FR-07.10:** AI Provider swap (OpenAI/Claude/Gemini) — strategy pattern
- **FR-07.11:** Logging (SLF4J, file rolling 10MB, giữ 30 ngày)

---

## 5. Yêu cầu phi chức năng (Non-Functional Requirements)

| Mã | Yêu cầu | Chỉ tiêu | Trạng thái |
|----|---------|----------|------------|
| NFR-01 | Thời gian phản hồi API | < 500ms (trung bình < 15ms) | ✅ Đạt |
| NFR-02 | Dung lượng file upload | Tối đa 50MB/file | ✅ Đạt |
| NFR-03 | Hỗ trợ người dùng đồng thời | ≥ 50 users (HikariCP pool=20) | ✅ Đạt |
| NFR-04 | Bảo mật mật khẩu | BCrypt strength 10 | ✅ Đạt |
| NFR-05 | Token expiry | Access 24h, Refresh 7d | ✅ Đạt |
| NFR-06 | Rate limit | 100 req/phút/IP | ✅ Đạt |
| NFR-07 | Backup RTO | 60 phút | ✅ Đạt |
| NFR-08 | Backup RPO | 1440 phút (24h) | ✅ Đạt |
| NFR-09 | Retention log | 30 ngày (10MB/file rolling) | ✅ Đạt |
| NFR-10 | OCR ngôn ngữ | Tiếng Việt + Tiếng Anh | ✅ Đạt |
| NFR-11 | AI fallback | Rule-based parser khi API offline | ✅ Đạt |
| NFR-12 | Database versioning | Flyway migration | ✅ Đạt (6 migrations) |

---

## 6. Ma trận use case

| Use Case | SUPER_ADMIN | ADMIN | MANAGER | STAFF | REVIEWER | LEGAL | SALES |
|----------|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| UC-01: Thiết lập doanh nghiệp | ✅ | ✅ | ✅ | ✅ | | ✅ | |
| UC-02: Quản lý sản phẩm | ✅ | ✅ | ✅ | ✅ | | | ✅ |
| UC-03: Quản lý chứng chỉ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| UC-04: Cảnh báo hết hạn | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| UC-05: Upload HSMT | ✅ | ✅ | ✅ | ✅ | | | ✅ |
| UC-06: OCR Review | ✅ | ✅ | ✅ | ✅ | ✅ | | |
| UC-07: Đối chiếu sản phẩm | ✅ | ✅ | ✅ | ✅ | ✅ | | ✅ |
| UC-08: Gap Analysis | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | |
| UC-09: Gợi ý giá | ✅ | ✅ | ✅ | ✅ | | | ✅ |
| UC-10: Tạo HSDT | ✅ | ✅ | ✅ | ✅ | | | ✅ |
| UC-11: Xuất hồ sơ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| UC-12: Clone gói thầu | ✅ | ✅ | ✅ | ✅ | | | |
| UC-13: Ghi kết quả trúng/trượt | ✅ | ✅ | ✅ | | | | |
| UC-14: Quản lý users | ✅ | ✅ | | | | | |
| UC-15: Sao lưu/phục hồi | ✅ | ✅ | | | | | |
| UC-16: Audit log | ✅ | ✅ | | | | | |
| UC-17: AI Config | ✅ | ✅ | | | | | |

---

## 7. Sơ đồ use case tổng quát

```
┌─────────────────────────────────────────────────────────────────────┐
│                        MEDTENDER SYSTEM                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────┐    ┌──────────────────┐    ┌───────────────┐ │
│  │ Thiết lập ban đầu│    │    Đọc HSMT      │    │ Đối chiếu SP  │ │
│  │                  │    │                  │    │               │ │
│  │ • Hồ sơ DN       │    │ • Upload file    │    │ • Smart match │ │
│  │ • Thư viện SP    │    │ • OCR (Tesseract)│    │ • Compliance  │ │
│  │ • CO/CQ/ISO/CE   │    │ • AI extraction  │    │ • Gap analysis│ │
│  │ • Cảnh báo HH    │    │ • Review/Approve │    │ • Giá gợi ý   │ │
│  └────────┬─────────┘    └────────┬─────────┘    └───────┬───────┘ │
│           │                       │                      │         │
│           └───────────────────────┼──────────────────────┘         │
│                                   │                                │
│                          ┌────────▼─────────┐                      │
│                          │   Tạo HSDT       │                      │
│                          │                  │                      │
│                          │ • Bảng so sánh   │                      │
│                          │ • Bảng giá       │                      │
│                          │ • Checklist      │                      │
│                          │ • Xuất W/P/Z/E   │                      │
│                          └────────┬─────────┘                      │
│                                   │                                │
│  ┌──────────────────┐    ┌────────▼─────────┐    ┌───────────────┐ │
│  │ Lịch sử & Tái SD │    │  Quản lý TK      │    │ Bảo mật & HT  │ │
│  │                  │    │                  │    │               │ │
│  │ • Clone gói thầu │    │ • 7 vai trò      │    │ • JWT + RBAC  │ │
│  │ • WON/LOST + giá │    │ • Lock/Unlock    │    │ • Rate limit  │ │
│  │ • PriceHistory   │    │ • Reset password │    │ • Backup+RTO  │ │
│  └──────────────────┘    └──────────────────┘    │ • AI swap     │ │
│                                                   └───────────────┘ │
│                                                                     │
│  ════════════════ DATA INFRASTRUCTURE ════════════════              │
│  PostgreSQL │ Apache Kafka │ Redis │ JPA/Hibernate │ Flyway         │
│  Tess4J OCR │ OpenAI GPT-4o │ Apache POI │ iText 7 │ Bucket4j      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 8. Luồng dữ liệu chính (Data Flow)

### 8.1 Pipeline xử lý HSMT

```
User Upload → Validate (size/type) → Save to Disk → DB record (PENDING)
    → Kafka Message → OCR Consumer → Tesseract OCR → OCR Log
    → AI Consumer → OpenAI/Rule-based → TenderRequirement (EXTRACTED)
    → User Review → Edit/Approve/Reject → VERIFIED/REJECTED
    → Product Matching → MatchResult (score + compliance)
    → HSDT Builder → Export (Word/PDF/ZIP/Excel)
```

### 8.2 Pipeline giá

```
Thắng thầu → recordOutcome(WON, price) → PriceHistory
    → suggestPrice() → compute from winning history → confidence scoring
    → HSDT Preview → price suggestion per product
```

---

## 9. Công nghệ sử dụng

| Lớp | Công nghệ | Phiên bản |
|-----|-----------|-----------|
| Backend | Java | 17 |
| Framework | Spring Boot | 3.4.5 |
| Database | PostgreSQL | 16 |
| ORM | Hibernate (JPA) | 6.6 |
| Migration | Flyway | (embedded) |
| Message Queue | Apache Kafka | (Docker) |
| Cache | Redis | (Docker) |
| Auth | JWT (jjwt) | 0.12.6 |
| OCR | Tess4J (Tesseract) | 5.9.0 |
| Document | Apache POI | 5.2.5 |
| PDF | iText 7 | 7.1.17 |
| AI | OpenAI GPT-4o | API |
| Rate Limit | Bucket4j | 8.7.0 |
| API Docs | SpringDoc OpenAPI | 2.6.0 |
| Mapping | MapStruct | 1.5.5 |
| Frontend | Vue 3 | 3.4 |
| UI Library | PrimeVue | 3.52 |
| Build | Vite | 5.4 |
| State | Pinia | 2 |
| Router | Vue Router | 4 |
| Testing | JUnit 5, Playwright | |
