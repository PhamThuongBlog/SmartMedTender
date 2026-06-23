# SLA Checklist — Hệ Thống MedTender V2

## 1. Tính Sẵn Sàng (Availability)

| Chỉ Tiêu | Mục Tiêu | Phương Pháp Đo |
|----------|----------|----------------|
| Uptime hệ thống | >= 99.5% | Prometheus + Grafana |
| Thời gian phản hồi API | < 500ms (p95) | Actuator metrics |
| Thời gian khởi động | < 60s | Docker logs |
| Database availability | >= 99.9% | Health check pg_isready |

## 2. Hiệu Năng (Performance)

| Chỉ Tiêu | Mục Tiêu | Ghi Chú |
|----------|----------|---------|
| API requests/second | >= 100 | Tối thiểu trên cấu hình 2 CPU, 4GB RAM |
| OCR processing time | < 30s/file 10 trang | Tesseract, async |
| AI extraction time | < 60s | OpenAI API, async |
| Export Word/PDF | < 10s | Với < 50 requirements |
| Database query time | < 100ms | Có index, connection pool |

## 3. Bảo Mật (Security)

| Chỉ Tiêu | Mục Tiêu |
|----------|----------|
| Mã hóa mật khẩu | BCrypt strength 10 |
| JWT expiry | 24h access, 7d refresh |
| Rate limiting | 100 req/phút/IP |
| File validation | MIME type + extension whitelist |
| SQL Injection | Không có (JPA/Hibernate) |
| XSS Protection | Header + input sanitization |
| HTTPS | Bắt buộc trên production |

## 4. Backup & Recovery

| Chỉ Tiêu | Mục Tiêu |
|----------|----------|
| Backup frequency | Hàng ngày (2:00 AM) |
| Backup retention | 30 ngày |
| RPO (Recovery Point) | < 24 giờ |
| RTO (Recovery Time) | < 4 giờ |
| Backup automation | Cron job trong container |

## 5. Monitoring & Alerting

| Chỉ Tiêu | Công Cụ |
|----------|---------|
| Health check | Actuator /prometheus |
| Log aggregation | Logback file + console |
| Error tracking | Error logs với stack trace |
| Performance metrics | Micrometer + Prometheus |
| Kafka monitoring | Consumer lag, DLQ size |

## 6. Hỗ Trợ (Support)

| Chỉ Tiêu | Mục Tiêu |
|----------|----------|
| Thời gian phản hồi sự cố | < 4 giờ (giờ hành chính) |
| Thời gian fix bug critical | < 24 giờ |
| Thời gian fix bug major | < 72 giờ |
| Tài liệu hướng dẫn | Đầy đủ (User + Admin manual) |
| Swagger docs | Cập nhật tự động |

## 7. Dữ Liệu (Data Integrity)

| Chỉ Tiêu | Mục Tiêu |
|----------|----------|
| Transaction integrity | ACID (PostgreSQL) |
| Soft delete | Tất cả entities chính |
| Audit trail | Login, upload, export, delete |
| Data validation | Bean Validation + DB constraints |
| UUID primary keys | Tránh ID collision khi merge |
