package com.medbid.export.service;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;

public class WatermarkEventHandler implements IEventHandler {

    private final PdfFont font;
    private final String watermarkText;

    public WatermarkEventHandler(PdfFont font, String watermarkText) {
        this.font = font;
        this.watermarkText = watermarkText;
    }

    @Override
    public void handleEvent(Event event) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfPage page = docEvent.getPage();
        Rectangle pageSize = page.getPageSize();

        float pageWidth = pageSize.getWidth();
        float pageHeight = pageSize.getHeight();

        float x = pageWidth / 2;
        float y = pageHeight / 2;

        PdfCanvas pdfCanvas = new PdfCanvas(
                page.newContentStreamBefore(),
                page.getResources(),
                docEvent.getDocument()
        );

        pdfCanvas.saveState();

        PdfExtGState gs = new PdfExtGState();
        gs.setFillOpacity(0.08f);
        pdfCanvas.setExtGState(gs);

        pdfCanvas.setFillColor(new DeviceRgb(128, 128, 128));

        pdfCanvas.beginText();
        pdfCanvas.setFontAndSize(font, Math.max(pageWidth, pageHeight) / 8f);
        pdfCanvas.setTextMatrix(
                (float) Math.cos(Math.toRadians(45)),
                (float) Math.sin(Math.toRadians(45)),
                (float) -Math.sin(Math.toRadians(45)),
                (float) Math.cos(Math.toRadians(45)),
                x - (watermarkText.length() * pageWidth / 80f),
                y
        );
        pdfCanvas.newlineShowText(watermarkText);
        pdfCanvas.endText();

        pdfCanvas.restoreState();
    }
}
