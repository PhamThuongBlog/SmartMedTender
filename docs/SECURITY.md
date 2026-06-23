# Tài Liệu Bảo Mật (Security Documentation)

## Tổng Quan

Hệ thống MedTender V2 áp dụng bảo mật nhiều lớp:

1. **Transport**: HTTPS (production)
2. **Authentication**: JWT (access + refresh token)
3. **Authorization**: RBAC (7 roles + 8 permissions)
4. **Application**: Rate limiting, input validation, secure headers
5. **Data**: BCrypt password hashing, UUID PKs, audit trail
6. **Infrastructure**: Docker network isolation, non-root containers

## Cơ Chế Xác Thực

### JWT Flow
```
Client → POST /api/auth/login → Server
Client ← {accessToken, refreshToken} ← Server
Client → GET /api/* (Authorization: Bearer <accessToken>) → Server
Client → POST /api/auth/refresh → Server
Client ← {newAccessToken, newRefreshToken} (token rotation)
```

### Token Configuration
- Access token: 24 giờ
- Refresh token: 7 ngày
- Thuật toán: HMAC-SHA256
- Secret key: Cấu hình qua biến môi trường `JWT_SECRET`

## Phân Quyền (RBAC)

### Role Hierarchy
```
SUPER_ADMIN → ADMIN → MANAGER → STAFF
                         → REVIEWER
                         → LEGAL
                         → SALES
```

### Permission Matrix
| Permission | SUPER_ADMIN | ADMIN | MANAGER | STAFF | REVIEWER | LEGAL | SALES |
|------------|:-----------:|:-----:|:-------:|:-----:|:--------:|:-----:|:-----:|
| VIEW | x | x | x | x | x | x | x |
| CREATE | x | x | x | x | | | x |
| UPDATE | x | x | x | x | | x | |
| DELETE | x | x | | | | | |
| EXPORT | x | x | x | x | x | x | x |
| APPROVE | x | x | x | | x | x | |
| UPLOAD | x | x | x | x | | | x |
| CONFIGURE | x | x | | | | | |

## Bảo Vệ Ứng Dụng

### Rate Limiting
- Mặc định: 100 requests/phút/IP
- Bỏ qua: `/actuator/**`, `/api/auth/login`
- Triển khai: Bucket4j token-bucket
- Header: `X-Forwarded-For` để xác định IP thực

### File Upload Security
- Whitelist extensions: pdf, docx, doc, xlsx, xls, zip, png, jpg, jpeg
- MIME type validation
- Kích thước tối đa: 50MB
- Lưu ngoài web root
- Tên file được sanitize

### SQL Injection
- Sử dụng Spring Data JPA (Prepared Statements)
- Không có native query động với user input
- Mọi query đều qua parameter binding

### XSS Protection
- Content-Security-Policy header
- X-Content-Type-Options: nosniff
- X-Frame-Options: DENY
- Output encoding trên frontend

### CSRF
- Disabled (REST API stateless với JWT)
- CORS cấu hình restrictive trên production

## Password Policy
- Tối thiểu 8 ký tự
- Phải có: chữ hoa + chữ thường + số + ký tự đặc biệt
- Hash: BCrypt strength 10
- Khóa tài khoản sau 5 lần sai

## Audit Trail
- Login attempts (success/failure)
- Create/Update/Delete entities
- Export operations
- File uploads
- AI/OCR requests

## Khuyến Nghị Production

1. Đổi `JWT_SECRET` thành chuỗi ngẫu nhiên >= 256 bit
2. Đổi tất cả password mặc định
3. Bật HTTPS với SSL certificate hợp lệ
4. Cấu hình CORS chỉ cho phép domain của bạn
5. Đặt `spring.flyway.clean-disabled=true`
6. Không expose `/actuator/env`, `/actuator/loggers` ra public
7. Sử dụng secrets manager cho API keys (không để trong .env)
8. Chạy container với non-root user
9. Scan Docker images định kỳ với Trivy/Snyk
10. Cập nhật dependencies thường xuyên
