package com.medbid.export.service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;
import com.medbid.tender.entity.Tender;
import com.medbid.tender.entity.TenderItem;
import com.medbid.tender.repository.TenderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfExportService {

    private final TenderRepository tenderRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_FMT_LONG = DateTimeFormatter.ofPattern(
            "'ngày' dd 'tháng' MM 'năm' yyyy", new Locale("vi", "VN"));

    public byte[] export(UUID tenderId) throws Exception {
        Tender tender = getTender(tenderId);
        List<TenderItem> items = tender.getItems().stream()
                .filter(i -> Boolean.FALSE.equals(i.getDeleted()))
                .toList();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        pdf.setDefaultPageSize(PageSize.A4);

        PdfFont fontRegular = PdfFontFactory.createFont(
                "fonts/arial.ttf", PdfEncodings.IDENTITY_H, true);
        PdfFont fontBold = PdfFontFactory.createFont(
                "fonts/arialbd.ttf", PdfEncodings.IDENTITY_H, true);
        PdfFont fontItalic = PdfFontFactory.createFont(
                "fonts/ariali.ttf", PdfEncodings.IDENTITY_H, true);

        pdf.addEventHandler(PdfDocumentEvent.END_PAGE,
                new WatermarkEventHandler(fontRegular, "TÀI LIỆU NỘI BỘ"));

        try (Document doc = new Document(pdf)) {
            doc.setMargins(40, 40, 40, 40);

            addNationalHeader(doc, fontRegular, fontBold);
            addBlankLine(doc, 8);
            addTitle(doc, "HỒ SƠ DỰ THẦU", fontBold, 18);
            addTitle(doc, "(Hồ sơ đề xuất tài chính - kỹ thuật)", fontItalic, 11);
            addBlankLine(doc, 12);
            addBidPackageInfo(doc, tender, fontRegular, fontBold);
            addBlankLine(doc, 12);

            if (!items.isEmpty()) {
                addTechnicalSpecsTable(doc, items, fontRegular, fontBold);
                addBlankLine(doc, 16);
                addPriceTable(doc, items, fontRegular, fontBold);
            }

            addBlankLine(doc, 16);
            addDateAndSignature(doc, fontRegular, fontBold, fontItalic);
        }

        log.info("Generated PDF export for tenderId={}, size={} bytes", tenderId, out.size());
        return out.toByteArray();
    }

    private void addNationalHeader(Document doc, PdfFont font, PdfFont bold) {
        Paragraph p1 = new Paragraph("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM")
                .setFont(bold).setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER);
        doc.add(p1);

        Paragraph p2 = new Paragraph("Độc lập - Tự do - Hạnh phúc")
                .setFont(bold).setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER);
        doc.add(p2);

        Paragraph p3 = new Paragraph("---------------------")
                .setFont(font).setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
        doc.add(p3);
    }

    private void addTitle(Document doc, String text, PdfFont font, int fontSize) {
        Paragraph p = new Paragraph(text)
                .setFont(font).setFontSize(fontSize)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4);
        doc.add(p);
    }

    private void addBidPackageInfo(Document doc, Tender tender, PdfFont font, PdfFont bold) {
        float[] colWidths = {150, 350};
        Table table = new Table(UnitValue.createPointArray(colWidths)).useAllAvailableWidth();

        addInfoRow(table, font, bold, "Tên gói thầu", tender.getName());
        if (tender.getBidPackageCode() != null) {
            addInfoRow(table, font, bold, "Mã gói thầu", tender.getBidPackageCode());
        }
        if (tender.getProcuringEntity() != null) {
            addInfoRow(table, font, bold, "Chủ đầu tư", tender.getProcuringEntity());
        }
        if (tender.getSubmissionDeadline() != null) {
            addInfoRow(table, font, bold, "Thời hạn nộp HS",
                    tender.getSubmissionDeadline().toLocalDate().format(DATE_FMT));
        }
        if (tender.getOpeningDate() != null) {
            addInfoRow(table, font, bold, "Ngày mở thầu",
                    tender.getOpeningDate().toLocalDate().format(DATE_FMT));
        }
        if (tender.getEstimatedValue() != null) {
            addInfoRow(table, font, bold, "Giá trị dự toán",
                    formatCurrency(tender.getEstimatedValue()) + " " + tender.getCurrency());
        }

        doc.add(table);
    }

    private void addInfoRow(Table table, PdfFont font, PdfFont bold, String label, String value) {
        Cell labelCell = new Cell().add(new Paragraph(label).setFont(bold).setFontSize(11));
        labelCell.setBorder(Border.NO_BORDER);
        labelCell.setPaddingBottom(4);

        Cell valueCell = new Cell().add(new Paragraph(value).setFont(font).setFontSize(11));
        valueCell.setBorder(Border.NO_BORDER);
        valueCell.setPaddingBottom(4);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addTechnicalSpecsTable(Document doc, List<TenderItem> items,
                                         PdfFont font, PdfFont bold) {
        Paragraph header = new Paragraph("BẢNG THÔNG SỐ KỸ THUẬT THIẾT BỊ")
                .setFont(bold).setFontSize(13)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(8);
        doc.add(header);

        float[] colWidths = {30, 130, 90, 80, 150, 50, 50, 70};
        Table table = new Table(UnitValue.createPointArray(colWidths)).useAllAvailableWidth();

        String[] headers = {"STT", "Tên thiết bị", "Hãng/Model", "Xuất xứ",
                "Thông số kỹ thuật", "Đơn vị", "Số lượng", "Ghi chú"};

        for (String h : headers) {
            Cell cell = new Cell().add(new Paragraph(h).setFont(bold).setFontSize(8));
            cell.setBackgroundColor(new DeviceRgb(217, 226, 243));
            cell.setTextAlignment(TextAlignment.CENTER);
            table.addHeaderCell(cell);
        }

        int idx = 1;
        for (TenderItem item : items) {
            addDataCell(table, font, String.valueOf(idx++));
            addDataCell(table, font, item.getName());
            addDataCell(table, font, "");
            addDataCell(table, font, "");
            addDataCell(table, font, item.getDescription() != null ? item.getDescription() : "");
            addDataCell(table, font, item.getUnit() != null ? item.getUnit() : "");
            addDataCell(table, font, formatBigDecimal(item.getQuantity()));
            addDataCell(table, font, item.getNotes() != null ? item.getNotes() : "");
        }

        doc.add(table);
    }

    private void addPriceTable(Document doc, List<TenderItem> items,
                                PdfFont font, PdfFont bold) {
        Paragraph header = new Paragraph("BẢNG GIÁ DỰ THẦU")
                .setFont(bold).setFontSize(13)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(8);
        doc.add(header);

        float[] colWidths = {30, 200, 120, 70, 120};
        Table table = new Table(UnitValue.createPointArray(colWidths)).useAllAvailableWidth();

        String[] headers = {"STT", "Tên thiết bị", "Đơn giá (VNĐ)", "Số lượng", "Thành tiền (VNĐ)"};

        for (String h : headers) {
            Cell cell = new Cell().add(new Paragraph(h).setFont(bold).setFontSize(9));
            cell.setBackgroundColor(new DeviceRgb(217, 226, 243));
            cell.setTextAlignment(TextAlignment.CENTER);
            table.addHeaderCell(cell);
        }

        int idx = 1;
        BigDecimal grandTotal = BigDecimal.ZERO;
        for (TenderItem item : items) {
            BigDecimal unitPrice = item.getEstimatedPrice() != null ? item.getEstimatedPrice() : BigDecimal.ZERO;
            BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
            BigDecimal lineTotal = unitPrice.multiply(qty);
            grandTotal = grandTotal.add(lineTotal);

            addDataCell(table, font, String.valueOf(idx++));
            addDataCell(table, font, item.getName());
            addNumberCell(table, font, formatCurrency(unitPrice));
            addNumberCell(table, font, formatBigDecimal(qty));
            addNumberCell(table, font, formatCurrency(lineTotal));
        }

        Cell labelCell = new Cell(1, 4).add(
                new Paragraph("TỔNG CỘNG").setFont(bold).setFontSize(10));
        labelCell.setTextAlignment(TextAlignment.RIGHT);
        labelCell.setBackgroundColor(new DeviceRgb(226, 239, 218));
        table.addCell(labelCell);

        Cell totalCell = new Cell().add(
                new Paragraph(formatCurrency(grandTotal)).setFont(bold).setFontSize(10));
        totalCell.setTextAlignment(TextAlignment.CENTER);
        totalCell.setBackgroundColor(new DeviceRgb(226, 239, 218));
        table.addCell(totalCell);

        doc.add(table);
    }

    private void addDataCell(Table table, PdfFont font, String text) {
        Cell cell = new Cell().add(new Paragraph(text != null ? text : "").setFont(font).setFontSize(8));
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        cell.setBorder(new SolidBorder(1));
        table.addCell(cell);
    }

    private void addNumberCell(Table table, PdfFont font, String text) {
        Cell cell = new Cell().add(new Paragraph(text != null ? text : "").setFont(font).setFontSize(8));
        cell.setTextAlignment(com.itextpdf.layout.property.TextAlignment.RIGHT);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        cell.setBorder(new SolidBorder(1));
        table.addCell(cell);
    }

    private void addDateAndSignature(Document doc, PdfFont font, PdfFont bold, PdfFont italic) {
        String dateText = "..., " + LocalDate.now().format(DATE_FMT_LONG);
        Paragraph dateP = new Paragraph(dateText)
                .setFont(italic).setFontSize(11)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(20);
        doc.add(dateP);

        addBlankLine(doc, 24);
        addBlankLine(doc, 24);

        Paragraph sigTitle = new Paragraph("ĐẠI DIỆN NHÀ THẦU")
                .setFont(bold).setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER);
        doc.add(sigTitle);

        Paragraph sigDetail = new Paragraph("(Ký tên, đóng dấu, ghi rõ họ tên)")
                .setFont(italic).setFontSize(11)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        doc.add(sigDetail);
    }

    private void addBlankLine(Document doc, int points) {
        Paragraph p = new Paragraph(" ").setFontSize(points / 2);
        p.setMarginTop(points / 2f);
        doc.add(p);
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
        return value.stripTrailingZeros().toPlainString();
    }

    private Tender getTender(UUID id) {
        return tenderRepository.findById(id)
                .orElseThrow(() -> new com.medbid.exception.ResourceNotFoundException("Tender", "id", id));
    }
}
