package com.medbid.ai.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OpenAI-based AI provider using GPT models via REST API.
 * Activated when app.ai.default-provider=openai.
 * Falls back to rule-based extraction when the API key is not configured or the call fails.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.ai.default-provider", havingValue = "openai", matchIfMissing = true)
public class OpenAIProvider implements AIProvider {

    private final String apiKey;
    private final String model;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final boolean apiConfigured;

    public OpenAIProvider(
            @Value("${app.ai.openai.api-key:}") String apiKey,
            @Value("${app.ai.openai.model:gpt-4o}") String model,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
        this.apiConfigured = apiKey != null && !apiKey.isBlank();
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        if (apiConfigured) {
            log.info("OpenAIProvider initialized with model: {}", model);
        } else {
            log.warn("OpenAI API key not configured. Using rule-based fallback parser.");
        }
    }

    @Override
    public ExtractionResult extractRequirements(String text, String context) {
        long startTime = System.currentTimeMillis();

        if (apiConfigured) {
            try {
                ExtractionResult result = callOpenAIExtraction(text, context, startTime);
                log.info("OpenAI extraction complete: {} requirements found, tokens={}, latency={}ms",
                        result.requirements().size(), result.tokensUsed(), result.latencyMs());
                return result;
            } catch (Exception e) {
                log.error("OpenAI extraction failed, falling back to rule-based parser: {}", e.getMessage());
            }
        }

        // Rule-based fallback
        log.info("Using rule-based parser for requirement extraction");
        List<ExtractedRequirement> requirements = ruleBasedExtract(text);
        long latencyMs = System.currentTimeMillis() - startTime;
        return new ExtractionResult(requirements, "rule-based-fallback", 0, latencyMs);
    }

