package com.medbid.export.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medbid.tender.entity.Tender;
import com.medbid.tender.entity.TenderItem;
import com.medbid.tender.repository.TenderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZipPackageService {

    private final TenderRepository tenderRepository;
    private final WordExportService wordExportService;
    private final PdfExportService pdfExportService;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public byte[] buildZip(UUID tenderId) throws Exception {
        Tender tender = getTender(tenderId);
        byte[] wordBytes = wordExportService.export(tenderId);
        byte[] pdfBytes = pdfExportService.export(tenderId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            String tenderName = sanitizeFilename(tender.getName());

            ZipEntry wordEntry = new ZipEntry("HSDT_" + tenderName + ".docx");
            zip.putNextEntry(wordEntry);
            zip.write(wordBytes);
            zip.closeEntry();

            ZipEntry pdfEntry = new ZipEntry("HSDT_" + tenderName + ".pdf");
            zip.putNextEntry(pdfEntry);
            zip.write(pdfBytes);
            zip.closeEntry();

            ZipEntry checklistEntry = new ZipEntry("DANH_MUC_KIEM_TRA_" + tenderName + ".txt");
            zip.putNextEntry(checklistEntry);
            byte[] checklistBytes = generateChecklist(tender).getBytes(java.nio.charset.StandardCharsets.UTF_8);
            zip.write(checklistBytes);
            zip.closeEntry();

            ZipEntry metadataEntry = new ZipEntry("metadata.json");
            zip.putNextEntry(metadataEntry);
            byte[] metadataBytes = generateMetadata(tender).getBytes(java.nio.charset.StandardCharsets.UTF_8);
            zip.write(metadataBytes);
            zip.closeEntry();

            zip.finish();
        }

        log.info("Generated ZIP package for tenderId={}, size={} bytes", tenderId, out.size());
        return out.toByteArray();
    }

    private String generateChecklist(Tender tender) {
        StringBuilder sb = new StringBuilder();
        sb.append("DANH MỤC KIỂM TRA HỒ SƠ DỰ THẦU\n");
        sb.append("============================================\n\n");
        sb.append("Gói thầu: ").append(tender.getName()).append("\n");
        if (tender.getBidPackageCode() != null) {
            sb.append("Mã gói thầu: ").append(tender.getBidPackageCode()).append("\n");
        }
        sb.append("Ngày tạo: ").append(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");

        sb.append("I. TÀI LIỆU HÀNH CHÍNH\n");
        sb.append("  [ ] Đơn dự thầu (theo mẫu)\n");
        sb.append("  [ ] Giấy ủy quyền (nếu có)\n");
        sb.append("  [ ] Bảo lãnh dự thầu\n");
        sb.append("  [ ] Giấy chứng nhận đăng ký kinh doanh (bản sao công chứng)\n");
        sb.append("  [ ] Báo cáo tài chính 3 năm gần nhất\n\n");

        sb.append("II. TÀI LIỆU KỸ THUẬT\n");
        sb.append("  [ ] Bảng thông số kỹ thuật thiết bị\n");
        sb.append("  [ ] Catalog sản phẩm\n");
        sb.append("  [ ] Giấy chứng nhận ISO/FDA/CE (nếu có)\n");
        sb.append("  [ ] Giấy chứng nhận xuất xứ (C/O)\n");
        sb.append("  [ ] Giấy chứng nhận chất lượng (C/Q)\n");
        sb.append("  [ ] Tài liệu hướng dẫn sử dụng\n\n");

        sb.append("III. TÀI LIỆU TÀI CHÍNH\n");
        sb.append("  [ ] Bảng giá dự thầu\n");
        sb.append("  [ ] Phân tích chi tiết giá\n\n");

        sb.append("IV. TÀI LIỆU KHÁC\n");
        sb.append("  [ ] Biên bản khảo sát hiện trạng (nếu có)\n");
        sb.append("  [ ] Hợp đồng tương tự đã thực hiện\n");
        sb.append("  [ ] Các tài liệu liên quan khác\n");

        return sb.toString();
    }

    private String generateMetadata(Tender tender) {
        try {
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("type", "HSDT");
            metadata.put("tenderId", tender.getId().toString());
            metadata.put("tenderName", tender.getName());
            metadata.put("bidPackageCode", tender.getBidPackageCode());
            metadata.put("procuringEntity", tender.getProcuringEntity());
            metadata.put("submissionDeadline",
                    tender.getSubmissionDeadline() != null ? tender.getSubmissionDeadline().toString() : null);
            metadata.put("estimatedValue", tender.getEstimatedValue());
            metadata.put("currency", tender.getCurrency());
            metadata.put("generatedAt", LocalDateTime.now().toString());
            metadata.put("generatedBy", "SmartMedTender V2.0");

            List<TenderItem> items = tender.getItems().stream()
                    .filter(i -> Boolean.FALSE.equals(i.getDeleted()))
                    .toList();

            List<Map<String, Object>> itemList = new ArrayList<>();
            for (TenderItem item : items) {
                Map<String, Object> itemMap = new LinkedHashMap<>();
                itemMap.put("itemNumber", item.getItemNumber());
                itemMap.put("name", item.getName());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("unit", item.getUnit());
                itemMap.put("estimatedPrice", item.getEstimatedPrice());
                itemList.add(itemMap);
            }
            metadata.put("items", itemList);
            metadata.put("totalItems", items.size());

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(metadata);
        } catch (Exception e) {
            log.warn("Failed to generate metadata JSON", e);
            return "{\"error\": \"Failed to generate metadata\"}";
        }
    }

    private String sanitizeFilename(String name) {
        if (name == null || name.isBlank()) return "unknown";
        return name.replaceAll("[^a-zA-Z0-9À-ỹ_\\- ]", "_")
                .replaceAll("\\s+", "_")
                .substring(0, Math.min(name.length(), 50));
    }

    private Tender getTender(UUID id) {
        return tenderRepository.findById(id)
                .orElseThrow(() -> new com.medbid.exception.ResourceNotFoundException("Tender", "id", id));
    }
}
