# ĐẶC TẢ API
## MedTender System V2.0 — RESTful API Specification

**Base URL:** `http://localhost:8082/api`  
**Swagger:** `http://localhost:8082/swagger-ui.html`  
**API Docs:** `http://localhost:8082/api-docs`

---

## Xác thực

Tất cả API (trừ public endpoints) yêu cầu header:
```
Authorization: Bearer <access_token>
```

**Public endpoints:** `/api/auth/login`, `/api/auth/refresh`, `/api/health`, `/actuator/health`, `/swagger-ui/**`

**Token expiry:** Access 24h | Refresh 7d

---

## 1. Auth API — `/api/auth`

| Method | Path | Auth | Mô tả |
|--------|------|------|-------|
| `POST` | `/api/auth/login` | Public | Đăng nhập → `{accessToken, refreshToken, tokenType, expiresIn}` |
| `POST` | `/api/auth/refresh` | Public | Refresh token → cặp token mới |
| `POST` | `/api/auth/logout` | Authenticated | Đăng xuất, revoke tokens |
| `POST` | `/api/auth/register` | ADMIN/SUPER_ADMIN | Đăng ký user mới |
| `GET` | `/api/auth/me` | Authenticated | Thông tin user hiện tại |
| `PATCH` | `/api/auth/change-password` | Authenticated | Đổi mật khẩu |

**Login Request:** `{ username: String, password: String }`  
**Login Response:** `{ accessToken, refreshToken, tokenType: "Bearer", expiresIn: 86400000 }`

---

## 2. Users API — `/api/users`

| Method | Path | Auth | Mô tả |
|--------|------|------|-------|
| `GET` | `/api/users?page=0&size=20&search=` | ADMIN/SUPER_ADMIN/MANAGER | Danh sách users |
| `GET` | `/api/users/{id}` | Authenticated | Chi tiết user |
| `POST` | `/api/users` | ADMIN/SUPER_ADMIN | Tạo user mới |
| `PUT` | `/api/users/{id}` | ADMIN/SUPER_ADMIN | Cập nhật user |
| `PATCH` | `/api/users/{id}/lock` | ADMIN/SUPER_ADMIN | Khóa/Mở khóa |
| `PATCH` | `/api/users/{id}/reset-password` | ADMIN/SUPER_ADMIN | Reset password |

---

## 3. Enterprise API — `/api/enterprises` + `/api/enterprise`

| Method | Path | Auth | Mô tả |
|--------|------|------|-------|
| `GET` | `/api/enterprises` | Authenticated | Danh sách doanh nghiệp |
| `PUT` | `/api/enterprise/profile` | Authenticated | Cập nhật hồ sơ chính |
| `GET` | `/api/enterprise/profile` | Authenticated | Lấy hồ sơ chính |
| `GET` | `/api/enterprise/legal-docs` | Authenticated | Tài liệu pháp lý |
| `POST` | `/api/enterprise/legal-docs` | Authenticated | Upload tài liệu PL |
| `PUT` | `/api/enterprise/legal-docs/{id}` | Authenticated | Sửa tài liệu PL |
| `DELETE` | `/api/enterprise/legal-docs/{id}` | Authenticated | Xóa tài liệu PL |

---

## 4. Products API — `/api/products`

| Method | Path | Auth | Mô tả |
|--------|------|------|-------|
| `GET` | `/api/products?page=&size=&search=&category=` | Authenticated | Danh sách sản phẩm (có filter) |
| `GET` | `/api/products/{id}` | Authenticated | Chi tiết sản phẩm |
| `POST` | `/api/products` | Authenticated | Tạo sản phẩm mới |
| `PUT` | `/api/products/{id}` | Authenticated | Cập nhật sản phẩm |
| `DELETE` | `/api/products/{id}` | Authenticated | Xóa mềm sản phẩm |

---

## 5. Document Library API — `/api/documents`

| Method | Path | Auth | Mô tả |
|--------|------|------|-------|
| `GET` | `/api/documents?page=&size=&search=&productId=&documentType=&status=` | Authenticated | Danh sách tài liệu (filterable) |
| `GET` | `/api/documents/{id}` | Authenticated | Chi tiết tài liệu |
| `POST` | `/api/documents` (multipart) | Authenticated | Upload tài liệu + file |
| `PUT` | `/api/documents/{id}` | Authenticated | Cập nhật metadata |
| `DELETE` | `/api/documents/{id}` | Authenticated | Xóa mềm |
| `GET` | `/api/documents/{id}/download` | Authenticated | Tải file |