    private ExtractionResult callOpenAIExtraction(String text, String context, long startTime) {
        String prompt = buildExtractionPrompt(text, context);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content",
                        "Bạn là chuyên viên phân tích hồ sơ mời thầu thiết bị y tế. " +
                        "Trích xuất các yêu cầu kỹ thuật, pháp lý và tài chính từ văn bản. " +
                        "Trả về kết quả dưới dạng JSON với các trường: description, type, operator, value, unit, mandatory, priority, confidenceScore."),
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 4096);

        String response = restClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        long latencyMs = System.currentTimeMillis() - startTime;
        return parseOpenAIResponse(response, latencyMs);
    }

    private String buildExtractionPrompt(String text, String context) {
        return String.format("""
                Hãy phân tích văn bản hồ sơ mời thầu sau đây và trích xuất tất cả các yêu cầu.

                Ngữ cảnh: %s

                Văn bản:
                %s

                Với mỗi yêu cầu, xác định:
                - description: mô tả yêu cầu
                - type: TECHNICAL, LEGAL, FINANCIAL, hoặc OTHER
                - operator: >=, <=, =, >, <, hoặc CONTAINS
                - value: giá trị yêu cầu (số hoặc text)
                - unit: đơn vị (nếu có)
                - mandatory: true nếu là yêu cầu bắt buộc
                - priority: 1-5 (1=cao nhất)
                - confidenceScore: 0.0-1.0

                Trả về mảng JSON các đối tượng yêu cầu.
                """, context, text.length() > 16000 ? text.substring(0, 16000) : text);
    }

    private ExtractionResult parseOpenAIResponse(String response, long latencyMs) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(response, new TypeReference<>() {});
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            if (choices == null || choices.isEmpty()) {
                return new ExtractionResult(Collections.emptyList(), model, 0, latencyMs);
            }

            Map<String, Object> choice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            String content = (String) message.get("content");

            Map<String, Object> usage = (Map<String, Object>) responseMap.get("usage");
            int tokensUsed = usage != null ? ((Number) usage.get("total_tokens")).intValue() : 0;

            List<ExtractedRequirement> requirements = parseJsonRequirements(content);
            return new ExtractionResult(requirements, model, tokensUsed, latencyMs);

        } catch (Exception e) {
            log.error("Failed to parse OpenAI response: {}", e.getMessage());
            return new ExtractionResult(Collections.emptyList(), model, 0, latencyMs);
        }
    }

    private List<ExtractedRequirement> parseJsonRequirements(String content) {
        try {
            String json = content;
            if (content.contains("```json")) {
                json = content.substring(content.indexOf("```json") + 7);
                if (json.contains("```")) {
                    json = json.substring(0, json.indexOf("```"));
                }
            } else if (content.contains("```")) {
                json = content.substring(content.indexOf("```") + 3);
                if (json.contains("```")) {
                    json = json.substring(0, json.indexOf("```"));
                }
            }

            List<Map<String, Object>> rawList = objectMapper.readValue(
                    json.trim(), new TypeReference<>() {});
            return rawList.stream()
                    .map(this::mapToRequirement)
                    .toList();
        } catch (JsonProcessingException e) {
            log.warn("Could not parse AI JSON response, returning empty: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private ExtractedRequirement mapToRequirement(Map<String, Object> map) {
        return new ExtractedRequirement(
                getString(map, "description", ""),
                getString(map, "type", "TECHNICAL"),
                getString(map, "operator", "="),
                getString(map, "value", ""),
                getString(map, "unit", ""),
                getBoolean(map, "mandatory", true),
                getInt(map, "priority", 3),
                getDouble(map, "confidenceScore", 0.8)
        );
    }

    // ==================== Rule-Based Fallback Parser ====================

    /**
     * Rule-based extraction using regex patterns for Vietnamese medical tender language.
     * This is the fallback when the AI API is unavailable.
     */
    private List<ExtractedRequirement> ruleBasedExtract(String text) {
        List<ExtractedRequirement> requirements = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return requirements;
        }

        Map<String, PatternConfig> patterns = new LinkedHashMap<>();
        patterns.put("CPU", new PatternConfig(
                Pattern.compile("(?:CPU|Bộ\\s*vi\\s*xử\\s*lý|Processor).*?(?:tối\\s*thiểu|>=|≥|từ)\\s*([A-Za-z0-9\\s\\-]+?)(?:\\s*GHz|\\s*trở\\s*lên|$)",
                        Pattern.CASE_INSENSITIVE),
                "TECHNICAL", ">=", "model", "", 1, true));
        patterns.put("CPU_CORES", new PatternConfig(
                Pattern.compile("(?:CPU|Bộ\\s*vi\\s*xử\\s*lý).*?(?:tối\\s*thiểu|>=|≥|từ)\\s*(\\d+)\\s*(?:nhân|core|lõi)",
                        Pattern.CASE_INSENSITIVE),
                "TECHNICAL", ">=", "cores", "", 1, true));
        patterns.put("RAM", new PatternConfig(
                Pattern.compile("(?:RAM|Bộ\\s*nhớ|Memory).*?(?:tối\\s*thiểu|>=|≥|từ)\\s*(\\d+)\\s*(GB|MB|TB)",
                        Pattern.CASE_INSENSITIVE),
                "TECHNICAL", ">=", "GB", "GB", 1, true));
        patterns.put("STORAGE", new PatternConfig(
                Pattern.compile("(?:Ổ\\s*cứng|SSD|HDD|Storage|Lưu\\s*trữ).*?(?:tối\\s*thiểu|>=|≥|từ)\\s*(\\d+)\\s*(GB|TB|MB)",
                        Pattern.CASE_INSENSITIVE),
                "TECHNICAL", ">=", "GB", "GB", 2, true));
        patterns.put("SCREEN", new PatternConfig(
                Pattern.compile("(?:Màn\\s*hình|Display|Screen|Monitor).*?(?:tối\\s*thiểu|>=|≥|từ)\\s*(\\d+\\.?\\d*)\\s*(?:inch|\")",
                        Pattern.CASE_INSENSITIVE),
                "TECHNICAL", ">=", "inch", "inch", 2, false));
        patterns.put("OS", new PatternConfig(
                Pattern.compile("(?:Hệ\\s*điều\\s*hành|OS|Operating\\s*System).*?(Windows|Linux|macOS|Android|iOS)\\s*(\\d+)?",
                        Pattern.CASE_INSENSITIVE),
                "TECHNICAL", "=", "", "", 2, true));
        patterns.put("WARRANTY", new PatternConfig(
                Pattern.compile("(?:Bảo\\s*hành|Warranty|Bảo\\s*đảm).*?(\\d+)\\s*(?:năm|tháng|year|month)",
                        Pattern.CASE_INSENSITIVE),
                "LEGAL", ">=", "năm", "năm", 2, true));
        patterns.put("CERTIFICATION", new PatternConfig(
                Pattern.compile("(?:Chứng\\s*chỉ|Giấy\\s*phép|Certificate|ISO|FDA|CE|CQ).*?(?:có|phải\\s*có|yêu\\s*cầu|bắt\\s*buộc)",
                        Pattern.CASE_INSENSITIVE),
                "LEGAL", "=", "Có", "", 1, true));
        patterns.put("EXPERIENCE", new PatternConfig(
                Pattern.compile("(?:Kinh\\s*nghiệm|Experience).*?(\\d+)\\s*(?:năm|year)",
                        Pattern.CASE_INSENSITIVE),
                "LEGAL", ">=", "năm", "năm", 3, false));
        patterns.put("PRICE", new PatternConfig(
                Pattern.compile("(?:Giá|Price|Đơn\\s*giá|Giá\\s*trị).*?(?:tối\\s*đa|<=|≤|không\\s*quá|dưới)\\s*(\\d[\\d,\\.]*)\\s*(?:VNĐ|đồng|USD|VND)?",
                        Pattern.CASE_INSENSITIVE),
                "FINANCIAL", "<=", "VNĐ", "VNĐ", 1, false));
        patterns.put("DELIVERY", new PatternConfig(
                Pattern.compile("(?:Giao\\s*hàng|Delivery|Bàn\\s*giao).*?(\\d+)\\s*(?:ngày|tuần|tháng|day|week|month)",
                        Pattern.CASE_INSENSITIVE),
                "LEGAL", "<=", "ngày", "ngày", 2, false));
        patterns.put("GENERIC_MIN", new PatternConfig(
                Pattern.compile("(.*?)(?:tối\\s*thiểu|ít\\s*nhất|>=|≥|từ)\\s*(\\d+\\.?\\d*)\\s*(GB|MB|TB|GHz|MHz|inch|kg|g|mm|cm|m|năm|tháng|ngày)?",
                        Pattern.CASE_INSENSITIVE),
                "TECHNICAL", ">=", "", "", 3, false));

        for (PatternConfig config : patterns.values()) {
            Matcher matcher = config.pattern.matcher(text);

            while (matcher.find()) {
                String value = config.defaultValue;
                String unit = config.defaultUnit;

                if (matcher.groupCount() >= 1 && matcher.group(1) != null) {
                    value = matcher.group(1).trim();
                }
                if (matcher.groupCount() >= 2 && matcher.group(2) != null) {
                    unit = matcher.group(2).trim();
                }

                String rawDescription = matcher.group(0).trim();
                final String description;
                if (rawDescription.length() > 255) {
                    description = rawDescription.substring(0, 252) + "...";
                } else {
                    description = rawDescription;
                }

                boolean duplicate = requirements.stream()
                        .anyMatch(r -> r.description() != null &&
                                similarity(r.description(), description) > 0.8);

                if (!duplicate) {
                    requirements.add(new ExtractedRequirement(
                            description,
                            config.type,
                            config.operator,
                            value,
                            unit,
                            config.mandatory,
                            config.priority,
                            0.6
                    ));
                }
            }
        }

        log.debug("Rule-based extraction found {} requirements from text of length {}", requirements.size(), text.length());
        return requirements;
    }

    private double similarity(String a, String b) {
        if (a == null || b == null) return 0.0;
        if (a.equals(b)) return 1.0;
        String longer = a.length() > b.length() ? a : b;
        String shorter = a.length() > b.length() ? b : a;
        if (longer.length() == 0) return 0.0;
        return (longer.length() - editDistance(longer, shorter)) / (double) longer.length();
    }

    private int editDistance(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else if (j > 0) {
                    int newValue = costs[j - 1];
                    if (a.charAt(i - 1) != b.charAt(j - 1)) {
                        newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                    }
                    costs[j - 1] = lastValue;
                    lastValue = newValue;
                }
            }
            if (i > 0) costs[b.length()] = lastValue;
        }
        return costs[b.length()];
    }

    @Override
    public ComparisonResult compareTechnicalSpecs(String productSpecs, String tenderSpecs) {
        if (apiConfigured) {
            try {
                return callOpenAIComparison(productSpecs, tenderSpecs);
            } catch (Exception e) {
                log.error("OpenAI comparison failed, falling back to rule-based: {}", e.getMessage());
            }
        }
        return ruleBasedComparison(productSpecs, tenderSpecs);
    }

    private ComparisonResult callOpenAIComparison(String productSpecs, String tenderSpecs) {
        String prompt = String.format("""
                So sánh thông số kỹ thuật sản phẩm với yêu cầu hồ sơ mời thầu.

                Thông số sản phẩm:
                %s

                Yêu cầu hồ sơ mời thầu:
                %s

                Trả về JSON với các trường:
                - passed: boolean
                - score: 0.0-1.0
                - missingCriteria: mô tả tiêu chí không đạt
                - recommendation: đề xuất hoặc giải pháp thay thế
                """, productSpecs, tenderSpecs);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        requestBody.put("temperature", 0.2);
        requestBody.put("max_tokens", 2048);

        String response = restClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        try {
            Map<String, Object> responseMap = objectMapper.readValue(response, new TypeReference<>() {});
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            if (choices == null || choices.isEmpty()) {
                return new ComparisonResult(false, 0.0, "No response from AI", "Retry or manual review required");
            }
            Map<String, Object> choice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            String content = (String) message.get("content");
            Map<String, Object> result = objectMapper.readValue(content, new TypeReference<>() {});
            return new ComparisonResult(
                    getBoolean(result, "passed", false),
                    getDouble(result, "score", 0.0),
                    getString(result, "missingCriteria", ""),
                    getString(result, "recommendation", "")
            );
        } catch (Exception e) {
            log.error("Failed to parse comparison response: {}", e.getMessage());
            return new ComparisonResult(false, 0.0, "Parse error", "Manual review required");
        }
    }

    private ComparisonResult ruleBasedComparison(String productSpecs, String tenderSpecs) {
        String productLower = productSpecs.toLowerCase();
        String tenderLower = tenderSpecs.toLowerCase();
        boolean passed = true;
        double score = 0.7;
        StringBuilder missing = new StringBuilder();

        if (tenderLower.contains("iso") && !productLower.contains("iso")) {
            passed = false;
            missing.append("Thiếu chứng chỉ ISO. ");
            score -= 0.2;
        }
        if (tenderLower.contains("ce") && !productLower.contains("ce")) {
            passed = false;
            missing.append("Thiếu chứng chỉ CE. ");
            score -= 0.2;
        }
        if (tenderLower.contains("fda") && !productLower.contains("fda")) {
            passed = false;
            missing.append("Thiếu chứng chỉ FDA. ");
            score -= 0.2;
        }

        score = Math.max(0.0, score);
        return new ComparisonResult(
                passed, score,
                missing.isEmpty() ? null : missing.toString().trim(),
                passed ? "Sản phẩm đáp ứng cơ bản yêu cầu" : "Cần bổ sung chứng chỉ và tài liệu đính kèm"
        );
    }

    @Override
    public ChecklistResult generateChecklist(String tenderRequirements) {
        List<String> items = new ArrayList<>();
        if (tenderRequirements != null && !tenderRequirements.isBlank()) {
            items.add("Kiểm tra hồ sơ pháp lý doanh nghiệp");
            items.add("Kiểm tra chứng chỉ sản phẩm (ISO, FDA, CE, CQ)");
            items.add("Đối chiếu thông số kỹ thuật");
            items.add("Kiểm tra thời hạn bảo hành");
            items.add("Kiểm tra thời gian giao hàng");
            items.add("Xác nhận giá chào thầu phù hợp");
            items.add("Kiểm tra hồ sơ năng lực kinh nghiệm");
        }
        return new ChecklistResult(items, "Checklist tự động tạo từ yêu cầu hồ sơ mời thầu. Cần rà soát bổ sung thủ công.");
    }

    // ==================== Helper Methods ====================

    private String getString(Map<String, Object> map, String key, String defaultValue) {
        Object val = map.get(key);
        return val != null ? val.toString() : defaultValue;
    }

    private boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        Object val = map.get(key);
        if (val instanceof Boolean b) return b;
        if (val instanceof String s) return Boolean.parseBoolean(s);
        return defaultValue;
    }

    private int getInt(Map<String, Object> map, String key, int defaultValue) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.intValue();
        if (val instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    private double getDouble(Map<String, Object> map, String key, double defaultValue) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.doubleValue();
        if (val instanceof String s) {
            try { return Double.parseDouble(s); } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    /**
     * Internal configuration record for rule-based pattern matching.
     */
    private record PatternConfig(
            Pattern pattern,
            String type,
            String operator,
            String defaultValue,
            String defaultUnit,
            int priority,
            boolean mandatory
    ) {}
}
