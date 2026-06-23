# HƯỚNG DẪN VẬN HÀNH
## MedTender System V2.0 — Operations Manual

**Ngày:** 07/06/2026 | **Phiên bản:** 2.0.0

---

## Mục lục

1. [Khởi động/Dừng hệ thống](#1-khởi-độngdừng-hệ-thống)
2. [Quy trình backup & phục hồi](#2-quy-trình-backup--phục-hồi)
3. [Giám sát hệ thống](#3-giám-sát-hệ-thống)
4. [Quản lý người dùng Admin](#4-quản-lý-người-dùng-admin)
5. [Thay đổi AI Provider](#5-thay-đổi-ai-provider)
6. [Xử lý sự cố](#6-xử-lý-sự-cố)
7. [Bảo trì định kỳ](#7-bảo-trì-định-kỳ)
8. [Logging & Audit](#8-logging--audit)
9. [SLA & RTO/RPO](#9-sla--rtorpo)

---

## 1. Khởi động/Dừng hệ thống

### 1.1 Khởi động Docker services

```bash
cd /opt/medtender

# Start infrastructure
docker compose up -d postgres zookeeper kafka redis

# Wait for services
docker compose ps  # all should be "healthy"
```

### 1.2 Khởi động Backend

```bash
# Method 1: systemd
sudo systemctl start medtender

# Method 2: manual
java -jar /opt/medtender/app.jar --spring.profiles.active=prod &

# Method 3: Docker
docker compose up -d backend
```

### 1.3 Kiểm tra startup

```bash
# Đợi Spring Boot khởi động (~15 giây)
sleep 20

# Kiểm tra health check
curl http://localhost:8082/api/health
# Expected: {"status":"UP"}

# Kiểm tra database
curl http://localhost:8082/api/products?size=1 \
  -H "Authorization: Bearer <token>"
```

### 1.4 Dừng hệ thống

```bash
# Dừng backend
sudo systemctl stop medtender

# Dừng tất cả services
docker compose down
```

---

## 2. Quy trình backup & phục hồi

### 2.1 Backup tự động (Scheduled)

Backup tự động chạy mỗi ngày lúc **2:00 AM**:
```yaml
# application.yml
app.backup.schedule: "0 0 2 * * ?"  # cron: 2:00 AM daily
```

File backup được lưu tại `${BACKUP_DIR}/backup_yyyyMMdd_HHmmss.sql`.

### 2.2 Backup thủ công (qua API)

```bash
TOKEN=$(curl -s -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"<password>"}' | jq -r '.accessToken')

# Backup ngay
curl -X POST http://localhost:8082/api/backup \
  -H "Authorization: Bearer $TOKEN"

# Response: {"status":"SUCCESS","file":"./backups/backup_20260607_020000.sql"}
```

### 2.3 Off-site Backup

```bash
# Copy backup đến vị trí off-site (cloud storage, NAS, external drive)
curl -X POST http://localhost:8082/api/backup/offsite \
  -H "Authorization: Bearer $TOKEN"

# Response: {
#   "status":"SUCCESS",
#   "localFile":"./backups/backup_20260607_020000.sql",
#   "offsiteFile":"./backups/offsite/offsite_backup_20260607_020000.sql",
#   "message":"Backup da duoc chuyen den vi tri off-site"
# }
```

### 2.4 Phục hồi dữ liệu (Restore)

**QUAN TRỌNG:** Phục hồi sẽ xóa toàn bộ dữ liệu hiện tại và thay thế bằng backup!

```bash
# 1. Dừng backend trước khi restore
sudo systemctl stop medtender

# 2. Restore qua API
curl -X POST "http://localhost:8082/api/backup/restore?file=backup_20260607_020000.sql" \
  -H "Authorization: Bearer $TOKEN"

# 3. Khởi động lại
sudo systemctl start medtender
```

**RTO cam kết:** 60 phút (tổng thời gian từ lúc phát hiện sự cố đến khi hệ thống hoạt động trở lại)  
**RPO cam kết:** 1440 phút = 24 giờ (dữ liệu bị mất tối đa kể từ lần backup cuối)

### 2.5 Kiểm tra SLA

```bash
curl http://localhost:8082/api/backup/sla \
  -H "Authorization: Bearer $TOKEN"

# Response: {
#   "rtoMinutes": 60,
#   "rpoMinutes": 1440,
#   "rtoDescription": "RTO: 60 phut...",
#   "rpoDescription": "RPO: 1440 phut...",
#   "backupSchedule": "0 0 2 * * ? (2:00 AM hang ngay)",
#   "retentionDays": 30,
#   "offsiteEnabled": true
# }
```

---

## 3. Giám sát hệ thống

### 3.1 Health Check

```bash
# Basic health
curl http://localhost:8082/api/health

# Spring Actuator health (chi tiết)
curl http://localhost:8082/actuator/health

# Metrics
curl http://localhost:8082/actuator/metrics/http.server.requests
```

### 3.2 Dashboard metrics

Truy cập API dashboard để xem thống kê:
```bash
curl http://localhost:8082/api/dashboard/stats \
  -H "Authorization: Bearer $TOKEN"
```

### 3.3 Cảnh báo hết hạn

Hệ thống tự động kiểm tra hàng ngày lúc 8:00 AM. Xem danh sách:
```bash
curl http://localhost:8082/api/expiry/summary \
  -H "Authorization: Bearer $TOKEN"
```

### 3.4 Prometheus metrics (nếu cấu hình)

```
# prometheus.yml
scrape_configs:
  - job_name: 'medtender'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8082']
```

---

## 4. Quản lý người dùng Admin

### 4.1 Tạo Admin mới (qua API)

```bash
curl -X POST http://localhost:8082/api/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newadmin",
    "password": "SecurePass@123",
    "email": "newadmin@company.com",
    "fullName": "New Admin",
    "roleId": "00000000-0000-0000-0000-000000000002"
  }'
```

### 4.2 Khóa tài khoản khẩn cấp

```bash
curl -X PATCH http://localhost:8082/api/users/{userId}/lock \
  -H "Authorization: Bearer $TOKEN"
```

### 4.3 Reset mật khẩu người dùng

```bash
curl -X PATCH http://localhost:8082/api/users/{userId}/reset-password \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"newPassword": "TempPass@12345"}'
```

---

## 5. Thay đổi AI Provider

Hệ thống hỗ trợ 3 AI provider theo strategy pattern — thay đổi không cần viết lại code.

### 5.1 Xem cấu hình hiện tại

```bash
curl http://localhost:8082/api/admin/ai-config \
  -H "Authorization: Bearer $TOKEN"
```

### 5.2 Chuyển sang provider khác

**Bước 1:** Cập nhật biến môi trường hoặc `application.yml`:
```yaml
app:
  ai:
    default-provider: claude  # hoặc gemini
    claude:
      api-key: ${CLAUDE_API_KEY}
      model: claude-sonnet-4-20250514
```

**Bước 2:** Đặt API key:
```bash
export CLAUDE_API_KEY="sk-ant-..."
```

**Bước 3:** Khởi động lại:
```bash
sudo systemctl restart medtender
```

**Không cần thay đổi code!** Strategy pattern tự động load provider tương ứng.

### 5.3 Test AI provider

```bash
curl -X POST http://localhost:8082/api/admin/ai-config/test \
  -H "Authorization: Bearer $TOKEN" \
  -d '"Máy siêu âm có độ phân giải >= 1920x1080, bảo hành >= 24 tháng"'
```

---

## 6. Xử lý sự cố

### 6.1 Backend không khởi động

```bash
# Kiểm tra log
sudo journalctl -u medtender -f

# Kiểm tra port bị chiếm
sudo lsof -i :8082

# Kiểm tra database connection
psql -h localhost -U medbid -d medbid_db -c "SELECT 1"

# Kiểm tra disk space
df -h
```

### 6.2 OCR không hoạt động

```bash
# Kiểm tra Tesseract
tesseract --version
tesseract --list-langs | grep vie

# Kiểm tra log
tail -f /opt/medtender/logs/medtender.log | grep OCR
```

### 6.3 AI extraction lỗi

```bash
# Kiểm tra API key
echo $OPENAI_API_KEY

# Kiểm tra log AI
tail -f /opt/medtender/logs/medtender.log | grep AI

# Nếu OpenAI API key hết hạn → hệ thống tự động fallback sang rule-based parser
# Kiểm tra log: "OpenAI API key not configured. Using rule-based fallback parser."
```

### 6.4 Database connection pool exhausted

```bash
# Tăng pool size trong application.yml
spring.datasource.hikari.maximum-pool-size: 30  # tăng từ mặc định 20

# Kiểm tra active connections
psql -h localhost -U medbid -d medbid_db -c "SELECT count(*) FROM pg_stat_activity"
```

---

## 7. Bảo trì định kỳ

| Tần suất | Công việc | Lệnh |
|----------|-----------|------|
| Hàng ngày | Kiểm tra backup log | `ls -la /opt/medtender/backups/` |
| Hàng tuần | Kiểm tra disk space | `df -h /opt/medtender/` |
| Hàng tuần | Dọn log cũ (>30 ngày) | `find /opt/medtender/logs/ -name "*.log" -mtime +30 -delete` |
| Hàng tháng | Kiểm tra cập nhật bảo mật | `apt list --upgradable` |
| Hàng tháng | Restore test (kiểm tra backup) | Restore vào DB test |
| Hàng quý | Review audit logs | `GET /api/audit?range=...` |
| Hàng quý | Đổi JWT secret | Cập nhật `JWT_SECRET` env var |
| 6 tháng | Cập nhật PostgreSQL version | `pg_upgrade` |

---

## 8. Logging & Audit

### 8.1 Vị trí log

- **Application log:** `/opt/medtender/logs/medtender.log`
- **Rolling policy:** 10MB/file, giữ 30 files (30 ngày)
- **Format:** Timestamp + Level + Thread + Logger + Message

### 8.2 Audit Log

```bash
# Xem audit log 30 ngày gần nhất
curl http://localhost:8082/api/audit \
  -H "Authorization: Bearer $TOKEN"

# Xem audit theo user
curl http://localhost:8082/api/audit/user/{userId} \
  -H "Authorization: Bearer $TOKEN"

# Xem audit theo hành động
curl http://localhost:8082/api/audit/action/DELETE \
  -H "Authorization: Bearer $TOKEN"

# Xem audit theo khoảng thời gian
curl "http://localhost:8082/api/audit/range?start=2026-06-01T00:00:00&end=2026-06-07T23:59:59" \
  -H "Authorization: Bearer $TOKEN"
```

### 8.3 Các thao tác được audit

- CREATE/UPDATE/DELETE entities
- LOGIN_SUCCESS/LOGIN_FAILURE
- PASSWORD_CHANGE / PASSWORD_RESET
- USER_LOCK / USER_UNLOCK
- EXPORT (Word/PDF/ZIP/Excel)
- BACKUP / RESTORE
- MANUAL_OVERRIDE (đối chiếu)

---

## 9. SLA & RTO/RPO

### 9.1 Cam kết SLA

| Chỉ số | Giá trị | Phương thức đảm bảo |
|--------|--------|-------------------|
| **RTO** (Recovery Time Objective) | **60 phút** | Backup pg_dump + restore script |
| **RPO** (Recovery Point Objective) | **1440 phút (24h)** | Scheduled backup 2:00 AM |
| **Uptime** | 99.5% (target) | systemd auto-restart, health check monitoring |
| **Response time** | < 500ms p95 | HikariCP pool, Redis cache, Kafka async |

### 9.2 Quy trình khôi phục thảm họa

```
1. Phát hiện sự cố (monitoring alert)
2. Xác định nguyên nhân (5 phút)
3. Restore database từ backup gần nhất (10 phút)
   └── pg_restore -h localhost -U medbid -d medbid_db -c <backup_file>
4. Khởi động backend (5 phút)
5. Verify health check (5 phút)
6. Thông báo người dùng (5 phút)
────────────────────────────────────
Tổng: ~30 phút (within RTO 60 phút)
```