---

## 6. Expiry Alerts API — `/api/expiry`

| Method | Path | Auth | Mô tả |
|--------|------|------|-------|
| `GET` | `/api/expiry/alerts?page=&size=&severity=` | Authenticated | Danh sách cảnh báo |
| `GET` | `/api/expiry/summary` | Authenticated | Tổng quan (critical/warning/info counts) |
| `POST` | `/api/expiry/check-now` | Authenticated | Kiểm tra hết hạn ngay |
| `PUT` | `/api/expiry/alerts/{id}/dismiss` | Authenticated | Bỏ qua 1 cảnh báo |
| `PUT` | `/api/expiry/alerts/dismiss-all` | Authenticated | Bỏ qua tất cả |

---

## 7. Tender API — `/api/tenders`

| Method | Path | Auth | Mô tả |
|--------|------|------|-------|
| `GET` | `/api/tenders` | Authenticated | Danh sách gói thầu |
| `POST` | `/api/tenders` | Authenticated | Tạo gói thầu mới |
| `GET` | `/api/tenders/{id}` | Authenticated | Chi tiết |
| `PUT` | `/api/tenders/{id}` | Authenticated | Cập nhật |
| `DELETE` | `/api/tenders/{id}` | Authenticated | Xóa mềm |
| `PATCH` | `/api/tenders/{id}/status` | Authenticated | Cập nhật trạng thái |
| `POST` | `/api/tenders/{id}/clone` | Authenticated | **Sao chép gói thầu** |
| `POST` | `/api/tenders/{id}/outcome` | Authenticated | **Ghi kết quả trúng/trượt + giá** |
| `GET` | `/api/tenders/history?statuses=WON,LOST` | Authenticated | **Lịch sử gói thầu** |
| `GET` | `/api/tenders/{id}/items` | Authenticated | Danh sách hạng mục |
| `POST` | `/api/tenders/{id}/items` | Authenticated | Thêm hạng mục |

---

## 8. HSMT API — `/api/hsmt`

| Method | Path | Auth | Mô tả |
|--------|------|------|-------|
| `POST` | `/api/hsmt/upload` (multipart 50MB) | Authenticated | Upload file HSMT + OCR + AI |
| `POST` | `/api/hsmt/upload/batch` | Authenticated | **Upload nhiều file** |
| `GET` | `/api/hsmt/{tenderId}/requirements?status=` | Authenticated | Danh sách yêu cầu đã trích xuất |
| `PUT` | `/api/hsmt/requirements/{id}` | Authenticated | Chỉnh sửa yêu cầu |
| `POST` | `/api/hsmt/requirements/{id}/approve` | Authenticated | Phê duyệt → VERIFIED |
| `POST` | `/api/hsmt/requirements/{id}/reject` | Authenticated | Từ chối → REJECTED |
| `POST` | `/api/hsmt/requirements/batch-approve` | Authenticated | Phê duyệt hàng loạt |
| `POST` | `/api/hsmt/requirements/batch-reject` | Authenticated | Từ chối hàng loạt |

---

## 9. Matching API — `/api/match`

| Method | Path | Auth | Mô tả |
|--------|------|------|-------|
| `POST` | `/api/match` | Authenticated | So sánh sản phẩm với gói thầu |
| `GET` | `/api/match/{tenderId}/best?limit=5` | Authenticated | Top sản phẩm phù hợp |
| `GET` | `/api/match/{tenderId}/smart-suggest?limit=5` | Authenticated | **Gợi ý thông minh (có certs + giá + gap)** |
| `GET` | `/api/match/{tenderId}/product/{productId}/compliance` | Authenticated | **Kiểm tra chứng chỉ** |
| `GET` | `/api/match/{tenderId}/gap-analysis?productId=` | Authenticated | **Phân tích khoảng trống** |
| `PUT` | `/api/match/results/override` | Authenticated | **Ghi đè thủ công** |
| `GET` | `/api/match/results?tenderId=&productId=` | Authenticated | Kết quả đã lưu |

