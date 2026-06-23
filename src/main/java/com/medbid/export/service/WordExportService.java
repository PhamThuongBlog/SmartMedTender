package com.medbid.export.service;

import com.medbid.tender.entity.Tender;
import com.medbid.tender.entity.TenderItem;
import com.medbid.tender.entity.TenderRequirement;
import com.medbid.tender.repository.TenderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordExportService {

    private final TenderRepository tenderRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_FMT_LONG = DateTimeFormatter.ofPattern("'ngày' dd 'tháng' MM 'năm' yyyy",
            new Locale("vi", "VN"));

    public byte[] export(UUID tenderId) throws Exception {
        Tender tender = getTender(tenderId);
        List<TenderItem> items = tender.getItems().stream()
                .filter(i -> Boolean.FALSE.equals(i.getDeleted()))
                .toList();

        try (XWPFDocument doc = new XWPFDocument()) {
            setNarrowMargins(doc);

            addNationalHeader(doc);
            addBlankLine(doc);
            addTitle(doc, "HỒ SƠ DỰ THẦU", 16);
            addTitle(doc, "(Hồ sơ đề xuất tài chính - kỹ thuật)", 11);
            addBlankLine(doc);
            addBidPackageInfo(doc, tender);
            addBlankLine(doc);

            if (!items.isEmpty()) {
                addTechnicalSpecsTable(doc, items);
                addBlankLine(doc);
                addPriceTable(doc, items);
            }

            addBlankLine(doc);
            addDateAndSignature(doc);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            log.info("Generated Word export for tenderId={}, size={} bytes", tenderId, out.size());
            return out.toByteArray();
        }
    }

    private void setNarrowMargins(XWPFDocument doc) {
        CTSectPr sectPr = doc.getDocument().getBody().addNewSectPr();
        CTPageMar pageMar = sectPr.addNewPgMar();
        pageMar.setLeft(720);
        pageMar.setRight(720);
        pageMar.setTop(720);
        pageMar.setBottom(720);
    }

    private void addNationalHeader(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun run1 = p.createRun();
        run1.setText("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM");
        run1.setBold(true);
        run1.setFontSize(12);
        run1.setFontFamily("Times New Roman");
        run1.addBreak();

        XWPFRun run2 = p.createRun();
        run2.setText("Độc lập - Tự do - Hạnh phúc");
        run2.setBold(true);
        run2.setFontSize(12);
        run2.setFontFamily("Times New Roman");
        run2.addBreak();

        XWPFRun run3 = p.createRun();
        run3.setText("---------------------");
        run3.setFontSize(10);
        run3.setFontFamily("Times New Roman");
    }

    private void addTitle(XWPFDocument doc, String title, int fontSize) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        p.setSpacingAfter(100);

        XWPFRun run = p.createRun();
        run.setText(title);
        run.setBold(true);
        run.setFontSize(fontSize);
        run.setFontFamily("Times New Roman");
    }

    private void addBidPackageInfo(XWPFDocument doc, Tender tender) {
        addInfoRow(doc, "Tên gói thầu", tender.getName());
        if (tender.getBidPackageCode() != null) {
            addInfoRow(doc, "Mã gói thầu", tender.getBidPackageCode());
        }
        if (tender.getProcuringEntity() != null) {
            addInfoRow(doc, "Chủ đầu tư", tender.getProcuringEntity());
        }
        if (tender.getSubmissionDeadline() != null) {
            addInfoRow(doc, "Thời hạn nộp hồ sơ", tender.getSubmissionDeadline().toLocalDate().format(DATE_FMT));
        }
        if (tender.getOpeningDate() != null) {
            addInfoRow(doc, "Ngày mở thầu", tender.getOpeningDate().toLocalDate().format(DATE_FMT));
        }
        if (tender.getEstimatedValue() != null) {
            String value = formatCurrency(tender.getEstimatedValue()) + " " + tender.getCurrency();
            addInfoRow(doc, "Giá trị dự toán", value);
        }
        if (tender.getDescription() != null && !tender.getDescription().isBlank()) {
            addInfoRow(doc, "Mô tả", tender.getDescription());
        }
    }

    private void addInfoRow(XWPFDocument doc, String label, String value) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingAfter(60);

        XWPFRun runLabel = p.createRun();
        runLabel.setText(label + ": ");
        runLabel.setBold(true);
        runLabel.setFontSize(12);
        runLabel.setFontFamily("Times New Roman");

        XWPFRun runValue = p.createRun();
        runValue.setText(value);
        runValue.setFontSize(12);
        runValue.setFontFamily("Times New Roman");
    }

    private void addTechnicalSpecsTable(XWPFDocument doc, List<TenderItem> items) {
        XWPFParagraph header = doc.createParagraph();
        header.setSpacingAfter(100);
        header.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun headerRun = header.createRun();
        headerRun.setText("BẢNG THÔNG SỐ KỸ THUẬT THIẾT BỊ");
        headerRun.setBold(true);
        headerRun.setFontSize(13);
        headerRun.setFontFamily("Times New Roman");

        String[] headers = {"STT", "Tên thiết bị", "Hãng/Model", "Xuất xứ", "Thông số kỹ thuật", "Đơn vị", "Số lượng", "Ghi chú"};
        int[] widths = {500, 2000, 1500, 1200, 2500, 800, 800, 1200};

        XWPFTable table = doc.createTable();
        table.setWidth("100%");

        XWPFTableRow headerRow = table.getRow(0);
        for (int i = 0; i < headers.length; i++) {
            XWPFTableCell cell = i == 0 ? headerRow.getCell(0) : headerRow.addNewTableCell();
            setCellText(cell, headers[i], true, 10);
            setCellWidth(cell, widths[i]);
            setCellBackground(cell, "D9E2F3");
        }

        int idx = 1;
        for (TenderItem item : items) {
            XWPFTableRow row = table.createRow();
            setCellText(row.getCell(0), String.valueOf(idx++), false, 10);
            setCellText(row.getCell(1), item.getName(), false, 10);
            setCellText(row.getCell(2), "", false, 10);
            setCellText(row.getCell(3), "", false, 10);
            setCellText(row.getCell(4), item.getDescription() != null ? item.getDescription() : "", false, 10);
            setCellText(row.getCell(5), item.getUnit() != null ? item.getUnit() : "", false, 10);
            setCellText(row.getCell(6), formatBigDecimal(item.getQuantity()), false, 10);
            setCellText(row.getCell(7), item.getNotes() != null ? item.getNotes() : "", false, 10);

            for (int i = 0; i < headers.length; i++) {
                setCellWidth(row.getCell(i), widths[i]);
            }
        }

        addTableBorders(table);
    }

    private void addPriceTable(XWPFDocument doc, List<TenderItem> items) {
        XWPFParagraph header = doc.createParagraph();
        header.setSpacingBefore(200);
        header.setSpacingAfter(100);
        header.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun headerRun = header.createRun();
        headerRun.setText("BẢNG GIÁ DỰ THẦU");
        headerRun.setBold(true);
        headerRun.setFontSize(13);
        headerRun.setFontFamily("Times New Roman");

        String[] headers = {"STT", "Tên thiết bị", "Đơn giá (VNĐ)", "Số lượng", "Thành tiền (VNĐ)"};
        int[] widths = {600, 3500, 2000, 1200, 2000};

        XWPFTable table = doc.createTable();
        table.setWidth("100%");

        XWPFTableRow headerRow = table.getRow(0);
        for (int i = 0; i < headers.length; i++) {
            XWPFTableCell cell = i == 0 ? headerRow.getCell(0) : headerRow.addNewTableCell();
            setCellText(cell, headers[i], true, 10);
            setCellWidth(cell, widths[i]);
            setCellBackground(cell, "D9E2F3");
        }

        int idx = 1;
        BigDecimal grandTotal = BigDecimal.ZERO;
        for (TenderItem item : items) {
            XWPFTableRow row = table.createRow();
            BigDecimal unitPrice = item.getEstimatedPrice() != null ? item.getEstimatedPrice() : BigDecimal.ZERO;
            BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
            BigDecimal lineTotal = unitPrice.multiply(qty);
            grandTotal = grandTotal.add(lineTotal);

            setCellText(row.getCell(0), String.valueOf(idx++), false, 10);
            setCellText(row.getCell(1), item.getName(), false, 10);
            setCellText(row.getCell(2), formatCurrency(unitPrice), false, 10);
            setCellText(row.getCell(3), formatBigDecimal(qty), false, 10);
            setCellText(row.getCell(4), formatCurrency(lineTotal), false, 10);

            for (int i = 0; i < headers.length; i++) {
                setCellWidth(row.getCell(i), widths[i]);
            }
        }

        XWPFTableRow totalRow = table.createRow();
        setCellText(totalRow.getCell(0), "", true, 10);
        XWPFTableCell mergedCell = totalRow.getCell(0);
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth tcw = mergedCell.getCTTc().addNewTcPr().addNewTcW();
        tcw.setW(java.math.BigInteger.valueOf(widths[0]));

        setCellText(totalRow.getCell(1), "TỔNG CỘNG", true, 11);
        setCellText(totalRow.getCell(2), "", false, 10);
        setCellText(totalRow.getCell(3), "", false, 10);
        setCellText(totalRow.getCell(4), formatCurrency(grandTotal), true, 11);

        for (int i = 0; i < headers.length; i++) {
            setCellWidth(totalRow.getCell(i), widths[i]);
            setCellBackground(totalRow.getCell(i), "E2EFDA");
        }

        addTableBorders(table);
    }

    private void addDateAndSignature(XWPFDocument doc) {
        XWPFParagraph dateP = doc.createParagraph();
        dateP.setAlignment(ParagraphAlignment.RIGHT);
        dateP.setSpacingBefore(200);
        dateP.setIndentationRight(400);

        String dateText = "..., " + LocalDate.now().format(DATE_FMT_LONG);
        XWPFRun dateRun = dateP.createRun();
        dateRun.setText(dateText);
        dateRun.setFontSize(12);
        dateRun.setFontFamily("Times New Roman");
        dateRun.setItalic(true);

        addBlankLine(doc);
        addBlankLine(doc);

        XWPFParagraph sigLine = doc.createParagraph();
        sigLine.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun sigTitle = sigLine.createRun();
        sigTitle.setText("ĐẠI DIỆN NHÀ THẦU");
        sigTitle.setBold(true);
        sigTitle.setFontSize(12);
        sigTitle.setFontFamily("Times New Roman");

        XWPFParagraph sigDetail = doc.createParagraph();
        sigDetail.setAlignment(ParagraphAlignment.CENTER);
        sigDetail.setSpacingBefore(400);
        XWPFRun sigName = sigDetail.createRun();
        sigName.setText("(Ký tên, đóng dấu, ghi rõ họ tên)");
        sigName.setFontSize(11);
        sigName.setFontFamily("Times New Roman");
        sigName.setItalic(true);
    }

    private void addBlankLine(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingAfter(0);
        p.setSpacingBefore(0);
        XWPFRun run = p.createRun();
        run.setText(" ");
        run.setFontSize(6);
    }

    private void setCellText(XWPFTableCell cell, String text, boolean bold, int fontSize) {
        cell.removeParagraph(0);
        XWPFParagraph p = cell.addParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = p.createRun();
        run.setText(text != null ? text : "");
        run.setBold(bold);
        run.setFontSize(fontSize);
        run.setFontFamily("Times New Roman");
    }

    private void setCellWidth(XWPFTableCell cell, int width) {
        CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        CTTblWidth tblWidth = tcPr.isSetTcW() ? tcPr.getTcW() : tcPr.addNewTcW();
        tblWidth.setW(java.math.BigInteger.valueOf(width));
        tblWidth.setType(STTblWidth.DXA);
    }

    private void setCellBackground(XWPFTableCell cell, String hexColor) {
        CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        CTShd shd = tcPr.isSetShd() ? tcPr.getShd() : tcPr.addNewShd();
        shd.setFill(hexColor);
        shd.setVal(STShd.CLEAR);
    }

    private void addTableBorders(XWPFTable table) {
        CTTbl ctTbl = table.getCTTbl();
        CTTblPr tblPr = ctTbl.getTblPr() != null ? ctTbl.getTblPr() : ctTbl.addNewTblPr();
        CTTblBorders borders = tblPr.addNewTblBorders();

        borders.addNewTop().setVal(STBorder.SINGLE);
        borders.getTop().setSz(java.math.BigInteger.valueOf(4));
        borders.addNewBottom().setVal(STBorder.SINGLE);
        borders.getBottom().setSz(java.math.BigInteger.valueOf(4));
        borders.addNewLeft().setVal(STBorder.SINGLE);
        borders.getLeft().setSz(java.math.BigInteger.valueOf(4));
        borders.addNewRight().setVal(STBorder.SINGLE);
        borders.getRight().setSz(java.math.BigInteger.valueOf(4));
        borders.addNewInsideH().setVal(STBorder.SINGLE);
        borders.getInsideH().setSz(java.math.BigInteger.valueOf(4));
        borders.addNewInsideV().setVal(STBorder.SINGLE);
        borders.getInsideV().setSz(java.math.BigInteger.valueOf(4));
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "";
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        nf.setMaximumFractionDigits(0);
        return nf.format(value);
    }

    private String formatBigDecimal(BigDecimal value) {
        if (value == null) return "";
        if (value.stripTrailingZeros().scale() <= 0) {
            return String.valueOf(value.longValue());
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private Tender getTender(UUID id) {
        return tenderRepository.findById(id)
                .orElseThrow(() -> new com.medbid.exception.ResourceNotFoundException("Tender", "id", id));
    }
}
