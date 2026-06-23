-- ============================================
-- V5: Seed Test Data for Real-World Testing
-- ============================================

-- Sample Products (Medical Devices)
INSERT INTO products (id, name, manufacturer, brand, model, origin_country, category, description, technical_specs, registration_number, registration_issue_date, registration_expiry_date, has_iso, has_fda, has_ce, has_co_cq, status, deleted) VALUES
(
    'a0000001-0000-0000-0000-000000000001',
    'Máy điện tim 12 kênh',
    'Nihon Kohden',
    'CardioMax',
    'ECG-1250K',
    'Nhật Bản',
    'Thiết bị chẩn đoán hình ảnh',
    'Máy điện tim 12 kênh đồng thời, màn hình cảm ứng 10.4 inch, tự động phân tích và đo đạc, bộ nhớ 500 bệnh nhân',
    '{"kênh": 12, "màn_hình": "10.4 inch LCD", "tần_số_lấy_mẫu": "8000Hz", "bộ_nhớ": "500 bệnh nhân", "kết_nối": "LAN, USB, RS-232C"}',
    'NK-ECG-2025-001',
    '2025-01-15',
    '2028-01-15',
    TRUE, TRUE, TRUE, TRUE,
    'ACTIVE', FALSE
),
(
    'a0000001-0000-0000-0000-000000000002',
    'Máy thở ICU cao cấp',
    'Dräger',
    'Evita',
    'Evita V800',
    'Đức',
    'Thiết bị y tế',
    'Máy thở chăm sóc tích cực với đa dạng chế độ thở: PCV, VCV, PSV, SIMV, CPAP, APRV. Tích hợp theo dõi CO2 cuối kỳ thở ra.',
    '{"chế_độ_thở": "PCV, VCV, PSV, SIMV, CPAP, APRV, PRVC", "thể_tích_khí_lưu_thông": "2-2000 ml", "FiO2": "21-100%", "PEEP": "0-35 cmH2O", "màn_hình": "17 inch cảm ứng"}',
    'DG-V800-2024-089',
    '2024-06-20',
    '2027-06-20',
    TRUE, TRUE, TRUE, TRUE,
    'ACTIVE', FALSE
),
(
    'a0000001-0000-0000-0000-000000000003',
    'Bơm tiêm điện 2 kênh',
    'Terumo',
    'Terufusion',
    'TE-332S',
    'Nhật Bản',
    'Vật tư tiêu hao',
    'Bơm tiêm điện 2 kênh độc lập, hỗ trợ bơm tiêm 5ml-50ml, có chế độ TCI, tích hợp thư viện thuốc',
    '{"số_kênh": 2, "tốc_độ_bơm": "0.1-1200 ml/h", "dung_tích_bơm_tiêm": "5-50 ml", "chế_độ": "TCI, liên tục, bolus", "độ_chính_xác": "±2%"}',
    'TM-TE332-2024-045',
    '2024-03-10',
    '2027-03-10',
    TRUE, TRUE, TRUE, FALSE,
    'ACTIVE', FALSE
),
(
    'a0000001-0000-0000-0000-000000000004',
    'Monitor theo dõi bệnh nhân đa thông số',
    'Philips',
    'IntelliVue',
    'MX750',
    'Hà Lan',
    'Thiết bị chẩn đoán hình ảnh',
    'Monitor theo dõi bệnh nhân với ECG 5 lead, SpO2, NIBP, IBP, Nhiệt độ, CO2. Màn hình cảm ứng 15 inch.',
    '{"thông_số": "ECG, SpO2, NIBP, IBP x3, Temp, CO2, RR", "màn_hình": "15 inch cảm ứng đa điểm", "thời_gian_pin": "4 giờ", "kết_nối": "LAN, WiFi, HL7"}',
    'PH-MX750-2025-123',
    '2025-02-01',
    '2028-02-01',
    TRUE, TRUE, TRUE, TRUE,
    'ACTIVE', FALSE
),
(
    'a0000001-0000-0000-0000-000000000005',
    'Máy X-quang kỹ thuật số DR',
    'Siemens Healthineers',
    'MULTIX',
    'MULTIX Impact',
    'Đức',
    'Thiết bị chẩn đoán hình ảnh',
    'Hệ thống X-quang kỹ thuật số trần gắn detector không dây, công suất 80kW, bàn chụp nâng hạ điện',
    '{"công_suất": "80 kW", "detector": "không dây 43x43 cm", "khoảng_tiêu_cự": "100-180 cm", "bàn_chụp": "nâng hạ điện, chịu tải 285 kg"}',
    'SH-DR-2024-567',
    '2024-09-15',
    '2027-09-15',
    TRUE, TRUE, TRUE, TRUE,
    'ACTIVE', FALSE
);