---

## 10. Quotation API — `/api/quotations`

| Method | Path | Auth | Mô tả |
|--------|------|------|-------|
| `GET` | `/api/quotations` | Authenticated | Danh sách báo giá |
| `POST` | `/api/quotations` | Authenticated | Tạo báo giá |
| `GET` | `/api/quotations/suggest/{productId}/tender/{tenderId}` | Authenticated | **Gợi ý giá** |
| `GET` | `/api/quotations/chart/{productId}` | Authenticated | Biểu đồ giá |
| `GET` | `/api/quotations/statistics/{productId}` | Authenticated | Thống kê giá |

---

## 11. HSDT API — `/api/hsdt`

| Method | Path | Auth | Mô tả |
|--------|------|------|-------|
| `POST` | `/api/hsdt/preview` | Authenticated | **Preview hồ sơ (comparison + checklist + price)** |
| `POST` | `/api/hsdt/checklist` | Authenticated | **Smart checklist** |
| `POST` | `/api/hsdt/export/word` | Authenticated | **Xuất Word (.docx)** |
| `POST` | `/api/hsdt/export/pdf` | Authenticated | **Xuất PDF (.pdf)** |
| `POST` | `/api/hsdt/export/zip` | Authenticated | **Xuất ZIP đầy đủ** |
| `POST` | `/api/hsdt/export/excel` | Authenticated | **Xuất Excel (.xlsx)** |

---

## 12. Export API — `/api/export`

| Method | Path | Auth | Mô tả |
|--------|------|------|-------|
| `GET` | `/api/export/word/{tenderId}` | Authenticated | Xuất Word trực tiếp |
| `GET` | `/api/export/pdf/{tenderId}` | Authenticated | Xuất PDF trực tiếp |
| `GET` | `/api/export/zip/{tenderId}` | Authenticated | Xuất ZIP trực tiếp |
| `GET` | `/api/export/excel/{tenderId}` | Authenticated | Xuất Excel trực tiếp |
| `GET` | `/api/export/history` | Authenticated | Lịch sử xuất |

---

## 13. Security/Admin API

| Method | Path | Auth | Mô tả |
|--------|------|------|-------|
| `GET` | `/api/audit` | ADMIN/SUPER_ADMIN | **Audit logs (30 ngày)** |
| `GET` | `/api/audit/user/{userId}` | ADMIN/SUPER_ADMIN | Audit theo user |
| `GET` | `/api/audit/entity?type=&id=` | ADMIN/SUPER_ADMIN | Audit theo entity |
| `GET` | `/api/admin/ai-config` | ADMIN/SUPER_ADMIN | **AI provider config** |
| `POST` | `/api/admin/ai-config/test` | ADMIN/SUPER_ADMIN | **Test AI extraction** |
| `POST` | `/api/backup` | ADMIN/SUPER_ADMIN | Backup ngay |
| `POST` | `/api/backup/offsite` | ADMIN/SUPER_ADMIN | **Off-site backup** |
| `GET` | `/api/backup/sla` | ADMIN/SUPER_ADMIN | **RTO/RPO SLA** |
| `POST` | `/api/backup/restore?file=` | ADMIN/SUPER_ADMIN | Phục hồi dữ liệu |

---

## 14. Health & Monitoring

| Method | Path | Auth | Mô tả |
|--------|------|------|-------|
| `GET` | `/api/health` | Authenticated | System health |
| `GET` | `/actuator/health` | Public | Spring Actuator health |
| `GET` | `/actuator/metrics` | SUPER_ADMIN | Metrics |
| `GET` | `/actuator/prometheus` | SUPER_ADMIN | Prometheus endpoint |

---

## Tổng kết

- **~120 API endpoints** trên 15 controllers
- **Định dạng response:** JSON (trừ export trả về binary file)
- **Pagination:** Spring Data `Page<T>` — `{content, totalElements, totalPages, ...}`
- **Error format:** `{ status, message, details, timestamp }`
- **Validation:** Jakarta Bean Validation (`@Valid`, `@NotNull`, `@Size`, `@Email`)
- **API Docs:** SpringDoc OpenAPI 2.6.0 → Swagger UI tại `/swagger-ui.html`
