package com.medbid.ocr.service;

import com.medbid.ocr.entity.OcrLog;
import com.medbid.ocr.repository.OcrLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrLogService {

    private final OcrLogRepository ocrLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OcrLog saveSuccess(String provider, String sourceFile, String resultText,
                               double confidence, long processingTimeMs) {
        OcrLog entity = OcrLog.builder()
                .provider(provider)
                .sourceFile(sourceFile)
                .resultText(resultText)
                .confidence(confidence)
                .processingTimeMs(processingTimeMs)
                .success(true)
                .createdAt(LocalDateTime.now())
                .build();
        OcrLog saved = ocrLogRepository.save(entity);
        log.debug("OCR log saved: id={}, provider={}, confidence={}", saved.getId(), provider, confidence);
        return saved;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OcrLog saveFailure(String provider, String sourceFile, String errorMessage,
                               long processingTimeMs) {
        OcrLog entity = OcrLog.builder()
                .provider(provider)
                .sourceFile(sourceFile)
                .confidence(0.0)
                .processingTimeMs(processingTimeMs)
                .success(false)
                .errorMessage(errorMessage)
                .createdAt(LocalDateTime.now())
                .build();
        OcrLog saved = ocrLogRepository.save(entity);
        log.warn("OCR failure log saved: id={}, provider={}, error={}", saved.getId(), provider, errorMessage);
        return saved;
    }
}