-- Product Documents (CO/CQ/ISO/CE/FDA/Catalogue with various expiry dates)
INSERT INTO product_documents (id, product_id, document_type, document_name, file_path, file_name, file_size, issue_date, expiry_date, notes, deleted) VALUES
(
    'b0000001-0000-0000-0000-000000000001',
    'a0000001-0000-0000-0000-000000000001',
    'ISO_13485', 'ISO 13485:2016 - Máy điện tim',
    '/uploads/docs/iso13485_ecg.pdf', 'iso13485_ecg.pdf', 204800,
    '2025-01-20', '2026-07-20', 'Chứng nhận ISO 13485 cho dòng máy điện tim', FALSE
),
(
    'b0000001-0000-0000-0000-000000000002',
    'a0000001-0000-0000-0000-000000000001',
    'CE', 'CE Marking - Máy điện tim 12 kênh',
    '/uploads/docs/ce_ecg.pdf', 'ce_ecg.pdf', 153600,
    '2025-01-20', '2026-08-15', 'CE marking cho thị trường châu Âu', FALSE
),
(
    'b0000001-0000-0000-0000-000000000003',
    'a0000001-0000-0000-0000-000000000001',
    'CO', 'Certificate of Origin - Nihon Kohden',
    '/uploads/docs/co_nihon.pdf', 'co_nihon.pdf', 102400,
    '2025-01-10', '2026-06-10', 'Giấy chứng nhận xuất xứ Nhật Bản', FALSE
),
(
    'b0000001-0000-0000-0000-000000000004',
    'a0000001-0000-0000-0000-000000000002',
    'CE', 'CE 0197 - Máy thở Evita V800',
    '/uploads/docs/ce_v800.pdf', 'ce_v800.pdf', 256000,
    '2024-06-25', '2026-05-25', 'CE marking theo MDD 93/42/EEC', FALSE
),
(
    'b0000001-0000-0000-0000-000000000005',
    'a0000001-0000-0000-0000-000000000002',
    'ISO_13485', 'ISO 13485 - Dräger Evita Series',
    '/uploads/docs/iso13485_evita.pdf', 'iso13485_evita.pdf', 189000,
    '2024-07-01', '2026-04-01', 'Chứng nhận ISO 13485 cho dòng Evita', FALSE
),
(
    'b0000001-0000-0000-0000-000000000006',
    'a0000001-0000-0000-0000-000000000002',
    'FDA', 'FDA 510(k) - Evita V800',
    '/uploads/docs/fda_v800.pdf', 'fda_v800.pdf', 312000,
    '2024-08-01', '2026-03-15', 'FDA clearance K241234', FALSE
),
(
    'b0000001-0000-0000-0000-000000000007',
    'a0000001-0000-0000-0000-000000000003',
    'CO', 'CO - Terumo Syringe Pump',
    '/uploads/docs/co_terumo.pdf', 'co_terumo.pdf', 98000,
    '2024-03-15', '2026-02-28', 'Giấy chứng nhận xuất xứ Nhật Bản', FALSE
),
(
    'b0000001-0000-0000-0000-000000000008',
    'a0000001-0000-0000-0000-000000000004',
    'CE', 'CE Marking - IntelliVue MX750',
    '/uploads/docs/ce_mx750.pdf', 'ce_mx750.pdf', 215000,
    '2025-02-05', '2026-07-10', 'CE marking MDR 2017/745', FALSE
),
(
    'b0000001-0000-0000-0000-000000000009',
    'a0000001-0000-0000-0000-000000000004',
    'CO', 'CO - Philips Medical',
    '/uploads/docs/co_philips.pdf', 'co_philips.pdf', 110000,
    '2025-02-05', '2026-06-01', 'Giấy chứng nhận xuất xứ Hà Lan', FALSE
),
(
    'b0000001-0000-0000-0000-000000000010',
    'a0000001-0000-0000-0000-000000000005',
    'CQ', 'CQ - Siemens MULTIX Impact',
    '/uploads/docs/cq_siemens.pdf', 'cq_siemens.pdf', 178000,
    '2024-09-20', '2026-05-20', 'Chứng nhận chất lượng thiết bị X-quang', FALSE
);

