param(
    [string]$OutputDir = "D:\NCKH_2027\Tien_ChuHai\MedTenderSystem_VERSION2\Code\SmartMedTender\uploads"
)

$ErrorActionPreference = "Stop"

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

$docxDir = Join-Path $OutputDir "temp_docx"
if (Test-Path $docxDir) { Remove-Item -Recurse -Force $docxDir }
New-Item -ItemType Directory -Force -Path $docxDir | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $docxDir "_rels") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $docxDir "word\_rels") | Out-Null

# [Content_Types].xml
$contentTypes = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
  <Override PartName="/word/settings.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml"/>
</Types>
"@
[System.IO.File]::WriteAllText((Join-Path $docxDir "[Content_Types].xml"), $contentTypes, [System.Text.Encoding]::UTF8)

# _rels/.rels
$rels = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>
"@
[System.IO.File]::WriteAllText((Join-Path $docxDir "_rels\.rels"), $rels, [System.Text.Encoding]::UTF8)

# word/_rels/document.xml.rels
$docRels = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings" Target="settings.xml"/>
</Relationships>
"@
[System.IO.File]::WriteAllText((Join-Path $docxDir "word\_rels\document.xml.rels"), $docRels, [System.Text.Encoding]::UTF8)

# word/settings.xml (minimal, required by some Word versions)
$settings = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:settings xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"/>
"@
[System.IO.File]::WriteAllText((Join-Path $docxDir "word\settings.xml"), $settings, [System.Text.Encoding]::UTF8)

# word/styles.xml
$styles = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:docDefaults>
    <w:rPrDefault><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman" w:cs="Times New Roman"/><w:sz w:val="22"/><w:szCs w:val="22"/><w:lang w:val="vi-VN"/></w:rPr></w:rPrDefault>
    <w:pPrDefault><w:pPr><w:spacing w:line="300" w:lineRule="auto"/></w:pPr></w:pPrDefault>
  </w:docDefaults>
  <w:style w:type="paragraph" w:styleId="Normal" w:default="1">
    <w:name w:val="Normal"/>
    <w:qFormat/>
    <w:pPr><w:spacing w:after="120" w:line="300" w:lineRule="auto"/></w:pPr>
    <w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="22"/></w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading1">
    <w:name w:val="heading 1"/>
    <w:basedOn w:val="Normal"/>
    <w:next w:val="Normal"/>
    <w:qFormat/>
    <w:pPr><w:spacing w:before="480" w:after="120"/></w:pPr>
    <w:rPr><w:rFonts w:ascii="Arial" w:hAnsi="Arial" w:cs="Arial"/><w:b/><w:bCs/><w:sz w:val="32"/><w:szCs w:val="32"/></w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading2">
    <w:name w:val="heading 2"/>
    <w:basedOn w:val="Normal"/>
    <w:next w:val="Normal"/>
    <w:qFormat/>
    <w:pPr><w:spacing w:before="200" w:after="100"/></w:pPr>
    <w:rPr><w:rFonts w:ascii="Arial" w:hAnsi="Arial" w:cs="Arial"/><w:b/><w:bCs/><w:sz w:val="28"/><w:szCs w:val="28"/></w:rPr>
  </w:style>
</w:styles>
"@
[System.IO.File]::WriteAllText((Join-Path $docxDir "word\styles.xml"), $styles, [System.Text.Encoding]::UTF8)

