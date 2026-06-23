package com.medbid.ai.controller;

import com.medbid.ai.provider.AIProviderFactory;
import com.medbid.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/ai-config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AIConfigController {

    private final AIProviderFactory aiProviderFactory;

    @Value("${app.ai.default-provider:openai}")
    private String defaultProvider;

    @Value("${app.ai.openai.model:gpt-4o}")
    private String openaiModel;

    @Value("${app.ai.gemini.model:gemini-2.0-flash}")
    private String geminiModel;

    @Value("${app.ai.claude.model:claude-sonnet-4-20250514}")
    private String claudeModel;

    /**
     * Get current AI provider config — shows which provider is active and available.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("currentProvider", defaultProvider);

        // Available providers with status
        List<Map<String, String>> providers = new ArrayList<>();

        Map<String, String> openai = new LinkedHashMap<>();
        openai.put("name", "openai");
        openai.put("model", openaiModel);
        openai.put("status", "ACTIVE");
        openai.put("description", "OpenAI GPT-4o — AI-first extraction with rule-based fallback");
        providers.add(openai);

        Map<String, String> gemini = new LinkedHashMap<>();
        gemini.put("name", "gemini");
        gemini.put("model", geminiModel);
        gemini.put("status", "STUB");
        gemini.put("description", "Google Gemini — pending implementation (stub mode)");
        providers.add(gemini);

        Map<String, String> claude = new LinkedHashMap<>();
        claude.put("name", "claude");
        claude.put("model", claudeModel);
        claude.put("status", "STUB");
        claude.put("description", "Anthropic Claude — pending implementation (stub mode)");
        providers.add(claude);

        config.put("providers", providers);
        config.put("canSwitchWithoutRestart", false);
        config.put("note", "Provider switching requires updating app.ai.default-provider in application.yml and restarting. All providers use the same AIProvider interface — no code changes needed.");

        return ResponseEntity.ok(config);
    }

    /**
     * Test the current AI provider with a sample extraction.
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testProvider(@RequestBody String sampleText) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("provider", defaultProvider);
        try {
            long start = System.currentTimeMillis();
            var extraction = aiProviderFactory.getProvider().extractRequirements(sampleText, "test");
            long latency = System.currentTimeMillis() - start;
            result.put("success", true);
            result.put("extractedCount", extraction.requirements().size());
            result.put("model", extraction.model());
            result.put("latencyMs", latency);
            result.put("tokensUsed", extraction.tokensUsed());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
}