-- Enterprise Profile (hồ sơ pháp lý doanh nghiệp mẫu)
INSERT INTO enterprise_profiles (id, company_name, company_name_en, tax_code, address, phone, email, website, legal_representative, legal_rep_position, established_date, business_license_number, business_license_issue_date, business_license_expiry_date, issuing_authority, deleted) VALUES
(
    'c0000001-0000-0000-0000-000000000001',
    'Công ty TNHH Thiết bị Y tế MedTender',
    'MedTender Medical Equipment Co., Ltd.',
    '0312345678',
    'Tầng 12, Tòa nhà Citilight, 45 Võ Thị Sáu, Quận 1, TP. Hồ Chí Minh',
    '028.3838.9999',
    'contact@medtender.vn',
    'https://medtender.vn',
    'Nguyễn Văn An',
    'Giám đốc',
    '2015-03-15',
    '0312345678',
    '2015-03-20',
    '2035-03-20',
    'Sở Kế hoạch và Đầu tư TP. Hồ Chí Minh',
    FALSE
);

-- Legal Documents for Enterprise
INSERT INTO legal_documents (id, enterprise_id, document_type, document_name, file_path, file_name, file_size, issue_date, expiry_date, issuing_authority, status, notes, deleted) VALUES
(
    'd0000001-0000-0000-0000-000000000001',
    'c0000001-0000-0000-0000-000000000001',
    'BUSINESS_LICENSE',
    'Giấy phép đăng ký kinh doanh',
    '/uploads/legal/gpkd_medtender.pdf', 'gpkd_medtender.pdf', 512000,
    '2015-03-20', '2035-03-20',
    'Sở Kế hoạch và Đầu tư TP. Hồ Chí Minh',
    'ACTIVE', 'ĐKKD số 0312345678', FALSE
),
(
    'd0000001-0000-0000-0000-000000000002',
    'c0000001-0000-0000-0000-000000000001',
    'ISO_9001',
    'ISO 9001:2015 - Hệ thống quản lý chất lượng',
    '/uploads/legal/iso9001_medtender.pdf', 'iso9001_medtender.pdf', 345000,
    '2024-01-10', '2026-04-10',
    'TÜV Rheinland Việt Nam',
    'ACTIVE', 'Chứng nhận ISO 9001:2015 cho hoạt động phân phối thiết bị y tế', FALSE
),
(
    'd0000001-0000-0000-0000-000000000003',
    'c0000001-0000-0000-0000-000000000001',
    'GMP_CERT',
    'Giấy chứng nhận GMP - Thực hành tốt phân phối',
    '/uploads/legal/gmp_medtender.pdf', 'gmp_medtender.pdf', 280000,
    '2024-06-15', '2026-02-15',
    'Bộ Y Tế - Cục Quản lý Dược',
    'ACTIVE', 'Chứng nhận GDP cho kho bảo quản thiết bị y tế', FALSE
);

-- Sample Tender
INSERT INTO tenders (id, name, description, bid_package_code, procuring_entity, submission_deadline, opening_date, estimated_value, currency, status, notes, deleted) VALUES
(
    'e0000001-0000-0000-0000-000000000001',
    'Gói thầu cung cấp thiết bị y tế chẩn đoán - Bệnh viện Đa khoa Trung ương',
    'Cung cấp và lắp đặt hệ thống máy điện tim, máy thở ICU, monitor theo dõi bệnh nhân và máy X-quang kỹ thuật số cho Khoa Chẩn đoán hình ảnh và Khoa Hồi sức tích cực.',
    'TB-2026-BVTW-001',
    'Bệnh viện Đa khoa Trung ương',
    '2026-08-15 09:00:00',
    '2026-08-15 10:00:00',
    12500000000.00,
    'VND',
    'DRAFT',
    'Gói thầu thuộc dự án Nâng cấp cơ sở vật chất Bệnh viện Đa khoa Trung ương giai đoạn 2026-2028',
    FALSE
);

-- Tender Items
INSERT INTO tender_items (id, tender_id, item_number, name, description, quantity, unit, estimated_price, deleted) VALUES
(
    'f0000001-0000-0000-0000-000000000001',
    'e0000001-0000-0000-0000-000000000001',
    1, 'Máy điện tim 12 kênh',
    'Máy điện tim 12 kênh đồng thời, có phân tích tự động',
    5.00, 'Cái', 350000000.00, FALSE
),
(
    'f0000001-0000-0000-0000-000000000002',
    'e0000001-0000-0000-0000-000000000001',
    2, 'Máy thở ICU cao cấp',
    'Máy thở chăm sóc tích cực có đa dạng chế độ thở',
    10.00, 'Cái', 850000000.00, FALSE
),
(
    'f0000001-0000-0000-0000-000000000003',
    'e0000001-0000-0000-0000-000000000001',
    3, 'Monitor theo dõi bệnh nhân đa thông số',
    'Monitor theo dõi bệnh nhân với ECG, SpO2, NIBP, IBP, CO2',
    20.00, 'Cái', 180000000.00, FALSE
),
(
    'f0000001-0000-0000-0000-000000000004',
    'e0000001-0000-0000-0000-000000000001',
    4, 'Máy X-quang kỹ thuật số DR',
    'Hệ thống X-quang kỹ thuật số detector không dây, công suất 80kW',
    2.00, 'Hệ thống', 2800000000.00, FALSE
);

