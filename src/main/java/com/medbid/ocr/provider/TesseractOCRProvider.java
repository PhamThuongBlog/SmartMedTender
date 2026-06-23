package com.medbid.ocr.provider;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Tesseract-based OCR provider using Tess4J.
 * Activated when app.ocr.default-provider=tesseract.
 * Falls back to Apache Tika or plain text reading when Tesseract DLL is unavailable.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.ocr.default-provider", havingValue = "tesseract", matchIfMissing = true)
public class TesseractOCRProvider implements OCRProvider {

    private final String dataPath;
    private final String language;
    private final Tesseract tesseract;
    private final boolean tesseractAvailable;

    public TesseractOCRProvider(
            @Value("${app.ocr.tesseract.data-path:/usr/share/tesseract-ocr/4.00/tessdata}") String dataPath,
            @Value("${app.ocr.tesseract.language:vie+eng}") String language) {
        this.dataPath = dataPath;
        this.language = language;
        this.tesseract = new Tesseract();
        this.tesseractAvailable = initTesseract();
    }

    private boolean initTesseract() {
        try {
            tesseract.setDatapath(dataPath);
            tesseract.setLanguage(language);
            log.info("Tesseract OCR initialized successfully with dataPath={}, language={}", dataPath, language);
            return true;
        } catch (Exception e) {
            log.warn("Tesseract OCR initialization failed: {}. Falling back to Tika/text reader.", e.getMessage());
            return false;
        }
    }

    @Override
    public OCRResult extractText(String filePath) {
        long startTime = System.currentTimeMillis();
        try {
            String text = CompletableFuture
                    .supplyAsync(() -> doExtractText(filePath))
                    .get();
            long processingTimeMs = System.currentTimeMillis() - startTime;
            String detectedLanguage = detectLanguage(text);
            double confidence = tesseractAvailable ? 0.85 : 0.60;

            log.info("OCR extraction complete: file={}, chars={}, time={}ms, lang={}",
                    filePath, text.length(), processingTimeMs, detectedLanguage);

            return new OCRResult(text, confidence, processingTimeMs, detectedLanguage);
        } catch (Exception e) {
            long processingTimeMs = System.currentTimeMillis() - startTime;
            log.error("OCR extraction failed for file: {}", filePath, e);
            return new OCRResult("", 0.0, processingTimeMs, "unknown");
        }
    }

    private String doExtractText(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("File not found: " + filePath);
        }

        if (tesseractAvailable) {
            try {
                return tesseract.doOCR(file);
            } catch (TesseractException e) {
                log.warn("Tesseract OCR failed, falling back to Tika: {}", e.getMessage());
            }
        }

        return fallbackExtract(filePath, file);
    }

    /**
     * Fallback text extraction for when Tesseract OCR is unavailable or fails.
     * Uses Apache POI for DOCX files, plain UTF-8 for other formats.
     */
    private String fallbackExtract(String filePath, File file) {
        String fileName = filePath.toLowerCase();

        // DOCX: extract text from XML inside ZIP using Apache POI
        if (fileName.endsWith(".docx")) {
            try (FileInputStream fis = new FileInputStream(file);
                 XWPFDocument doc = new XWPFDocument(fis);
                 XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                String text = extractor.getText();
                log.info("POI extracted {} chars from DOCX: {}", text.length(), filePath);
                return text;
            } catch (Exception e) {
                log.warn("POI extraction failed for {}: {}. Trying raw fallback...", filePath, e.getMessage());
            }
        }

        // Plain text / other: read as UTF-8 string
        try {
            String text = Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
            log.info("Raw fallback extracted {} chars from {}", text.length(), filePath);
            return text;
        } catch (IOException e) {
            log.error("Fallback extraction failed for: {}", filePath, e);
            return "";
        }
    }

    @Override
    public List<OCRTableResult> extractTable(String filePath) {
        // Tesseract does not natively support table extraction.
        // This method returns an empty list; structured table extraction
        // requires a dedicated provider (e.g., Google Document AI, Azure Form Recognizer).
        log.debug("TesseractOCRProvider does not support table extraction. File: {}", filePath);
        return Collections.emptyList();
    }

    @Override
    public String detectLanguage(String text) {
        if (text == null || text.isBlank()) {
            return "unknown";
        }

        // Simple heuristic: detect Vietnamese by checking for common Vietnamese characters
        // (with diacritics). Fall back to English otherwise.
        String lower = text.toLowerCase();
        boolean hasVietnameseChars = lower.chars().anyMatch(c ->
                c == 'à' || c == 'á' || c == 'ả' || c == 'ã' || c == 'ạ'
                        || c == 'ă' || c == 'ằ' || c == 'ắ' || c == 'ẳ' || c == 'ẵ' || c == 'ặ'
                        || c == 'â' || c == 'ầ' || c == 'ấ' || c == 'ẩ' || c == 'ẫ' || c == 'ậ'
                        || c == 'è' || c == 'é' || c == 'ẻ' || c == 'ẽ' || c == 'ẹ'
                        || c == 'ê' || c == 'ề' || c == 'ế' || c == 'ể' || c == 'ễ' || c == 'ệ'
                        || c == 'ì' || c == 'í' || c == 'ỉ' || c == 'ĩ' || c == 'ị'
                        || c == 'ò' || c == 'ó' || c == 'ỏ' || c == 'õ' || c == 'ọ'
                        || c == 'ô' || c == 'ồ' || c == 'ố' || c == 'ổ' || c == 'ỗ' || c == 'ộ'
                        || c == 'ơ' || c == 'ờ' || c == 'ớ' || c == 'ở' || c == 'ỡ' || c == 'ợ'
                        || c == 'ù' || c == 'ú' || c == 'ủ' || c == 'ũ' || c == 'ụ'
                        || c == 'ư' || c == 'ừ' || c == 'ứ' || c == 'ử' || c == 'ữ' || c == 'ự'
                        || c == 'ỳ' || c == 'ý' || c == 'ỷ' || c == 'ỹ' || c == 'ỵ'
                        || c == 'đ'
        );

        if (hasVietnameseChars) {
            return "vie";
        }
        return "eng";
    }
}
