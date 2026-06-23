# Checklist Bàn Giao Hệ Thống

## 1. Mã Nguồn
- [x] Backend Spring Boot source code
- [x] Frontend VueJS source code
- [x] Flyway migration scripts
- [x] Docker deployment files
- [x] CI/CD pipeline (GitHub Actions)

## 2. Cơ Sở Dữ Liệu
- [x] PostgreSQL schema (17 bảng)
- [x] Flyway migration (V1__init_schema.sql)
- [x] Default data (roles, permissions, admin user, FAQ)
- [x] UUID primary keys
- [x] Soft delete + audit columns

## 3. Chức Năng
- [x] Đăng nhập/đăng xuất (JWT)
- [x] Phân quyền RBAC (7 roles)
- [x] Quản lý hồ sơ doanh nghiệp
- [x] Thư viện thiết bị y tế
- [x] Upload HSMT + OCR
- [x] AI trích xuất yêu cầu
- [x] Đối chiếu sản phẩm thông minh
- [x] Tạo HSDT tự động
- [x] Xuất Word/PDF/ZIP
- [x] Quản lý giá & báo giá
- [x] Workflow gói thầu
- [x] Chatbot FAQ
- [x] Thông báo (in-app)
- [x] Dashboard thống kê
- [x] Audit log
- [x] Backup/Restore

## 4. Bảo Mật
- [x] BCrypt password encoding
- [x] JWT access + refresh token
- [x] RBAC phân quyền chi tiết
- [x] Rate limiting (100 req/phút/IP)
- [x] File validation (MIME + extension)
- [x] SQL injection protection (JPA)
- [x] XSS protection
- [x] Secure headers
- [x] CORS configuration

## 5. DevOps
- [x] Dockerfile (backend)
- [x] docker-compose.yml (full stack)
- [x] nginx.conf
- [x] .env.example
- [x] backup.sh / restore.sh / deploy.sh
- [x] Jenkinsfile
- [x] GitHub Actions CI/CD

## 6. Monitoring
- [x] Spring Boot Actuator
- [x] Prometheus metrics endpoint
- [x] Health check endpoints
- [x] Centralized logging (Logback)
- [x] Kafka monitoring

## 7. Tài Liệu
- [x] README.md
- [x] DEPLOYMENT.md (hướng dẫn triển khai)
- [x] ARCHITECTURE.md (kiến trúc)
- [x] USER_MANUAL.md (hướng dẫn sử dụng)
- [x] Swagger/OpenAPI docs (tự động)
- [ ] Backup/restore guide (trong DEPLOYMENT.md)
- [ ] UAT checklist
- [ ] SLA checklist
- [ ] Security audit checklist

## 8. Nghiệm Thu
- [ ] Build thành công (./mvnw clean package)
- [ ] Docker compose up thành công
- [ ] Health check OK
- [ ] Login hoạt động
- [ ] Upload HSMT hoạt động
- [ ] OCR pipeline hoạt động
- [ ] Matching hoạt động
- [ ] Export Word/PDF/ZIP không lỗi font
- [ ] Backup/restore hoạt động