-- Tender Requirements (yêu cầu kỹ thuật mẫu - cả bắt buộc và ưu tiên)
INSERT INTO tender_requirements (id, tender_id, description, type, operator, value, unit, mandatory, priority, source, confidence_score, status, deleted) VALUES
(
    '10000001-0000-0000-0000-000000000001',
    'e0000001-0000-0000-0000-000000000001',
    'Máy điện tim có tối thiểu 12 kênh đo đồng thời',
    'TECHNICAL', '>=', '12', 'kênh', TRUE, 1, 'MANUAL', 0.95, 'EXTRACTED', FALSE
),
(
    '10000001-0000-0000-0000-000000000002',
    'e0000001-0000-0000-0000-000000000001',
    'Màn hình màu LCD kích thước tối thiểu 10 inch',
    'TECHNICAL', '>=', '10', 'inch', TRUE, 1, 'MANUAL', 0.92, 'EXTRACTED', FALSE
),
(
    '10000001-0000-0000-0000-000000000003',
    'e0000001-0000-0000-0000-000000000001',
    'Có khả năng lưu trữ dữ liệu tối thiểu 500 bệnh nhân',
    'TECHNICAL', '>=', '500', 'bệnh nhân', TRUE, 1, 'MANUAL', 0.88, 'EXTRACTED', FALSE
),
(
    '10000001-0000-0000-0000-000000000004',
    'e0000001-0000-0000-0000-000000000001',
    'Máy thở hỗ trợ chế độ PCV, VCV, PSV, SIMV',
    'TECHNICAL', 'CONTAINS', 'PCV,VCV,PSV,SIMV', '', TRUE, 1, 'MANUAL', 0.95, 'EXTRACTED', FALSE
),
(
    '10000001-0000-0000-0000-000000000005',
    'e0000001-0000-0000-0000-000000000001',
    'Thể tích khí lưu thông điều chỉnh từ 2ml đến 2000ml',
    'TECHNICAL', 'BETWEEN', '2-2000', 'ml', TRUE, 1, 'MANUAL', 0.90, 'EXTRACTED', FALSE
),
(
    '10000001-0000-0000-0000-000000000006',
    'e0000001-0000-0000-0000-000000000001',
    'Có chứng nhận ISO 13485 còn hiệu lực',
    'CERTIFICATION', '=', 'true', '', TRUE, 1, 'MANUAL', 0.97, 'EXTRACTED', FALSE
),
(
    '10000001-0000-0000-0000-000000000007',
    'e0000001-0000-0000-0000-000000000001',
    'Có chứng nhận CE hoặc FDA còn hiệu lực',
    'CERTIFICATION', '=', 'true', '', TRUE, 1, 'MANUAL', 0.96, 'EXTRACTED', FALSE
),
(
    '10000001-0000-0000-0000-000000000008',
    'e0000001-0000-0000-0000-000000000001',
    'Nhà thầu có ít nhất 3 năm kinh nghiệm cung cấp thiết bị y tế',
    'EXPERIENCE', '>=', '3', 'năm', FALSE, 2, 'MANUAL', 0.85, 'EXTRACTED', FALSE
),
(
    '10000001-0000-0000-0000-000000000009',
    'e0000001-0000-0000-0000-000000000001',
    'Có trung tâm bảo hành hoặc đối tác bảo hành tại Việt Nam',
    'TECHNICAL', '=', 'true', '', FALSE, 2, 'MANUAL', 0.82, 'EXTRACTED', FALSE
),
(
    '10000001-0000-0000-0000-000000000010',
    'e0000001-0000-0000-0000-000000000001',
    'Bảo hành tối thiểu 24 tháng',
    'TECHNICAL', '>=', '24', 'tháng', TRUE, 1, 'MANUAL', 0.93, 'EXTRACTED', FALSE
),
(
    '10000001-0000-0000-0000-000000000011',
    'e0000001-0000-0000-0000-000000000001',
    'Cung cấp catalogue gốc và tài liệu kỹ thuật bằng tiếng Việt hoặc tiếng Anh',
    'TECHNICAL', '=', 'true', '', FALSE, 3, 'MANUAL', 0.91, 'EXTRACTED', FALSE
),
(
    '10000001-0000-0000-0000-000000000012',
    'e0000001-0000-0000-0000-000000000001',
    'Đào tạo chuyển giao công nghệ cho nhân viên y tế tại bệnh viện',
    'TECHNICAL', '=', 'true', '', FALSE, 3, 'MANUAL', 0.87, 'EXTRACTED', FALSE
);
