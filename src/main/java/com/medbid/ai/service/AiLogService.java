package com.medbid.ai.service;

import com.medbid.ai.entity.AiLog;
import com.medbid.ai.repository.AiLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiLogService {

    private final AiLogRepository aiLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiLog saveSuccess(String provider, String model, String requestType,
                              String requestPrompt, String responseText,
                              int tokensUsed, long latencyMs, BigDecimal cost) {
        AiLog aiLog = AiLog.builder()
                .provider(provider)
                .model(model)
                .requestType(requestType)
                .requestPrompt(requestPrompt)
                .responseText(responseText)
                .tokensUsed(tokensUsed)
                .latencyMs(latencyMs)
                .cost(cost != null ? cost : BigDecimal.ZERO)
                .success(true)
                .createdAt(LocalDateTime.now())
                .build();
        AiLog saved = aiLogRepository.save(aiLog);
        log.debug("AI log saved: id={}, provider={}, type={}, tokens={}, latency={}ms",
                saved.getId(), provider, requestType, tokensUsed, latencyMs);
        return saved;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiLog saveFailure(String provider, String model, String requestType,
                              String requestPrompt, String errorMessage,
                              int tokensUsed, long latencyMs) {
        AiLog aiLog = AiLog.builder()
                .provider(provider)
                .model(model)
                .requestType(requestType)
                .requestPrompt(requestPrompt)
                .tokensUsed(tokensUsed)
                .latencyMs(latencyMs)
                .cost(BigDecimal.ZERO)
                .success(false)
                .errorMessage(errorMessage)
                .createdAt(LocalDateTime.now())
                .build();
        AiLog saved = aiLogRepository.save(aiLog);
        log.warn("AI failure log saved: id={}, provider={}, error={}", saved.getId(), provider, errorMessage);
        return saved;
    }
}