# word/document.xml
$document = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
            xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
            xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
            xmlns:xml="http://www.w3.org/XML/1998/namespace">
  <w:body>
    <w:p>
      <w:pPr><w:jc w:val="center"/></w:pPr>
      <w:r><w:rPr><w:b/><w:sz w:val="36"/><w:rFonts w:ascii="Arial" w:hAnsi="Arial"/></w:rPr><w:t xml:space="preserve">CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM</w:t></w:r>
    </w:p>
    <w:p>
      <w:pPr><w:jc w:val="center"/></w:pPr>
      <w:r><w:rPr><w:b/><w:sz w:val="36"/><w:rFonts w:ascii="Arial" w:hAnsi="Arial"/></w:rPr><w:t xml:space="preserve">Độc lập - Tự do - Hạnh phúc</w:t></w:r>
    </w:p>
    <w:p>
      <w:pPr><w:jc w:val="center"/><w:spacing w:before="200"/></w:pPr>
      <w:r><w:rPr><w:b/><w:sz w:val="32"/><w:rFonts w:ascii="Arial" w:hAnsi="Arial"/></w:rPr><w:t xml:space="preserve">HỒ SƠ MỜI THẦU</w:t></w:r>
    </w:p>
    <w:p>
      <w:pPr><w:jc w:val="center"/><w:spacing w:before="100" w:after="200"/></w:pPr>
      <w:r><w:rPr><w:b/><w:sz w:val="28"/><w:rFonts w:ascii="Arial" w:hAnsi="Arial"/></w:rPr><w:t xml:space="preserve">(E-HSMT)</w:t></w:r>
    </w:p>

    <w:p>
      <w:pPr><w:jc w:val="center"/><w:spacing w:before="300"/></w:pPr>
      <w:r><w:rPr><w:b/><w:sz w:val="32"/><w:rFonts w:ascii="Arial" w:hAnsi="Arial"/></w:rPr><w:t xml:space="preserve">GÓI THẦU: MUA SẮM THIẾT BỊ Y TẾ</w:t></w:r>
    </w:p>
    <w:p>
      <w:pPr><w:jc w:val="center"/><w:spacing w:before="100"/></w:pPr>
      <w:r><w:rPr><w:b/><w:sz w:val="28"/><w:rFonts w:ascii="Arial" w:hAnsi="Arial"/></w:rPr><w:t xml:space="preserve">BỆNH VIỆN ĐA KHOA TRUNG ƯƠNG</w:t></w:r>
    </w:p>
    <w:p>
      <w:pPr><w:jc w:val="center"/><w:spacing w:before="100" w:after="300"/></w:pPr>
      <w:r><w:rPr><w:sz w:val="24"/></w:rPr><w:t xml:space="preserve">Số hiệu gói thầu: TBYT-2026-001</w:t></w:r>
    </w:p>

    <w:p><w:pPr><w:pStyle w:val="Heading1"/></w:pPr><w:r><w:t xml:space="preserve">PHẦN 1: THÔNG TIN CHUNG</w:t></w:r></w:p>

    <w:p><w:r><w:t xml:space="preserve">1.1. Tên gói thầu: Mua sắm thiết bị y tế chuyên khoa năm 2026</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">1.2. Tên dự án: Dự án đầu tư nâng cấp trang thiết bị y tế Bệnh viện Đa khoa Trung ương giai đoạn 2025-2027</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">1.3. Bên mời thầu: Bệnh viện Đa khoa Trung ương</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">1.4. Địa chỉ: Số 1, Đường Giải Phóng, Quận Hai Bà Trưng, TP. Hà Nội</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">1.5. Điện thoại: (024) 3869 3731 | Fax: (024) 3869 1607</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">1.6. Email: benhvien@dktu.gov.vn</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">1.7. Loại hợp đồng: Hợp đồng trọn gói</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">1.8. Thời gian thực hiện hợp đồng: 120 ngày kể từ ngày ký kết</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">1.9. Nguồn vốn: Ngân sách Nhà nước</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">1.10. Hình thức lựa chọn nhà thầu: Đấu thầu rộng rãi trong nước</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">1.11. Phương thức đấu thầu: Một giai đoạn hai túi hồ sơ</w:t></w:r></w:p>

    <w:p><w:pPr><w:pStyle w:val="Heading1"/></w:pPr><w:r><w:t xml:space="preserve">PHẦN 2: PHẠM VI CUNG CẤP</w:t></w:r></w:p>
    <w:p><w:pPr><w:pStyle w:val="Heading2"/></w:pPr><w:r><w:t xml:space="preserve">2.1. Danh mục thiết bị y tế cần mua sắm</w:t></w:r></w:p>

    <w:p><w:r><w:rPr><w:b/></w:rPr><w:t xml:space="preserve">Gói thầu bao gồm các thiết bị y tế sau:</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">a) Máy chụp cộng hưởng từ (MRI) 1.5 Tesla - Số lượng: 01 hệ thống</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">b) Máy chụp cắt lớp vi tính (CT Scanner) 128 lát cắt - Số lượng: 01 hệ thống</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">c) Máy X-quang kỹ thuật số (DR) - Số lượng: 02 hệ thống</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">d) Máy siêu âm Doppler màu 4D - Số lượng: 02 hệ thống</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">e) Hệ thống nội soi tiêu hóa HD - Số lượng: 01 hệ thống</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">f) Hệ thống nội soi phẫu thuật 4K - Số lượng: 01 hệ thống</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">g) Máy gây mê kèm thở - Số lượng: 04 hệ thống</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">h) Monitor theo dõi bệnh nhân đa thông số - Số lượng: 20 hệ thống</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">i) Bơm tiêm điện - Số lượng: 30 thiết bị</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">j) Máy phân tích huyết học tự động - Số lượng: 02 hệ thống</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">k) Máy xét nghiệm sinh hóa tự động - Số lượng: 02 hệ thống</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">l) Tủ an toàn sinh học cấp II - Số lượng: 05 thiết bị</w:t></w:r></w:p>

    <w:p><w:pPr><w:pStyle w:val="Heading1"/></w:pPr><w:r><w:t xml:space="preserve">PHẦN 3: YÊU CẦU KỸ THUẬT</w:t></w:r></w:p>

    <w:p><w:pPr><w:pStyle w:val="Heading2"/></w:pPr><w:r><w:t xml:space="preserve">3.1. Máy chụp cộng hưởng từ (MRI) 1.5 Tesla</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Loại nam châm siêu dẫn, từ lực 1.5 Tesla</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Đường kính lòng nam châm >= 60 cm</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Độ đồng nhất từ trường: <= 1.0 ppm V-RMS trên 24cm DSV</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Gradient: biên độ >= 33 mT/m, slew rate >= 120 T/m/s</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Số kênh thu nhận >= 16 kênh</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Hỗ trợ chụp toàn thân, sọ não, cột sống, khớp, mạch máu</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Phần mềm xử lý hình ảnh DICOM 3.0</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Tiêu chuẩn FDA 510(k) hoặc CE Mark (Châu Âu)</w:t></w:r></w:p>

    <w:p><w:pPr><w:pStyle w:val="Heading2"/></w:pPr><w:r><w:t xml:space="preserve">3.2. Máy chụp cắt lớp vi tính (CT Scanner) 128 lát cắt</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Số lát cắt tối thiểu: 128 lát cắt/vòng quay</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Thời gian quay 360°: <= 0.35 giây</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Công suất bóng: >= 7.5 MHU</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Điện áp bóng: 80, 100, 120, 140 kV</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Dòng bóng: 20 - 500 mA</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Độ phân giải không gian: >= 15 lp/cm ở 0% MTF</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Phần mềm giảm nhiễu bằng AI, tái tạo ảnh lặp</w:t></w:r></w:p>

    <w:p><w:pPr><w:pStyle w:val="Heading2"/></w:pPr><w:r><w:t xml:space="preserve">3.3. Máy X-quang kỹ thuật số (DR)</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Detector: tấm phẳng (Flat Panel Detector), kích thước >= 43x43 cm</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Kích thước điểm ảnh: <= 140 µm</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Dải động: >= 16 bit</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Công suất máy phát: >= 65 kW</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Bóng X-quang: anode quay, tiêu điểm kép 0.6/1.2 mm</w:t></w:r></w:p>

    <w:p><w:pPr><w:pStyle w:val="Heading2"/></w:pPr><w:r><w:t xml:space="preserve">3.4. Máy siêu âm Doppler màu 4D</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Hệ thống siêu âm kỹ thuật số toàn phần</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Cổng kết nối đầu dò: >= 4 cổng kích hoạt đồng thời</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Đầu dò Convex: tần số 1-7 MHz</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Đầu dò Linear: tần số 3-14 MHz</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Đầu dò Tim: tần số 1-5 MHz</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Đầu dò âm đạo: tần số 3-11 MHz</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Chế độ hiển thị: B-mode, M-mode, Color Doppler, Power Doppler, PW/CW Doppler, 3D/4D</w:t></w:r></w:p>

    <w:p><w:pPr><w:pStyle w:val="Heading1"/></w:pPr><w:r><w:t xml:space="preserve">PHẦN 4: YÊU CẦU TÀI CHÍNH</w:t></w:r></w:p>

    <w:p><w:r><w:t xml:space="preserve">4.1. Giá trị dự toán gói thầu: 85.000.000.000 VND (Tám mươi lăm tỷ đồng)</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">4.2. Giá dự thầu không được vượt giá dự toán gói thầu</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">4.3. Đồng tiền dự thầu: Việt Nam Đồng (VND)</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">4.4. Bảo đảm dự thầu: 1.700.000.000 VND (2% giá trị dự toán)</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">4.5. Hình thức bảo đảm dự thầu: Thư bảo lãnh của ngân hàng hoặc đặt cọc</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">4.6. Hiệu lực của bảo đảm dự thầu: >= 150 ngày kể từ ngày mở thầu</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">4.7. Thời hạn thanh toán: 30 ngày sau khi hoàn thành nghiệm thu từng giai đoạn</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">4.8. Tạm ứng: 15% giá trị hợp đồng sau khi ký kết</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">4.9. Bảo hành: tối thiểu 24 tháng đối với toàn bộ thiết bị</w:t></w:r></w:p>

    <w:p><w:pPr><w:pStyle w:val="Heading1"/></w:pPr><w:r><w:t xml:space="preserve">PHẦN 5: YÊU CẦU NĂNG LỰC NHÀ THẦU</w:t></w:r></w:p>

    <w:p><w:pPr><w:pStyle w:val="Heading2"/></w:pPr><w:r><w:t xml:space="preserve">5.1. Yêu cầu chung</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Có đăng ký kinh doanh hợp lệ, mã ngành nghề phù hợp</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Không trong danh sách nhà thầu bị cấm tham gia đấu thầu</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Có chứng chỉ ISO 13485:2016 về hệ thống quản lý chất lượng trang thiết bị y tế</w:t></w:r></w:p>

    <w:p><w:pPr><w:pStyle w:val="Heading2"/></w:pPr><w:r><w:t xml:space="preserve">5.2. Kinh nghiệm nhà thầu</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Đã thực hiện >= 3 hợp đồng cung cấp thiết bị y tế trong 5 năm gần đây</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Tổng giá trị các hợp đồng >= 50 tỷ VND</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Có văn bản xác nhận hoàn thành hợp đồng từ chủ đầu tư</w:t></w:r></w:p>

    <w:p><w:pPr><w:pStyle w:val="Heading2"/></w:pPr><w:r><w:t xml:space="preserve">5.3. Năng lực kỹ thuật</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Có đội ngũ kỹ sư được nhà sản xuất ủy quyền và đào tạo chứng chỉ</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Có kho phụ tùng, linh kiện thay thế tại Việt Nam</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Cam kết thời gian phản hồi dịch vụ <= 24 giờ</w:t></w:r></w:p>

    <w:p><w:pPr><w:pStyle w:val="Heading1"/></w:pPr><w:r><w:t xml:space="preserve">PHẦN 6: QUY TRÌNH ĐẤU THẦU</w:t></w:r></w:p>

    <w:p><w:r><w:t xml:space="preserve">6.1. Phát hành HSMT: từ 08:00 ngày 01/06/2026 đến 17:00 ngày 15/06/2026</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">6.2. Địa điểm phát hành: Phòng Kế hoạch Tổng hợp, Bệnh viện Đa khoa Trung ương</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">6.3. Hạn cuối nộp hồ sơ dự thầu: 14:00 ngày 15/07/2026</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">6.4. Mở hồ sơ đề xuất kỹ thuật: 14:30 ngày 15/07/2026</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">6.5. Địa điểm mở thầu: Hội trường tầng 3, Bệnh viện Đa khoa Trung ương</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">6.6. Hình thức nộp hồ sơ: Trực tiếp hoặc qua đường bưu điện</w:t></w:r></w:p>

    <w:p><w:pPr><w:pStyle w:val="Heading1"/></w:pPr><w:r><w:t xml:space="preserve">PHẦN 7: TIÊU CHÍ ĐÁNH GIÁ</w:t></w:r></w:p>

    <w:p><w:r><w:t xml:space="preserve">7.1. Đánh giá hồ sơ đề xuất kỹ thuật (tối đa 100 điểm):</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Đáp ứng thông số kỹ thuật: 40 điểm</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Kinh nghiệm nhà thầu: 15 điểm</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Năng lực kỹ thuật và dịch vụ hậu mãi: 15 điểm</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Tiến độ cung cấp và lắp đặt: 15 điểm</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Uy tín và thương hiệu sản phẩm: 10 điểm</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">- Bảo hành và đào tạo chuyển giao: 5 điểm</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">7.2. Điểm kỹ thuật tối thiểu để vượt qua: >= 70 điểm</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">7.3. Đánh giá hồ sơ đề xuất tài chính: Giá dự thầu thấp nhất sau khi hiệu chỉnh</w:t></w:r></w:p>

    <w:p><w:pPr><w:pStyle w:val="Heading1"/></w:pPr><w:r><w:t xml:space="preserve">PHẦN 8: ĐIỀU KHOẢN CHUNG</w:t></w:r></w:p>

    <w:p><w:r><w:t xml:space="preserve">8.1. Nhà thầu phải chịu mọi chi phí liên quan đến việc chuẩn bị hồ sơ dự thầu</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">8.2. Bên mời thầu có quyền hủy đấu thầu mà không phải bồi thường</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">8.3. Nhà thầu có thể đề nghị làm rõ HSMT bằng văn bản trước ngày mở thầu 10 ngày</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">8.4. Hồ sơ dự thầu nộp muộn sẽ bị loại và trả lại nguyên trạng</w:t></w:r></w:p>
    <w:p><w:r><w:t xml:space="preserve">8.5. Nhà thầu trúng thầu phải cung cấp bảo lãnh thực hiện hợp đồng: 5% giá trị hợp đồng</w:t></w:r></w:p>

    <w:p><w:pPr><w:spacing w:before="600"/></w:pPr><w:r><w:t xml:space="preserve" /></w:r></w:p>
    <w:p>
      <w:pPr><w:jc w:val="right"/></w:pPr>
      <w:r><w:rPr><w:b/></w:rPr><w:t xml:space="preserve">Hà Nội, ngày 01 tháng 06 năm 2026</w:t></w:r>
    </w:p>
    <w:p><w:pPr><w:spacing w:before="200"/></w:pPr><w:r><w:t xml:space="preserve" /></w:r></w:p>
    <w:p>
      <w:pPr><w:jc w:val="right"/></w:pPr>
      <w:r><w:rPr><w:b/></w:rPr><w:t xml:space="preserve">GIÁM ĐỐC BỆNH VIỆN</w:t></w:r>
    </w:p>
    <w:p><w:pPr><w:spacing w:before="400"/></w:pPr><w:r><w:t xml:space="preserve" /></w:r></w:p>
    <w:p>
      <w:pPr><w:jc w:val="right"/></w:pPr>
      <w:r><w:rPr><w:b/></w:rPr><w:t xml:space="preserve">(Đã ký)</w:t></w:r>
    </w:p>
    <w:p><w:pPr><w:spacing w:before="200"/></w:pPr><w:r><w:t xml:space="preserve" /></w:r></w:p>
    <w:p>
      <w:pPr><w:jc w:val="right"/></w:pPr>
      <w:r><w:rPr><w:b/><w:sz w:val="26"/></w:rPr><w:t xml:space="preserve">PGS.TS. Nguyễn Văn An</w:t></w:r>
    </w:p>

    <w:sectPr>
      <w:pgSz w:w="11906" w:h="16838"/>
      <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1800" w:header="720" w:footer="720"/>
    </w:sectPr>
  </w:body>
</w:document>
"@
[System.IO.File]::WriteAllText((Join-Path $docxDir "word\document.xml"), $document, [System.Text.Encoding]::UTF8)

Write-Host "All XML files created. Compressing to DOCX..."

$outputFile = Join-Path $OutputDir "HSMT_ThietBiYTe_GoiTBYT-2026-001.docx"
if (Test-Path $outputFile) { Remove-Item -Force $outputFile }

Add-Type -AssemblyName System.IO.Compression
[System.IO.Compression.ZipFile]::CreateFromDirectory($docxDir, $outputFile)

Remove-Item -Recurse -Force $docxDir

$fileSize = [math]::Round((Get-Item $outputFile).Length / 1024, 2)
Write-Host ""
Write-Host "=== GENERATE SUCCESS ==="
Write-Host "File: $outputFile"
Write-Host "Size: ${fileSize} KB"
