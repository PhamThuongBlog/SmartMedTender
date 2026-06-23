package com.medbid.export.service;

import com.medbid.exception.BusinessException;
import com.medbid.exception.ResourceNotFoundException;
import com.medbid.export.entity.ExportHistory;
import com.medbid.export.repository.ExportHistoryRepository;
import com.medbid.tender.entity.Tender;
import com.medbid.tender.entity.TenderItem;
import com.medbid.tender.repository.TenderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final TenderRepository tenderRepository;
    private final ExportHistoryRepository exportHistoryRepository;
    private final WordExportService wordExportService;
    private final PdfExportService pdfExportService;
    private final ZipPackageService zipPackageService;

    public byte[] exportWord(UUID tenderId) {
        Tender tender = getTender(tenderId);
        ExportHistory history = createHistory(tenderId, "HSDT", "DOCX");
        try {
            byte[] data = wordExportService.export(tenderId);
            markCompleted(history, (long) data.length);
            log.info("Word export completed: tenderId={}, size={} bytes", tenderId, data.length);
            return data;
        } catch (Exception e) {
            markFailed(history, "Word export failed: " + e.getMessage());
            log.error("Word export failed for tenderId={}", tenderId, e);
            throw new BusinessException("Lỗi khi xuất file Word: " + e.getMessage());
        }
    }

    public byte[] exportPdf(UUID tenderId) {
        Tender tender = getTender(tenderId);
        ExportHistory history = createHistory(tenderId, "HSDT", "PDF");
        try {
            byte[] data = pdfExportService.export(tenderId);
            markCompleted(history, (long) data.length);
            log.info("PDF export completed: tenderId={}, size={} bytes", tenderId, data.length);
            return data;
        } catch (Exception e) {
            markFailed(history, "PDF export failed: " + e.getMessage());
            log.error("PDF export failed for tenderId={}", tenderId, e);
            throw new BusinessException("Lỗi khi xuất file PDF: " + e.getMessage());
        }
    }

    public byte[] exportZip(UUID tenderId) {
        Tender tender = getTender(tenderId);
        ExportHistory history = createHistory(tenderId, "HSDT_PACKAGE", "ZIP");
        try {
            byte[] data = zipPackageService.buildZip(tenderId);
            markCompleted(history, (long) data.length);
            log.info("ZIP export completed: tenderId={}, size={} bytes", tenderId, data.length);
            return data;
        } catch (Exception e) {
            markFailed(history, "ZIP export failed: " + e.getMessage());
            log.error("ZIP export failed for tenderId={}", tenderId, e);
            throw new BusinessException("Lỗi khi xuất file ZIP: " + e.getMessage());
        }
    }

    public byte[] exportExcel(UUID tenderId) {
        Tender tender = getTender(tenderId);
        ExportHistory history = createHistory(tenderId, "HSDT_DATA", "XLSX");

        List<TenderItem> items = tender.getItems().stream()
                .filter(i -> Boolean.FALSE.equals(i.getDeleted()))
                .toList();

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);

            Sheet techSheet = workbook.createSheet("THÔNG SỐ KỸ THUẬT");
            createTechSheet(techSheet, tender, items, headerStyle, dataStyle, titleStyle);

            Sheet priceSheet = workbook.createSheet("BẢNG GIÁ");
            createPriceSheet(priceSheet, tender, items, headerStyle, dataStyle, currencyStyle, titleStyle);

            for (int i = 0; i < 2; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (int col = 0; col < 8; col++) {
                    sheet.autoSizeColumn(col);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            byte[] data = out.toByteArray();
            markCompleted(history, (long) data.length);
            log.info("Excel export completed: tenderId={}, size={} bytes", tenderId, data.length);
            return data;

        } catch (Exception e) {
            markFailed(history, "Excel export failed: " + e.getMessage());
            log.error("Excel export failed for tenderId={}", tenderId, e);
            throw new BusinessException("Lỗi khi xuất file Excel: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<ExportHistory> getHistory(UUID tenderId, Pageable pageable) {
        if (tenderId != null) {
            return exportHistoryRepository.findByTenderId(tenderId, pageable);
        }
        return exportHistoryRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    private void createTechSheet(Sheet sheet, Tender tender, List<TenderItem> items,
                                  CellStyle headerStyle, CellStyle dataStyle, CellStyle titleStyle) {
        int rowIdx = 0;

        Row titleRow = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("THÔNG SỐ KỸ THUẬT THIẾT BỊ - " + tender.getName());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

        sheet.createRow(rowIdx++);

        String[] headers = {"STT", "Tên thiết bị", "Hãng/Model", "Xuất xứ",
                "Thông số kỹ thuật", "Đơn vị", "Số lượng", "Ghi chú"};

        Row headerRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int idx = 1;
        for (TenderItem item : items) {
            Row row = sheet.createRow(rowIdx++);
            setCell(row, 0, String.valueOf(idx++), dataStyle);
            setCell(row, 1, item.getName(), dataStyle);
            setCell(row, 2, "", dataStyle);
            setCell(row, 3, "", dataStyle);
            setCell(row, 4, item.getDescription(), dataStyle);
            setCell(row, 5, item.getUnit(), dataStyle);
            setCell(row, 6, formatNumber(item.getQuantity()), dataStyle);
            setCell(row, 7, item.getNotes(), dataStyle);
        }
    }

    private void createPriceSheet(Sheet sheet, Tender tender, List<TenderItem> items,
                                   CellStyle headerStyle, CellStyle dataStyle,
                                   CellStyle currencyStyle, CellStyle titleStyle) {
        int rowIdx = 0;

        Row titleRow = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BẢNG GIÁ DỰ THẦU - " + tender.getName());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        sheet.createRow(rowIdx++);

        String[] headers = {"STT", "Tên thiết bị", "Đơn giá (VNĐ)", "Số lượng", "Thành tiền (VNĐ)"};
        Row headerRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int idx = 1;
        BigDecimal grandTotal = BigDecimal.ZERO;
        for (TenderItem item : items) {
            Row row = sheet.createRow(rowIdx++);
            BigDecimal price = item.getEstimatedPrice() != null ? item.getEstimatedPrice() : BigDecimal.ZERO;
            BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
            BigDecimal total = price.multiply(qty);
            grandTotal = grandTotal.add(total);

            setCell(row, 0, String.valueOf(idx++), dataStyle);
            setCell(row, 1, item.getName(), dataStyle);
            setCell(row, 2, price.doubleValue(), currencyStyle);
            setCell(row, 3, formatNumber(item.getQuantity()), dataStyle);
            setCell(row, 4, total.doubleValue(), currencyStyle);
        }

        Row totalRow = sheet.createRow(rowIdx);
        Cell totalLabel = totalRow.createCell(0);
        totalLabel.setCellValue("TỔNG CỘNG");
        totalLabel.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 3));

        Cell totalValue = totalRow.createCell(4);
        totalValue.setCellValue(grandTotal.doubleValue());
        totalValue.setCellStyle(currencyStyle);
    }

    private void setCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void setCell(Row row, int col, double value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 13);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private ExportHistory createHistory(UUID tenderId, String exportType, String fileFormat) {
        ExportHistory history = ExportHistory.builder()
                .tenderId(tenderId)
                .exportType(exportType)
                .fileFormat(fileFormat)
                .status("PROCESSING")
                .createdAt(LocalDateTime.now())
                .build();
        return exportHistoryRepository.save(history);
    }

    private void markCompleted(ExportHistory history, Long fileSize) {
        history.setStatus("COMPLETED");
        history.setFileSize(fileSize);
        history.setCompletedAt(LocalDateTime.now());
        exportHistoryRepository.save(history);
    }

    private void markFailed(ExportHistory history, String errorMessage) {
        history.setStatus("FAILED");
        history.setErrorMessage(errorMessage);
        history.setCompletedAt(LocalDateTime.now());
        exportHistoryRepository.save(history);
    }

    private String formatNumber(BigDecimal value) {
        if (value == null) return "";
        if (value.stripTrailingZeros().scale() <= 0) {
            return String.valueOf(value.longValue());
        }
        return value.stripTrailingZeros().toPlainString();
    }

    private Tender getTender(UUID id) {
        return tenderRepository.findById(id)
                .filter(t -> Boolean.FALSE.equals(t.getDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Tender", "id", id));
    }
}
