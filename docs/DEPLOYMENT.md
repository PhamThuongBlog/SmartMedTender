# Hướng Dẫn Triển Khai (Deployment Guide)

## Yêu Cầu Môi Trường

- Docker 24+ và Docker Compose v2+
- JDK 17 (nếu build từ source)
- 4GB RAM tối thiểu cho production
- PostgreSQL 16, Kafka, Redis (nếu không dùng Docker)

## Triển Khai Với Docker Compose (Khuyến Nghị)

### 1. Chuẩn Bị

```bash
# Copy file cấu hình môi trường
cp .env.example .env
# Chỉnh sửa .env với thông tin thực tế:
# - DATASOURCE_PASSWORD: đổi mật khẩu mạnh
# - JWT_SECRET: chuỗi ngẫu nhiên ít nhất 256 bit
# - OPENAI_API_KEY: nếu dùng AI extraction
```

### 2. Build và Khởi Động

```bash
# Build backend JAR
./mvnw clean package -DskipTests

# Khởi động toàn bộ stack
docker compose up -d

# Kiểm tra trạng thái
docker compose ps
curl http://localhost:8082/actuator/health
```

### 3. Kiểm Tra

```bash
# Health check
curl http://localhost:8082/actuator/health

# Login test
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"12345678@Abc"}'

# Swagger UI
open http://localhost:8082/swagger-ui.html
```

### 4. Dừng Hệ Thống

```bash
docker compose down        # Giữ dữ liệu
docker compose down -v     # Xóa tất cả dữ liệu
```

## Triển Khai Không Dùng Docker

### Infrastructure

Cần cài đặt và chạy riêng:
- PostgreSQL 16 (port 5432)
- Apache Kafka + Zookeeper (port 9092, 2181)
- Redis 7 (port 6379)

### Backend

```bash
# Cấu hình biến môi trường
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/medbid_prod
export SPRING_DATASOURCE_PASSWORD=<your-password>
export JWT_SECRET=<random-256-bit-string>

# Chạy với Maven
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Hoặc build JAR và chạy
./mvnw clean package -DskipTests
java -jar target/SmartMedTender-2.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

### Frontend

```bash
cd frontend
npm install
npm run build
# Copy frontend/dist/ vào thư mục web server (nginx/apache)
```

## Giám Sát

- Health check: `GET /actuator/health`
- Metrics: `GET /actuator/metrics`
- Prometheus endpoint: `GET /actuator/prometheus`
- Logs: `docker compose logs -f backend`

## Backup

```bash
# Tự động: cron job chạy lúc 2:00 AM mỗi ngày
# Thủ công:
bash scripts/backup.sh
```

## Rollback

```bash
# Khôi phục database từ backup
bash scripts/restore.sh backups/backup_YYYYMMDD_HHMMSS.sql

# Rollback Docker image
docker compose down
docker tag smartmedtender:vOLD smartmedtender:latest
docker compose up -d
```
