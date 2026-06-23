# HƯỚNG DẪN TRIỂN KHAI & ĐÓNG GÓI
## MedTender System V2.0

**Ngày:** 07/06/2026 | **Phiên bản:** 2.0.0

---

## Mục lục

1. [Yêu cầu hệ thống](#1-yêu-cầu-hệ-thống)
2. [Cài đặt môi trường](#2-cài-đặt-môi-trường)
3. [Cấu hình ứng dụng](#3-cấu-hình-ứng-dụng)
4. [Deploy với Docker Compose](#4-deploy-với-docker-compose)
5. [Deploy Production (Manual)](#5-deploy-production-manual)
6. [Deploy với Nginx Reverse Proxy + HTTPS](#6-deploy-với-nginx-reverse-proxy--https)
7. [Đóng gói ứng dụng](#7-đóng-gói-ứng-dụng)

---

## 1. Yêu cầu hệ thống

### Production

| Thành phần | Yêu cầu tối thiểu | Khuyến nghị |
|-----------|-------------------|------------|
| OS | Ubuntu 22.04+ / Windows Server 2019+ | Ubuntu 24.04 LTS |
| CPU | 2 cores | 4 cores |
| RAM | 4 GB | 8 GB |
| Disk | 20 GB | 50 GB SSD |
| Java | JDK 17 | OpenJDK 17.0.x |
| PostgreSQL | 14+ | 16 |
| Kafka | 3.x | 3.x (embedded via Docker) |
| Redis | 7.x | 7.x (Docker) |
| Tesseract | 4.x / 5.x | 5.x (có `vie` trained data) |
| Node.js | 18+ | 22 LTS |

### Development

- Java 17, Maven 3.9+
- Node.js 18+, npm 9+
- Docker Desktop
- Git

---

## 2. Cài đặt môi trường

### 2.1 Cài đặt Tesseract OCR (cho tính năng đọc HSMT)

**Ubuntu:**
```bash
sudo apt-get update
sudo apt-get install -y tesseract-ocr tesseract-ocr-vie tesseract-ocr-eng
echo $TESSDATA_PREFIX  # should be /usr/share/tesseract-ocr/4.00/tessdata
```

**Windows:**
- Tải Tesseract từ https://github.com/UB-Mannheim/tesseract/wiki
- Cài đặt vào `C:\Program Files\Tesseract-OCR\`
- Thêm vào PATH
- Tải `vie.traineddata` từ https://github.com/tesseract-ocr/tessdata vào thư mục tessdata

**Docker (alternative):** OCR được tích hợp sẵn trong Docker image (cài Tesseract trong Dockerfile)

### 2.2 Cài đặt PostgreSQL

```bash
# Ubuntu
sudo apt-get install -y postgresql postgresql-client
sudo -u postgres psql -c "CREATE DATABASE medbid_db;"
sudo -u postgres psql -c "CREATE USER medbid WITH PASSWORD 'your_password';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE medbid_db TO medbid;"
```

### 2.3 Cài đặt Node.js + npm

```bash
# Ubuntu
curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
sudo apt-get install -y nodejs
```

---

## 3. Cấu hình ứng dụng

### 3.1 Cấu hình Backend (`application.yml`)

Các biến môi trường quan trọng:

```bash
# Database
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/medbid_db
export SPRING_DATASOURCE_USERNAME=medbid
export SPRING_DATASOURCE_PASSWORD=your_password

# JWT Secret (BẮT BUỘC thay đổi trong production!)
export JWT_SECRET="your-256-bit-secret-key-change-in-production"

# AI Provider
export OPENAI_API_KEY="sk-your-openai-api-key"
# export GEMINI_API_KEY="..."  # khi chuyển sang Gemini
# export CLAUDE_API_KEY="..."  # khi chuyển sang Claude

# File upload
export FILE_UPLOAD_DIR=/var/medtender/uploads

# Backup
export BACKUP_DIR=/var/medtender/backups
export BACKUP_OFFSITE_DIR=/mnt/offsite-backup/medtender
export BACKUP_RTO=60
export BACKUP_RPO=1440

# CORS (production)
export CORS_ALLOWED_ORIGINS=https://medtender.yourcompany.com
```

### 3.2 Cấu hình Production Profile

Tạo file `application-prod.yml` hoặc sử dụng biến môi trường:

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
  jpa:
    hibernate:
      ddl-auto: validate  # KHÔNG dùng update trong production!
  flyway:
    enabled: true  # migration tự động

app:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS}
    allow-all-origins: false
  jwt:
    secret: ${JWT_SECRET}
```

### 3.3 Cấu hình Frontend

Cập nhật `frontend/vite.config.js` cho production:

```js
export default defineConfig({
  server: {
    proxy: {
      '/api': 'http://localhost:8082'  // backend URL
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets'
  }
})
```

---

## 4. Deploy với Docker Compose

### 4.1 docker-compose.yml

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: medbid_db
      POSTGRES_USER: medbid
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on: [zookeeper]
    ports:
      - "9092:9092"
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  backend:
    build: .
    ports:
      - "8082:8082"
    depends_on: [postgres, kafka, redis]
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/medbid_db
      - SPRING_DATASOURCE_USERNAME=medbid
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - OPENAI_API_KEY=${OPENAI_API_KEY}
    volumes:
      - uploads:/app/uploads
      - backups:/app/backups

  frontend:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    depends_on: [backend]
    volumes:
      - ./frontend/dist:/usr/share/nginx/html
      - ./nginx.conf:/etc/nginx/conf.d/default.conf
      - ./ssl:/etc/nginx/ssl

volumes:
  pgdata:
  uploads:
  backups:
```

### 4.2 Dockerfile (Backend)

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
RUN apk add --no-cache tesseract-ocr tesseract-ocr-data-vie
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 4.3 Khởi động

```bash
# Dev mode
docker compose up -d postgres zookeeper kafka redis
./mvnw spring-boot:run
cd frontend && npm run dev

# Production
docker compose up -d
```

---

## 5. Deploy Production (Manual)

### 5.1 Build Backend

```bash
cd Code/SmartMedTender

# Build JAR
./mvnw clean package -DskipTests

# JAR file: target/SmartMedTender-2.0.0-SNAPSHOT.jar

# Run
java -jar target/SmartMedTender-2.0.0-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --server.port=8082
```

### 5.2 Build Frontend

```bash
cd Code/SmartMedTender/frontend
npm install
npm run build

# Output: dist/  (static files)
```

### 5.3 Systemd Service (Linux)

```ini
# /etc/systemd/system/medtender.service
[Unit]
Description=MedTender Backend
After=network.target postgresql.service

[Service]
Type=simple
User=medtender
WorkingDirectory=/opt/medtender
ExecStart=/usr/bin/java -jar /opt/medtender/app.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl enable medtender
sudo systemctl start medtender
```

---

## 6. Deploy với Nginx Reverse Proxy + HTTPS

### 6.1 nginx.conf

```nginx
server {
    listen 80;
    server_name medtender.yourcompany.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name medtender.yourcompany.com;

    ssl_certificate     /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # Frontend static files
    root /var/www/medtender;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # Backend API proxy
    location /api/ {
        proxy_pass http://localhost:8082;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Upload size limit
        client_max_body_size 50M;
    }

    # WebSocket support (nếu cần)
    location /ws/ {
        proxy_pass http://localhost:8082;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

### 6.2 SSL Certificate (Let's Encrypt)

```bash
sudo apt-get install certbot python3-certbot-nginx
sudo certbot --nginx -d medtender.yourcompany.com
sudo certbot renew --dry-run  # test auto-renewal
```

---

## 7. Đóng gói ứng dụng

### 7.1 Cấu trúc thư mục production

```
/opt/medtender/
├── app.jar                          ← Backend JAR
├── config/
│   └── application-prod.yml         ← Production config
├── uploads/                         ← File upload directory
├── backups/                         ← Local backup directory
│   └── offsite/                     ← Off-site backup mirror
├── logs/                            ← Application logs
│   └── medtender.log
└── frontend-dist/                   ← Frontend static files
    ├── index.html
    └── assets/
```

### 7.2 Build script (`build.sh`)

```bash
#!/bin/bash
echo "=== MedTender Build Script ==="

# Build backend
echo "Building backend..."
cd Code/SmartMedTender
./mvnw clean package -DskipTests
cp target/SmartMedTender-*.jar /opt/medtender/app.jar

# Build frontend
echo "Building frontend..."
cd frontend
npm ci
npm run build
cp -r dist/* /var/www/medtender/

# Apply database migrations
echo "Running migrations..."
java -jar /opt/medtender/app.jar --spring.flyway.enabled=true

echo "Build complete!"
```

### 7.3 Health check

```bash
# Kiểm tra backend
curl http://localhost:8082/api/health

# Kiểm tra frontend
curl http://localhost/

# Kiểm tra database
curl http://localhost:8082/api/products?size=1 -H "Authorization: Bearer $(curl -s -X POST http://localhost:8082/api/auth/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"your_password"}' | jq -r '.accessToken')"
```
