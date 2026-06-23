# Hướng Dẫn Quản Trị (Admin Manual)

## 1. Cài Đặt Hệ Thống

Xem [DEPLOYMENT.md](DEPLOYMENT.md) để biết chi tiết triển khai.

## 2. Quản Lý Người Dùng

### Tạo người dùng mới
1. Đăng nhập với tài khoản ADMIN
2. Vào menu "Quản trị" → "Người dùng"
3. Nhấn "Thêm mới"
4. Nhập: username, password (>= 8 ký tự, có chữ hoa, chữ thường, số, ký tự đặc biệt), email, họ tên, role
5. Nhấn "Lưu"

### Khóa/Mở khóa tài khoản
1. Vào danh sách người dùng
2. Nhấn icon khóa/mở khóa ở dòng tương ứng

### Reset mật khẩu
1. Vào danh sách người dùng
2. Nhấn "Reset mật khẩu"
3. Nhập mật khẩu mới

## 3. Phân Quyền (RBAC)

Hệ thống có 7 roles:

| Role | Quyền hạn |
|------|-----------|
| SUPER_ADMIN | Toàn quyền (kể cả cấu hình hệ thống) |
| ADMIN | Quản lý user, cấu hình, backup |
| MANAGER | Quản lý gói thầu, phê duyệt, export |
| STAFF | Tạo hồ sơ, upload, xem dữ liệu |
| REVIEWER | Kiểm duyệt hồ sơ, phê duyệt |
| LEGAL | Quản lý hồ sơ pháp lý, phê duyệt |
| SALES | Xem, tạo báo giá, export |

## 4. Cấu Hình Hệ Thống

### File .env
```
JWT_SECRET=<secret-key>
DATASOURCE_PASSWORD=<db-password>
OPENAI_API_KEY=<api-key>
```

### Rate Limiting
Mặc định: 100 requests/phút/IP. Có thể thay đổi trong `RateLimitFilter.java`.

### File Upload
- Định dạng cho phép: PDF, DOCX, DOC, XLSX, XLS, ZIP, PNG, JPG, JPEG
- Kích thước tối đa: 50MB/file
- Cấu hình trong `application.yml` → `app.file`

## 5. Backup & Restore

### Backup thủ công
```bash
bash scripts/backup.sh
```

### Restore
```bash
bash scripts/restore.sh backups/backup_YYYYMMDD_HHMMSS.sql
```

### Lịch backup tự động
- Hàng ngày lúc 2:00 AM
- Giữ backup 30 ngày gần nhất

## 6. Giám Sát

### Health Check
```bash
curl http://localhost:8082/actuator/health
curl http://localhost:8082/api/health
```

### Metrics (Prometheus)
```bash
curl http://localhost:8082/actuator/prometheus
```

### Logs
```bash
docker compose logs -f backend       # Docker
tail -f logs/medtender.log           # Không Docker
```

## 7. Xử Lý Sự Cố

### Backend không khởi động
1. Kiểm tra PostgreSQL đã chạy chưa: `docker compose ps postgres`
2. Kiểm tra logs: `docker compose logs backend`
3. Kiểm tra kết nối DB: `telnet localhost 5432`

### OCR không hoạt động
1. Kiểm tra Tesseract đã cài chưa: `tesseract --version`
2. Kiểm tra file TESSDATA: `/usr/share/tesseract-ocr/4.00/tessdata`
3. Fallback: dùng Google Vision OCR (cần API key)

### Kafka consumer không nhận message
1. Kiểm tra Kafka: `docker compose logs kafka`
2. Kiểm tra consumer group lag:
```bash
docker exec medbid-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group hsmt-processing-group --describe
```
