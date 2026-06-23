# HƯỚNG DẪN SỬ DỤNG
## MedTender System V2.0 — User Manual

**Ngày:** 07/06/2026 | **Phiên bản:** 2.0.0

---

## Mục lục

1. [Bắt đầu](#1-bắt-đầu)
2. [1. Thiết lập ban đầu](#2-1-thiết-lập-ban-đầu)
3. [2. Đọc hồ sơ mời thầu (HSMT)](#3-2-đọc-hồ-sơ-mời-thầu-hsmt)
4. [3. Đối chiếu sản phẩm thông minh](#4-3-đối-chiếu-sản-phẩm-thông-minh)
5. [4. Tạo hồ sơ dự thầu (HSDT)](#5-4-tạo-hồ-sơ-dự-thầu-hsdt)
6. [5. Lịch sử & tái sử dụng](#6-5-lịch-sử--tái-sử-dụng)
7. [6. Quản lý tài khoản (Admin)](#7-6-quản-lý-tài-khoản-admin)
8. [7. Quản trị hệ thống (Admin)](#8-7-quản-trị-hệ-thống-admin)
9. [Phím tắt & Mẹo](#9-phím-tắt--mẹo)

---

## 1. Bắt đầu

### 1.1 Đăng nhập

1. Mở trình duyệt, truy cập URL hệ thống (VD: `https://medtender.yourcompany.com`)
2. Nhập **Tên đăng nhập** và **Mật khẩu**
3. Nhấn **Đăng nhập**

**Tài khoản mặc định:** `admin` / `12345678@Abc`  
(Tài khoản Super Admin — có toàn quyền)

### 1.2 Giao diện chính

Sau khi đăng nhập, bạn sẽ thấy:
- **Menu bên trái** — Điều hướng đến tất cả các chức năng
- **Khu vực nội dung** — Hiển thị trang hiện tại
- **Thanh trên cùng** — Thông tin tài khoản, thông báo, cài đặt

### 1.3 Các vai trò người dùng

| Vai trò | Quyền chính |
|--------|------------|
| **Super Admin** | Toàn quyền — quản lý hệ thống, users, backup, AI config |
| **Admin** | Quản lý users, cấu hình, xem tất cả dữ liệu |
| **Manager** | Tạo/sửa/xuất dữ liệu, phê duyệt hồ sơ |
| **Staff** | Nhập liệu, upload HSMT, tạo HSDT |
| **Reviewer** | Kiểm duyệt yêu cầu kỹ thuật đã trích xuất |
| **Legal** | Quản lý hồ sơ pháp lý, chứng chỉ |
| **Sales** | Xem sản phẩm, tạo báo giá, xem lịch sử trúng thầu |

---

## 2. 1. Thiết lập ban đầu

### 2.1 Cấu hình hồ sơ doanh nghiệp

1. Vào menu **Hồ sơ doanh nghiệp** (biểu tượng tòa nhà)
2. Điền thông tin công ty:
   - Tên công ty (tiếng Việt + tiếng Anh)
   - Mã số thuế
   - Địa chỉ, điện thoại, email, website
   - Người đại diện pháp luật + chức vụ
3. Phần **Giấy phép kinh doanh**: số GPKD, cơ quan cấp, ngày cấp, ngày hết hạn
4. Nhấn **Lưu hồ sơ**

### 2.2 Upload tài liệu pháp lý

1. Trong tab **Tài liệu pháp lý**, nhấn **Tải lên**
2. Chọn loại tài liệu: ĐKKD, Thuế, GMP, ISO 9001, CO, CQ, CE, FDA, GSP, GDP, Catalogue...
3. Nhập tên, cơ quan cấp, ngày cấp, ngày hết hạn
4. Chọn file (PDF, DOCX, ảnh)
5. Nhấn **Tải lên**

### 2.3 Thêm sản phẩm vào thư viện

1. Vào menu **Sản phẩm**, nhấn **Thêm sản phẩm**
2. Điền thông tin:
   - Tên sản phẩm (bắt buộc)
   - Hãng sản xuất, thương hiệu, model
   - Xuất xứ, danh mục
   - Số đăng ký lưu hành, ngày cấp, ngày hết hạn
   - Mô tả sản phẩm
3. Tích chọn chứng chỉ: **ISO**, **FDA**, **CE**, **CO/CQ**
4. Nhấn **Lưu**

### 2.4 Quản lý chứng chỉ sản phẩm (CO/CQ/ISO/CE/FDA/Catalogue)

1. Vào menu **Thư viện tài liệu**
2. Nhấn **Tải lên tài liệu**
3. Chọn sản phẩm, loại tài liệu, tên, cơ quan cấp, ngày cấp/hết hạn
4. Chọn file và nhấn **Tải lên**
5. Sử dụng bộ lọc để tìm CO, CE, ISO... riêng biệt

### 2.5 Theo dõi cảnh báo hết hạn

1. Vào menu **Cảnh báo hết hạn**
2. Hệ thống hiển thị:
   - **< 30 ngày** (CRITICAL — đỏ)
   - **30-60 ngày** (WARNING — vàng)
   - **60-90 ngày** (INFO — xanh)
   - **Đã hết hạn** (EXPIRED)
3. Nhấn **Kiểm tra ngay** để quét lại toàn bộ
4. Nhấn **Bỏ qua** để dismiss cảnh báo

---

## 3. 2. Đọc hồ sơ mời thầu (HSMT)

### 3.1 Tạo gói thầu mới

1. Vào menu **Gói thầu** → **Thêm gói thầu**
2. Điền thông tin:
   - Tên gói thầu
   - Mã gói thầu, bên mời thầu
   - Hạn nộp hồ sơ, ngày mở thầu
   - Giá dự toán
3. Nhấn **Lưu**

### 3.2 Upload file HSMT

1. Vào menu **Tải lên HSMT**
2. **Bước 1:** Chọn gói thầu từ dropdown
3. **Bước 2:** Kéo-thả hoặc chọn file HSMT
   - Hỗ trợ: PDF, DOCX, DOC, XLSX, XLS, ZIP, PNG, JPG
   - Tối đa **50MB**/file
   - Có thể upload **nhiều file** cùng lúc
4. Nhấn **Tải lên & Trích xuất**
5. Hệ thống sẽ:
   - OCR trích xuất văn bản
   - AI phân tích yêu cầu kỹ thuật
   - Hiển thị kết quả (số yêu cầu trích xuất, bắt buộc, độ tin cậy)

### 3.3 Xem xét và chỉnh sửa yêu cầu (OCR Review)

1. Vào menu **So sánh kỹ thuật** → hoặc **Xem xét yêu cầu kỹ thuật**
2. Chọn gói thầu từ dropdown
3. Hệ thống hiển thị danh sách yêu cầu với các cột:
   - **Mô tả** — Nội dung yêu cầu
   - **Loại** — TECHNICAL, CERTIFICATION, EXPERIENCE...
   - **Toán tử** — >=, <=, =, CONTAINS...
   - **Giá trị** + **Đơn vị**
   - **Bắt buộc** — checkmark nếu mandatory
   - **Độ tin cậy** — AI confidence score
   - **Trạng thái** — Đã trích xuất / Đã duyệt / Từ chối
4. **Chỉnh sửa:** Nhấn đúp vào dòng để sửa trực tiếp
5. **Phê duyệt:** Nhấn nút ✓ (xanh) → VERIFIED
6. **Từ chối:** Nhấn nút ✗ (đỏ) → REJECTED
7. **Phê duyệt tất cả:** Nhấn nút "Phê duyệt tất cả"

---

## 4. 3. Đối chiếu sản phẩm thông minh

### 4.1 So sánh sản phẩm với gói thầu

1. Vào menu **So sánh kỹ thuật**
2. Chọn **Gói thầu** và **Sản phẩm**
3. Nhấn **So sánh**

Trang kết quả hiển thị:

| Khu vực | Mô tả |
|---------|-------|
| **Điểm phù hợp tổng quan** | Knob gauge (xanh ≥ 80%, vàng ≥ 50%, đỏ < 50%) + passed/failed/partial |
| **Tình trạng chứng chỉ** | ISO ✓/✗, CE ✓/✗, FDA ✓/✗, CO/CQ ✓/✗ — kiểm tra compliance |
| **Giá gợi ý** | Dựa trên lịch sử trúng thầu (có confidence level) |
| **Bảng chi tiết** | Từng yêu cầu: mô tả, yêu cầu, thực tế, kết quả, điểm, ghi chú |
| **Cảnh báo** | Thiếu chứng chỉ, tiêu chí không đạt |

### 4.2 Gợi ý sản phẩm thông minh (Smart Suggest)

- Sau khi chọn gói thầu, hệ thống tự động gợi ý **top sản phẩm phù hợp**
- Hiển thị: tên SP, chứng chỉ, điểm, đạt/tổng, **giá gợi ý**, cảnh báo
- Nhấn nút ▶ để so sánh chi tiết

### 4.3 Phân tích khoảng trống (Gap Analysis)

1. Sau khi so sánh, nhấn nút **Gap Analysis**
2. Hệ thống hiển thị modal:
   - **Tiêu chí không đạt** — danh sách + khuyến nghị
   - **Tài liệu/chứng chỉ thiếu** — danh sách
   - **Chứng chỉ hết hạn** — danh sách
   - **Khuyến nghị hành động** — các bước cần làm
   - **Giá gợi ý** — phân tích chi tiết

### 4.4 Ghi đè thủ công (Manual Override)

1. Trong bảng chi tiết đối chiếu, tìm cột **Ghi đè**
2. Nhấn nút bút chì
3. Bật/tắt toggle **Đạt/Ko**
4. Nhập lý do ghi đè
5. Hệ thống tự động lưu — kết quả hiển thị badge `[ĐÃ GHI ĐÈ]`

---

## 5. 4. Tạo hồ sơ dự thầu (HSDT)

### 5.1 Quy trình 4 bước

#### Bước 1: Chọn gói thầu
- Chọn gói thầu cần dự thầu từ dropdown
- Xem preview thông tin gói thầu
- Nhấn **Tiếp tục → Chọn sản phẩm**

#### Bước 2: Chọn sản phẩm
- Dual-pane: **Sản phẩm có sẵn** (trái) ↔ **Đã chọn** (phải)
- Mỗi sản phẩm hiển thị: tên, hãng, chứng chỉ (ISO/CE/FDA/CQ badges)
- Nhấn vào sản phẩm để chọn/bỏ chọn
- Dùng ô tìm kiếm để lọc sản phẩm
- Nhấn **Tiếp tục → Xem xét**

#### Bước 3: Xem xét hồ sơ
Hệ thống tự động tổng hợp:

- **Bảng so sánh kỹ thuật:** Sản phẩm, điểm phù hợp, Đạt/TC, chứng chỉ, giá
- **Smart Checklist:** 41+ mục chia 5 section:
  - I. Tài liệu hành chính (Đơn dự thầu, GPKD, báo cáo tài chính...)
  - II. Tài liệu kỹ thuật (Catalog, hướng dẫn sử dụng, tiêu chí bắt buộc...)
  - III. Chứng chỉ (CO/CQ/ISO/CE/FDA — trạng thái từng cái)
  - IV. Tài chính (Bảng giá, bảo lãnh dự thầu...)
  - V. Khác (Biên bản khảo sát, hợp đồng tương tự...)
- **Hồ sơ pháp lý doanh nghiệp:** Tên công ty, MST, địa chỉ, người đại diện
- **Tổng giá dự kiến**

Mỗi mục checklist có:
- ✓ (xanh) = OK
- ⚠ (vàng) = Cảnh báo
- ✗ (đỏ) = Thiếu
- ⏰ (đỏ) = Hết hạn

#### Bước 4: Xuất hồ sơ
- **Word (.docx)** — Bảng so sánh kỹ thuật + thông tin gói thầu
- **PDF (.pdf)** — Có watermark "TÀI LIỆU NỘI BỘ"
- **Excel (.xlsx)** — Sheet thông số + sheet bảng giá
- **ZIP (đầy đủ)** — DOCX + PDF + checklist.txt + metadata.json

File tải về có tên: `HSDT_{tên_gói_thầu}.{định_dạng}`

---

## 6. 5. Lịch sử & tái sử dụng

### 6.1 Sao chép gói thầu cũ

1. Vào menu **Gói thầu**
2. Tìm gói thầu cần sao chép
3. Vào **Chi tiết gói thầu**
4. Nhấn **Sao chép** (Clone)
5. Hệ thống tạo bản sao mới với tên `"Tên gói thầu (Bản sao)"` ở trạng thái DRAFT
6. Bản sao giữ nguyên: mô tả, hạng mục, yêu cầu kỹ thuật

### 6.2 Ghi nhận kết quả trúng/trượt

1. Vào **Chi tiết gói thầu** đã nộp
2. Nhấn **Đánh dấu trúng thầu** hoặc **Đánh dấu trượt**
3. Nếu trúng: nhập **giá trúng thầu** (VND)
4. Hệ thống tự động:
   - Cập nhật trạng thái → WON/LOST
   - Lưu giá vào **PriceHistory** để tái sử dụng cho gợi ý giá sau này

### 6.3 Xem lịch sử gói thầu

1. Vào menu **Gói thầu**
2. Sử dụng bộ lọc trạng thái: WON/LOST
3. Xem danh sách gói thầu đã có kết quả
4. Mỗi gói thầu hiển thị: tên, mã gói, bên mời thầu, giá trúng, ngày

---

## 7. 6. Quản lý tài khoản (Admin)

### 7.1 Danh sách người dùng

1. Vào menu **Quản lý người dùng** (chỉ hiển thị cho Admin/Super Admin)
2. Bảng hiển thị: username, họ tên, email, vai trò, trạng thái
3. **Tìm kiếm:** Nhập tên/username vào ô tìm kiếm
4. **Lọc:** Theo vai trò hoặc trạng thái (hoạt động/vô hiệu)

### 7.2 Tạo tài khoản mới

1. Nhấn **Thêm người dùng**
2. Điền thông tin:
   - **Tên đăng nhập** (bắt buộc, unique)
   - **Mật khẩu** (tối thiểu 8 ký tự)
   - **Email**, Họ tên, Số điện thoại
   - **Vai trò** — chọn 1 trong 7 vai trò
3. Nhấn **Tạo mới**

### 7.3 Khóa/Mở khóa tài khoản

- Nhấn biểu tượng 🔒 để khóa (người dùng không thể đăng nhập)
- Nhấn biểu tượng 🔓 để mở khóa
- Tài khoản tự động khóa sau **5 lần đăng nhập sai**

### 7.4 Đặt lại mật khẩu

1. Nhấn biểu tượng 🔑 trên dòng người dùng
2. Nhập mật khẩu mới (tối thiểu 8 ký tự)
3. Nhấn **Đặt lại**
4. Người dùng sẽ phải đăng nhập lại với mật khẩu mới

---

## 8. 7. Quản trị hệ thống (Admin)

### 8.1 Cấu hình AI Provider

1. Vào API `/api/admin/ai-config` (qua Swagger UI hoặc trực tiếp)
2. Xem thông tin: provider hiện tại, các provider có sẵn
3. Để chuyển đổi: cập nhật cấu hình + restart backend (xem Hướng dẫn vận hành)

### 8.2 Sao lưu dữ liệu

1. Vào menu **Cài đặt** → **Sao lưu**
2. Nhấn **Sao lưu ngay** để tạo backup thủ công
3. Nhấn **Off-site Backup** để copy đến vị trí off-site
4. Xem SLA: RTO/RPO hiện tại

### 8.3 Xem nhật ký (Audit Log)

1. API có sẵn tại `/api/audit`
2. Lọc theo: user, entity type, action, khoảng thời gian
3. Mỗi bản ghi audit gồm: user, hành động, entity, giá trị cũ/mới, IP, thời gian

### 8.4 Trung tâm xuất (Export Center)

1. Vào menu **Xuất tài liệu**
2. Tab **Lịch sử xuất** — xem các file đã xuất trước đây
3. Tab **Xuất nhanh** — chọn gói thầu → xuất Word/PDF/ZIP ngay

---

## 9. Phím tắt & Mẹo

### 9.1 Điều hướng nhanh

| Hành động | Cách thực hiện |
|-----------|---------------|
| Tìm kiếm trong bảng | Nhập vào ô tìm kiếm (tự động debounce 300ms) |
| Lọc trong dropdown | Gõ trực tiếp vào dropdown (PrimeVue filter) |
| Xem chi tiết sản phẩm/gói thầu | Nhấn vào tên trong bảng |
| Sắp xếp cột | Nhấn vào tiêu đề cột |
| Phân trang | Sử dụng paginator ở cuối bảng |

### 9.2 Mẹo sử dụng

1. **Thiết lập đầy đủ hồ sơ doanh nghiệp trước** — checklist HSDT sẽ tự động kiểm tra
2. **Upload đầy đủ chứng chỉ sản phẩm** — hệ thống sẽ tự động compliance check
3. **Sử dụng Gap Analysis** trước khi tạo HSDT để biết còn thiếu gì
4. **Ghi nhận kết quả trúng/trượt** sau mỗi lần dự thầu — dữ liệu này giúp gợi ý giá chính xác hơn cho lần sau
5. **Sao chép gói thầu cũ** nếu dự thầu lại cùng bên mời thầu — tiết kiệm thời gian nhập liệu

### 9.3 Định dạng file hỗ trợ

| Loại | Định dạng |
|------|-----------|
| Upload HSMT | PDF, DOCX, DOC, XLSX, XLS, ZIP, PNG, JPG, JPEG |
| Upload tài liệu | PDF, DOCX, DOC, PNG, JPG, JPEG |
| Xuất hồ sơ | DOCX (Word), PDF, XLSX (Excel), ZIP |
| Kích thước tối đa | 50MB/file |

---

## Phụ lục: Vòng đời dự thầu điển hình

```
Ngày 1: Thiết lập ban đầu
├── 1.1 Cấu hình hồ sơ doanh nghiệp (1 lần)
├── 1.2 Nhập thư viện sản phẩm (1 lần)
└── 1.3 Upload chứng chỉ CO/CQ/ISO/CE/FDA (định kỳ)

Ngày 2-3: Nhận HSMT → Phân tích
├── 2.1 Tạo gói thầu mới
├── 2.2 Upload file HSMT (PDF/Word)
├── 2.3 Hệ thống OCR + AI trích xuất yêu cầu
└── 2.4 Review & phê duyệt yêu cầu (OCR Review)

Ngày 3-5: Đối chiếu sản phẩm
├── 3.1 Smart suggest → chọn sản phẩm phù hợp
├── 3.2 So sánh chi tiết từng sản phẩm với yêu cầu
├── 3.3 Gap analysis → xác định khoảng trống
├── 3.4 Manual override nếu cần
└── 3.5 Xem giá gợi ý từ lịch sử

Ngày 5-7: Tạo & nộp HSDT
├── 4.1 Tạo HSDT (chọn gói thầu + sản phẩm)
├── 4.2 Kiểm tra checklist → bổ sung tài liệu thiếu
├── 4.3 Xuất Word/PDF/ZIP
└── 4.4 Nộp hồ sơ cho bên mời thầu

Sau khi có kết quả:
├── 5.1 Ghi nhận trúng/trượt + giá
└── 5.2 Dữ liệu giá → tái sử dụng cho gói thầu sau
```
