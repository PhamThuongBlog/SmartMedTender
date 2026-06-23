# UAT Checklist — Nghiệm Thu Người Dùng

## 1. Đăng Nhập & Bảo Mật

- [ ] Đăng nhập thành công với tài khoản admin
- [ ] Đăng nhập thất bại hiển thị thông báo lỗi
- [ ] Sau 5 lần sai, tài khoản bị khóa
- [ ] Token JWT hoạt động (gọi API với Bearer token)
- [ ] Token hết hạn trả về 401
- [ ] Refresh token hoạt động
- [ ] Đăng xuất hủy refresh token
- [ ] Phân quyền hoạt động đúng (từng role thấy menu khác nhau)

## 2. Quản Lý Gói Thầu

- [ ] Tạo gói thầu mới với đầy đủ thông tin
- [ ] Xem danh sách gói thầu (phân trang)
- [ ] Tìm kiếm/lọc gói thầu theo trạng thái
- [ ] Cập nhật thông tin gói thầu
- [ ] Xóa gói thầu (soft delete)
- [ ] Chuyển trạng thái: DRAFT → REVIEWING → APPROVED → SUBMITTED
- [ ] Clone gói thầu (copy items + requirements)
- [ ] Không thể chuyển về trạng thái trước đó sai quy trình

## 3. Upload & Xử Lý HSMT

- [ ] Upload file PDF/DOCX thành công
- [ ] Hiển thị progress bar khi upload
- [ ] Từ chối file sai định dạng (.exe, .bat...)
- [ ] Từ chối file > 50MB
- [ ] OCR tự động chạy sau upload
- [ ] Hiển thị trạng thái OCR (PENDING → PROCESSING → COMPLETED)
- [ ] Xem kết quả OCR (danh sách yêu cầu kỹ thuật)
- [ ] Chỉnh sửa yêu cầu thủ công
- [ ] Phê duyệt/từ chối từng yêu cầu

## 4. Thư Viện Thiết Bị

- [ ] Thêm thiết bị mới với thông số kỹ thuật
- [ ] Upload catalogue, chứng chỉ
- [ ] Tìm kiếm theo tên, hãng, model
- [ ] Lọc theo danh mục
- [ ] Cập nhật thông tin thiết bị
- [ ] Cảnh báo chứng chỉ sắp hết hạn

## 5. Đối Chiếu Sản Phẩm

- [ ] Chọn gói thầu + sản phẩm để đối chiếu
- [ ] Hiển thị kết quả PASS/FAIL cho từng tiêu chí
- [ ] Hiển thị điểm số tổng thể
- [ ] Ghi chú và override thủ công
- [ ] Tìm sản phẩm tốt nhất cho gói thầu

## 6. Tạo & Xuất HSDT

- [ ] Tạo HSDT từ gói thầu + sản phẩm đã chọn
- [ ] Xuất file Word (.docx) — kiểm tra font tiếng Việt
- [ ] Xuất file PDF — kiểm tra watermark
- [ ] Xuất file ZIP — kiểm tra đủ file bên trong
- [ ] Nội dung đúng format hành chính VN
- [ ] Bảng kỹ thuật có đủ cột: STT, Tên TB, Hãng, Xuất xứ, Thông số...
- [ ] Bảng giá có cột thành tiền và tổng cộng

## 7. Chatbot

- [ ] Hỏi câu có trong FAQ → trả lời đúng
- [ ] Hỏi câu không có → trả lời mặc định
- [ ] Hiển thị câu hỏi liên quan
- [ ] Giao diện chat trực quan

## 8. Thông Báo

- [ ] Nhận thông báo khi có sự kiện mới
- [ ] Đánh dấu đã đọc
- [ ] Đếm số thông báo chưa đọc
- [ ] Click vào thông báo điều hướng đúng

## 9. Dashboard

- [ ] Hiển thị số liệu thống kê chính xác
- [ ] Biểu đồ hiển thị đúng
- [ ] Số liệu cập nhật real-time

## 10. Backup & Restore

- [ ] Backup database thành công
- [ ] Restore database từ file backup
- [ ] Dữ liệu sau restore đầy đủ

## 11. Hiệu Năng

- [ ] Trang tải < 3 giây
- [ ] API response < 500ms (p95)
- [ ] Upload file 10MB thành công
- [ ] Export HSDT < 10 giây (với < 50 yêu cầu)

## Ký Xác Nhận

| Vai trò | Họ tên | Ngày | Ký |
|---------|--------|------|-----|
| Người nghiệm thu | | / / | |
| Quản lý dự án | | / / | |
| Khách hàng | | / / | |
