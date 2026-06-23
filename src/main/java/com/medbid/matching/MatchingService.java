package com.medbid.matching;

import com.medbid.exception.BusinessException;
import com.medbid.exception.ResourceNotFoundException;
import com.medbid.matching.dto.ComplianceDetail;
import com.medbid.matching.dto.GapAnalysisResponse;
import com.medbid.matching.dto.SmartMatchResponse;
import com.medbid.matching.entity.MatchResult;
import com.medbid.product.entity.Product;
import com.medbid.product.entity.ProductDocument;
import com.medbid.product.repository.ProductDocumentRepository;
import com.medbid.product.repository.ProductRepository;
import com.medbid.quotation.service.QuotationService;
import com.medbid.tender.entity.Tender;
import com.medbid.tender.entity.TenderRequirement;
import com.medbid.tender.repository.TenderRepository;
import com.medbid.tender.repository.TenderRequirementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final TenderRepository tenderRepository;
    private final TenderRequirementRepository requirementRepository;
    private final ProductRepository productRepository;
    private final ProductDocumentRepository productDocumentRepository;
    private final MatchResultRepository matchResultRepository;
    private final QuotationService quotationService;

    @Transactional
    public MatchResponse matchProduct(UUID tenderId, UUID productId) {
        Tender tender = getTender(tenderId);
        Product product = getProduct(productId);

        List<TenderRequirement> requirements = requirementRepository
                .findByTenderIdAndDeletedFalse(tenderId);

        if (requirements.isEmpty()) {
            throw new BusinessException("Gói thầu không có yêu cầu kỹ thuật nào để đối chiếu");
        }

        matchResultRepository.deleteByTenderIdAndProductId(tenderId, productId);

        List<MatchDetail> details = new ArrayList<>();
        int passedCount = 0;
        double totalScore = 0.0;

        String productDescription = buildProductDescriptionText(product);

        for (TenderRequirement req : requirements) {
            MatchResult result = evaluateRequirement(req, product, productDescription);
            // Save first to get generated ID
            result = matchResultRepository.save(result);

            boolean passed = Boolean.TRUE.equals(result.getPassed());
            double score = result.getScore() != null ? result.getScore() : 0.0;
            if (passed) {
                passedCount++;
            }

            totalScore += score;

            String status;
            if (passed && score >= 80) {
                status = "PASS";
            } else if (passed) {
                status = "PARTIAL";
            } else {
                status = "FAIL";
            }

            details.add(new MatchDetail(
                    result.getId(),
                    req.getId(),
                    req.getDescription(),
                    req.getType(),
                    req.getOperator(),
                    req.getValue(),
                    req.getUnit(),
                    findActualValue(product, req),
                    status,
                    score,
                    result.getMissingCriteria(),
                    result.getIsManualOverride(),
                    result.getOverrideReason()
            ));
        }

        int total = requirements.size();
        int failed = total - passedCount;
        double overallScore = total > 0 ? totalScore / total : 0.0;

        log.info("Matching completed: tenderId={}, productId={}, passed={}/{}, overallScore={:.2f}",
                tenderId, productId, passedCount, total, overallScore);

        return new MatchResponse(
                tenderId,
                productId,
                product.getName(),
                total,
                passedCount,
                failed,
                Math.round(overallScore * 100.0) / 100.0,
                details
        );
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> findBestMatches(UUID tenderId, int limit) {
        getTender(tenderId);

        List<TenderRequirement> requirements = requirementRepository
                .findByTenderIdAndDeletedFalse(tenderId);

        if (requirements.isEmpty()) {
            throw new BusinessException("Gói thầu không có yêu cầu kỹ thuật nào để đối chiếu");
        }

        List<Product> products = productRepository.findByDeletedFalse(
                org.springframework.data.domain.PageRequest.of(0, Math.min(limit * 3, 100)))
                .getContent();

        List<MatchResponse> results = new ArrayList<>();

        for (Product product : products) {
            try {
                String productDescription = buildProductDescriptionText(product);

                int passedCount = 0;
                double totalScore = 0.0;

                for (TenderRequirement req : requirements) {
                    MatchResult result = evaluateRequirement(req, product, productDescription);
                    if (Boolean.TRUE.equals(result.getPassed())) {
                        passedCount++;
                    }
                    totalScore += result.getScore() != null ? result.getScore() : 0.0;
                }

                int total = requirements.size();
                double overallScore = total > 0 ? totalScore / total : 0.0;

                if (passedCount > 0 || overallScore > 30.0) {
                    results.add(new MatchResponse(
                            tenderId,
                            product.getId(),
                            product.getName(),
                            total,
                            passedCount,
                            total - passedCount,
                            Math.round(overallScore * 100.0) / 100.0,
                            null
                    ));
                }
            } catch (Exception e) {
                log.warn("Error matching product {}: {}", product.getId(), e.getMessage());
            }
        }

        results.sort((a, b) -> Double.compare(b.overallScore(), a.overallScore()));

        if (results.size() > limit) {
            results = results.subList(0, limit);
        }

        log.info("Found {} best matches for tenderId={}", results.size(), tenderId);
        return results;
    }

    /**
     * Smart product suggestion with compliance + price scoring.
     */
    @Transactional(readOnly = true)
    public List<SmartMatchResponse> smartSuggest(UUID tenderId, int limit) {
        getTender(tenderId);
        List<TenderRequirement> requirements = requirementRepository.findByTenderIdAndDeletedFalse(tenderId);
        if (requirements.isEmpty()) throw new BusinessException("Gói thầu không có yêu cầu kỹ thuật nào");

        List<Product> products = productRepository.findByDeletedFalse(
                org.springframework.data.domain.PageRequest.of(0, Math.min(limit * 3, 100))).getContent();

        List<SmartMatchResponse> results = new ArrayList<>();
        for (Product product : products) {
            try {
                String productDescription = buildProductDescriptionText(product);
                int passedCount = 0;
                double totalScore = 0.0;
                List<MatchDetail> details = new ArrayList<>();

                for (TenderRequirement req : requirements) {
                    MatchResult result = evaluateRequirement(req, product, productDescription);
                    boolean passed = Boolean.TRUE.equals(result.getPassed());
                    if (passed) passedCount++;
                    totalScore += result.getScore() != null ? result.getScore() : 0.0;

                    String status = passed ? (result.getScore() >= 80 ? "PASS" : "PARTIAL") : "FAIL";
                    details.add(new MatchDetail(result.getId(), req.getId(), req.getDescription(), req.getType(),
                            req.getOperator(), req.getValue(), req.getUnit(),
                            findActualValue(product, req), status, result.getScore(), result.getMissingCriteria(),
                            false, null));
                }

                // Document compliance check
                var compliance = checkDocumentCompliance(product, requirements);
                double complianceBonus = compliance.isCompliant() ? 10.0 : (compliance.score() * 0.1);

                int total = requirements.size();
                double overallScore = total > 0 ? (totalScore / total) + complianceBonus : 0.0;
                overallScore = Math.min(100, Math.round(overallScore * 100.0) / 100.0);

                // Price suggestion
                QuotationService.SuggestedPrice priceInfo = null;
                try {
                    priceInfo = quotationService.suggestPrice(product.getId(), tenderId);
                } catch (Exception ignored) {}

                // Gap warnings
                List<String> gapWarnings = new ArrayList<>();
                if (!compliance.isCompliant()) {
                    gapWarnings.addAll(compliance.missingDocs().stream()
                            .map(d -> "Thiếu chứng chỉ: " + d).toList());
                }

                SmartMatchResponse resp = SmartMatchResponse.builder()
                        .tenderId(tenderId).productId(product.getId()).productName(product.getName())
                        .productManufacturer(product.getManufacturer()).productCategory(product.getCategory())
                        .totalRequirements(total).passed(passedCount).failed(total - passedCount)
                        .overallScore(overallScore).details(null) // details only on full comparison
                        .hasIso(product.getHasIso()).hasCe(product.getHasCe())
                        .hasFda(product.getHasFda()).hasCoCq(product.getHasCoCq())
                        .missingDocuments(compliance.missingDocs())
                        .expiredDocuments(compliance.expiredDocs())
                        .suggestedPrice(priceInfo != null ? priceInfo.suggestedPrice() : null)
                        .lastWinningPrice(priceInfo != null ? priceInfo.maxPrice() : null)
                        .priceConfidence(priceInfo != null ? priceInfo.confidence() : null)
                        .priceDataPoints(priceInfo != null ? priceInfo.dataPoints() : 0)
                        .gapWarnings(gapWarnings)
                        .build();

                if (passedCount > 0 || overallScore > 25.0) results.add(resp);
            } catch (Exception e) {
                log.warn("Error smart-matching product {}: {}", product.getId(), e.getMessage());
            }
        }

        results.sort((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()));
        if (results.size() > limit) results = results.subList(0, limit);
        log.info("Smart suggest found {} matches for tenderId={}", results.size(), tenderId);
        return results;
    }

    /**
     * Full compliance check: document/certificate availability + expiry.
     */
    @Transactional(readOnly = true)
    public List<ComplianceDetail> checkCompliance(UUID tenderId, UUID productId) {
        Product product = getProduct(productId);
        List<TenderRequirement> requirements = requirementRepository.findByTenderIdAndDeletedFalse(tenderId);
        List<ProductDocument> docs = productDocumentRepository.findByProductIdAndDeletedFalse(productId);

        List<ComplianceDetail> results = new ArrayList<>();
        for (TenderRequirement req : requirements) {
            if (!"CERTIFICATION".equals(req.getType())) continue;
            ComplianceDetail detail = evaluateCertificationRequirement(req, product, docs);
            results.add(detail);
        }
        return results;
    }

    /**
     * Gap analysis: missing criteria + missing documents + recommendations.
     */
    @Transactional(readOnly = true)
    public GapAnalysisResponse analyzeGaps(UUID tenderId, UUID productId) {
        Tender tender = getTender(tenderId);
        Product product = getProduct(productId);
        List<TenderRequirement> requirements = requirementRepository.findByTenderIdAndDeletedFalse(tenderId);
        List<ProductDocument> docs = productDocumentRepository.findByProductIdAndDeletedFalse(productId);
        String productDescription = buildProductDescriptionText(product);

        List<GapAnalysisResponse.GapItem> missingCriteria = new ArrayList<>();
        List<GapAnalysisResponse.GapItem> missingDocuments = new ArrayList<>();
        List<GapAnalysisResponse.GapItem> expiredCertificates = new ArrayList<>();
        List<String> recommendedActions = new ArrayList<>();
        int passed = 0, failed = 0;
        double totalScore = 0;

        for (TenderRequirement req : requirements) {
            MatchResult result = evaluateRequirement(req, product, productDescription);
            if (Boolean.TRUE.equals(result.getPassed())) { passed++; }
            else { failed++; }
            totalScore += result.getScore() != null ? result.getScore() : 0.0;

            if (!Boolean.TRUE.equals(result.getPassed())) {
                missingCriteria.add(GapAnalysisResponse.GapItem.builder()
                        .category(req.getType()).description(req.getDescription())
                        .severity(Boolean.TRUE.equals(req.getMandatory()) ? "CRITICAL" : "WARNING")
                        .currentStatus("INSUFFICIENT")
                        .recommendation(result.getMissingCriteria()).build());
            }
        }

        // Check certification requirements
        for (TenderRequirement req : requirements) {
            if (!"CERTIFICATION".equals(req.getType())) continue;
            String reqLower = req.getDescription().toLowerCase();
            Map<String, String> certMap = Map.of(
                    "iso", "ISO_13485", "ce", "CE", "fda", "FDA", "co/cq", "CO", "co", "CO", "cq", "CQ"
            );
            for (var entry : certMap.entrySet()) {
                if (reqLower.contains(entry.getKey())) {
                    boolean hasDoc = docs.stream().anyMatch(d -> entry.getValue().equals(d.getDocumentType()));
                    if (!hasDoc) {
                        missingDocuments.add(GapAnalysisResponse.GapItem.builder()
                                .category("DOCUMENT").description("Thiếu chứng chỉ " + entry.getValue()
                                        + " cho sản phẩm " + product.getName())
                                .severity(Boolean.TRUE.equals(req.getMandatory()) ? "CRITICAL" : "WARNING")
                                .currentStatus("MISSING")
                                .recommendation("Tải lên chứng chỉ " + entry.getValue() + " cho sản phẩm").build());
                    }
                }
            }
        }

        // Check for expired documents
        LocalDate today = LocalDate.now();
        for (ProductDocument doc : docs) {
            if (doc.getExpiryDate() != null && doc.getExpiryDate().isBefore(today)) {
                expiredCertificates.add(GapAnalysisResponse.GapItem.builder()
                        .category("DOCUMENT").description("Chứng chỉ " + doc.getDocumentType()
                                + " - " + doc.getDocumentName() + " đã hết hạn")
                        .severity("CRITICAL").currentStatus("EXPIRED")
                        .recommendation("Gia hạn chứng chỉ " + doc.getDocumentType()).build());
            }
        }

        // Build recommendations
        if (!missingDocuments.isEmpty()) {
            recommendedActions.add("Bổ sung " + missingDocuments.size() + " chứng chỉ còn thiếu cho sản phẩm");
        }
        if (!expiredCertificates.isEmpty()) {
            recommendedActions.add("Gia hạn " + expiredCertificates.size() + " chứng chỉ đã hết hạn");
        }
        if (failed > 0) {
            recommendedActions.add("Xem xét " + failed + " tiêu chí không đạt — có thể cần điều chỉnh cấu hình sản phẩm");
        }
        if (missingDocuments.isEmpty() && expiredCertificates.isEmpty() && failed == 0) {
            recommendedActions.add("Sản phẩm đáp ứng đầy đủ — sẵn sàng nộp hồ sơ dự thầu");
        }

        // Price
        GapAnalysisResponse.PriceSuggestion priceSuggestion = null;
        try {
            QuotationService.SuggestedPrice sp = quotationService.suggestPrice(productId, tenderId);
            if (sp != null) {
                priceSuggestion = GapAnalysisResponse.PriceSuggestion.builder()
                        .suggestedPrice(sp.suggestedPrice()).lastWinningPrice(sp.maxPrice())
                        .averagePrice(sp.averagePrice()).minPrice(sp.minPrice()).maxPrice(sp.maxPrice())
                        .confidence(sp.confidence()).dataPoints(sp.dataPoints())
                        .currency("VND").build();
            }
        } catch (Exception ignored) {}

        int total = requirements.size();
        return GapAnalysisResponse.builder()
                .tenderId(tenderId).productId(productId).productName(product.getName())
                .tenderName(tender.getName()).totalRequirements(total)
                .passedRequirements(passed).failedRequirements(failed)
                .overallScore(total > 0 ? Math.round(totalScore / total * 100.0) / 100.0 : 0)
                .missingCriteria(missingCriteria).missingDocuments(missingDocuments)
                .expiredCertificates(expiredCertificates).recommendedActions(recommendedActions)
                .priceSuggestion(priceSuggestion).build();
    }

    /**
     * Manual override a match result.
     */
    @Transactional
    public MatchResult overrideResult(UUID matchResultId, boolean passed, String reason, UUID userId) {
        MatchResult result = matchResultRepository.findById(matchResultId)
                .orElseThrow(() -> new ResourceNotFoundException("MatchResult", "id", matchResultId));
        result.setPassed(passed);
        result.setScore(passed ? 100.0 : 0.0);
        result.setIsManualOverride(true);
        result.setOverrideReason(reason);
        result.setOverrideBy(userId);
        result.setUpdatedAt(java.time.LocalDateTime.now());
        MatchResult saved = matchResultRepository.save(result);
        log.info("Match result {} overridden to passed={} by user {}", matchResultId, passed, userId);
        return saved;
    }

    // --- Document Compliance Helpers ---

    private record ComplianceResult(boolean isCompliant, double score, List<String> missingDocs, List<String> expiredDocs) {}

    private ComplianceResult checkDocumentCompliance(Product product, List<TenderRequirement> requirements) {
        List<ProductDocument> docs = productDocumentRepository.findByProductIdAndDeletedFalse(product.getId());
        List<String> missingDocs = new ArrayList<>();
        List<String> expiredDocs = new ArrayList<>();
        int totalChecks = 0, passedChecks = 0;
        LocalDate today = LocalDate.now();

        for (TenderRequirement req : requirements) {
            if (!"CERTIFICATION".equals(req.getType())) continue;
            String reqLower = req.getDescription().toLowerCase();
            totalChecks++;

            // ISO check
            if (reqLower.contains("iso")) {
                if (!Boolean.TRUE.equals(product.getHasIso())) missingDocs.add("ISO");
                else passedChecks++;
            }
            // CE check
            if (reqLower.contains("ce")) {
                if (!Boolean.TRUE.equals(product.getHasCe())) missingDocs.add("CE");
                else passedChecks++;
            }
            // FDA check
            if (reqLower.contains("fda")) {
                if (!Boolean.TRUE.equals(product.getHasFda())) missingDocs.add("FDA");
                else passedChecks++;
            }
            // CO/CQ check
            if (reqLower.contains("co") || reqLower.contains("cq")) {
                if (!Boolean.TRUE.equals(product.getHasCoCq())) missingDocs.add("CO/CQ");
                else passedChecks++;
            }
        }

        // Check for expired docs
        for (ProductDocument doc : docs) {
            if (doc.getExpiryDate() != null && doc.getExpiryDate().isBefore(today)) {
                expiredDocs.add(doc.getDocumentType() + " (" + doc.getDocumentName() + ")");
            }
        }

        double score = totalChecks > 0 ? (passedChecks * 100.0 / totalChecks) : 100.0;
        boolean compliant = missingDocs.isEmpty() && expiredDocs.isEmpty();
        return new ComplianceResult(compliant, score, missingDocs, expiredDocs);
    }

    private ComplianceDetail evaluateCertificationRequirement(
            TenderRequirement req, Product product, List<ProductDocument> docs) {
        String reqLower = req.getDescription().toLowerCase();
        List<String> missingDocs = new ArrayList<>();
        List<String> expiredDocs = new ArrayList<>();
        boolean compliant = true;
        LocalDate today = LocalDate.now();

        // Check specific cert requirements
        Map<String, String> certMap = Map.of(
                "iso", "ISO_13485", "ce", "CE", "fda", "FDA", "co/cq", "CO", "co", "CO", "cq", "CQ"
        );

        for (var entry : certMap.entrySet()) {
            if (reqLower.contains(entry.getKey())) {
                boolean hasDoc = docs.stream().anyMatch(d -> entry.getValue().equals(d.getDocumentType()));
                if (!hasDoc) {
                    missingDocs.add(entry.getValue());
                    compliant = false;
                } else {
                    // Check expiry
                    boolean expired = docs.stream()
                            .filter(d -> entry.getValue().equals(d.getDocumentType()))
                            .anyMatch(d -> d.getExpiryDate() != null && d.getExpiryDate().isBefore(today));
                    if (expired) {
                        expiredDocs.add(entry.getValue());
                        compliant = false;
                    }
                }
            }
        }

        return ComplianceDetail.builder()
                .requirementId(req.getId()).requirement(req.getDescription())
                .type(req.getType()).compliant(compliant)
                .status(compliant ? "OK" : (missingDocs.isEmpty() ? "EXPIRED_DOC" : "MISSING_DOC"))
                .missingDocuments(missingDocs).expiredDocuments(expiredDocs)
                .score(compliant ? 100.0 : 0.0)
                .notes(compliant ? "Đầy đủ chứng chỉ" : "Thiếu: " + String.join(", ", missingDocs)).build();
    }

    // --- Existing private methods ---

    private MatchResult evaluateRequirement(TenderRequirement req, Product product, String productDescription) {
        String operator = req.getOperator() != null ? req.getOperator().toLowerCase().trim() : "contains";
        String reqValue = req.getValue() != null ? req.getValue().trim() : "";
        String reqDescription = req.getDescription() != null ? req.getDescription() : "";

        double score = 0.0;
        boolean passed = false;
        String missingCriteria = null;

        try {
            switch (operator) {
                case ">=" -> {
                    var eval = evaluateNumericComparison(product, req, reqValue, ">=");
                    passed = eval.passed();
                    score = eval.score();
                    missingCriteria = eval.missingCriteria();
                }
                case "<=" -> {
                    var eval = evaluateNumericComparison(product, req, reqValue, "<=");
                    passed = eval.passed();
                    score = eval.score();
                    missingCriteria = eval.missingCriteria();
                }
                case ">" -> {
                    var eval = evaluateNumericComparison(product, req, reqValue, ">");
                    passed = eval.passed();
                    score = eval.score();
                    missingCriteria = eval.missingCriteria();
                }
                case "<" -> {
                    var eval = evaluateNumericComparison(product, req, reqValue, "<");
                    passed = eval.passed();
                    score = eval.score();
                    missingCriteria = eval.missingCriteria();
                }
                case "=", "==" -> {
                    var eval = evaluateEquality(product, req, reqValue);
                    passed = eval.passed();
                    score = eval.score();
                    missingCriteria = eval.missingCriteria();
                }
                case "contains" -> {
                    var eval = evaluateContains(productDescription, reqDescription, reqValue);
                    passed = eval.passed();
                    score = eval.score();
                    missingCriteria = eval.missingCriteria();
                }
                default -> {
                    var eval = evaluateContains(productDescription, reqDescription, reqValue);
                    passed = eval.passed();
                    score = eval.score();
                    missingCriteria = eval.missingCriteria();
                }
            }
        } catch (Exception e) {
            log.warn("Error evaluating requirement {}: {}", req.getId(), e.getMessage());
            passed = false;
            score = 0.0;
            missingCriteria = "Lỗi khi đánh giá: " + e.getMessage();
        }

        MatchResult result = new MatchResult();
        result.setTenderId(req.getTenderId());
        result.setTenderRequirementId(req.getId());
        result.setProductId(product.getId());
        result.setPassed(passed);
        result.setScore(score);
        result.setMissingCriteria(missingCriteria);
        result.setCreatedAt(java.time.LocalDateTime.now());

        return result;
    }

    private record EvalResult(boolean passed, double score, String missingCriteria) {}

    private EvalResult evaluateNumericComparison(Product product, TenderRequirement req,
                                                  String reqValue, String operator) {
        try {
            double requiredValue = Double.parseDouble(reqValue);
            String fieldName = extractFieldName(req.getDescription());

            Double productValue = findNumericValue(product, fieldName, req.getDescription());
            if (productValue == null) {
                return new EvalResult(false, 0.0,
                        "Thiếu thông số: " + fieldName + " (yêu cầu " + operator + " " + reqValue + ")");
            }

            boolean passed = switch (operator) {
                case ">=" -> productValue >= requiredValue;
                case "<=" -> productValue <= requiredValue;
                case ">" -> productValue > requiredValue;
                case "<" -> productValue < requiredValue;
                default -> false;
            };

            double score;
            if (passed) {
                score = 100.0;
            } else {
                double ratio = requiredValue != 0 ? Math.min(productValue / requiredValue, 1.0) : 0.0;
                score = ratio * 60.0;
            }

            String missing = passed ? null :
                    "Thông số " + fieldName + ": có " + productValue + ", yêu cầu " + operator + " " + reqValue;

            return new EvalResult(passed, score, missing);

        } catch (NumberFormatException e) {
            return new EvalResult(false, 0.0, "Không thể phân tích giá trị số: " + reqValue);
        }
    }

    private EvalResult evaluateEquality(Product product, TenderRequirement req, String reqValue) {
        String fieldName = extractFieldName(req.getDescription());
        String productValue = findStringValue(product, fieldName);

        if (productValue == null) {
            return new EvalResult(false, 0.0, "Thiếu thông tin: " + fieldName);
        }

        boolean passed = productValue.equalsIgnoreCase(reqValue.trim());
        double score = passed ? 100.0 : 0.0;
        String missing = passed ? null :
                fieldName + ": " + productValue + " (yêu cầu: " + reqValue + ")";

        return new EvalResult(passed, score, missing);
    }

    private EvalResult evaluateContains(String productDescription, String reqDescription, String reqValue) {
        String searchText = reqValue != null && !reqValue.isBlank() ? reqValue.trim().toLowerCase() : "";
        String descriptionLower = reqDescription.toLowerCase();

        if (searchText.isEmpty()) {
            Set<String> keywords = extractKeywords(descriptionLower);
            if (keywords.isEmpty()) {
                return new EvalResult(true, 80.0, null);
            }

            int matchCount = 0;
            List<String> missingKeywords = new ArrayList<>();

            for (String keyword : keywords) {
                if (productDescription.contains(keyword)) {
                    matchCount++;
                } else {
                    missingKeywords.add(keyword);
                }
            }

            double score = keywords.isEmpty() ? 80.0 : (matchCount * 100.0 / keywords.size());
            boolean passed = matchCount >= keywords.size() / 2.0;

            String missing = missingKeywords.isEmpty() ? null :
                    "Thiếu tiêu chí: " + String.join(", ", missingKeywords);

            return new EvalResult(passed, Math.round(score * 10.0) / 10.0, missing);
        }

        if (productDescription.contains(searchText)) {
            return new EvalResult(true, 100.0, null);
        }

        Set<String> searchTerms = new HashSet<>(Arrays.asList(searchText.split("[,; ]+")));
        searchTerms.removeIf(String::isBlank);

        if (searchTerms.isEmpty()) {
            return new EvalResult(false, 0.0, "Không tìm thấy: " + searchText);
        }

        int found = 0;
        List<String> missing = new ArrayList<>();
        for (String term : searchTerms) {
            if (productDescription.contains(term.trim().toLowerCase())) {
                found++;
            } else {
                missing.add(term.trim());
            }
        }

        double score = found * 100.0 / searchTerms.size();
        boolean passed = found > 0;

        String missingStr = missing.isEmpty() ? null :
                "Thiếu tiêu chí từ '" + searchText + "': " + String.join(", ", missing);

        return new EvalResult(passed, score, missingStr);
    }

    private String extractFieldName(String description) {
        if (description == null || description.isBlank()) return "";
        Pattern pattern = Pattern.compile("([a-zA-ZÀ-ỹ_]+)\\s*(?:>=|<=|>|<|=)");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            return matcher.group(1).toLowerCase().trim();
        }
        Pattern pattern2 = Pattern.compile("^([a-zA-ZÀ-ỹ_]+)");
        Matcher matcher2 = pattern2.matcher(description.trim());
        if (matcher2.find()) {
            return matcher2.group(1).toLowerCase().trim();
        }
        return description.trim().toLowerCase();
    }

    private Set<String> extractKeywords(String description) {
        Set<String> keywords = new LinkedHashSet<>();
        if (description == null || description.isBlank()) return keywords;

        String cleaned = description.replaceAll("[,;.()\\[\\]{}|/\\\\]+", " ");
        String[] words = cleaned.split("\\s+");

        Set<String> stopWords = Set.of(
                "của", "và", "hoặc", "có", "không", "phải", "được", "với", "cho",
                "tối", "thiểu", "đa", "the", "and", "or", "is", "are", "be", "to",
                "in", "mm", "cm", "m", "kg", "g", "theo", "từ", "đến"
        );

        for (String word : words) {
            word = word.trim().toLowerCase();
            if (word.length() >= 2 && !stopWords.contains(word)) {
                keywords.add(word);
            }
        }

        return keywords;
    }

    private Double findNumericValue(Product product, String fieldName, String description) {
        Map<String, Object> specs = product.getTechnicalSpecs();
        if (specs != null) {
            for (Map.Entry<String, Object> entry : specs.entrySet()) {
                if (entry.getKey().toLowerCase().contains(fieldName)
                        || fieldName.contains(entry.getKey().toLowerCase())) {
                    Object val = entry.getValue();
                    if (val instanceof Number num) {
                        return num.doubleValue();
                    }
                    if (val instanceof String str) {
                        try {
                            return Double.parseDouble(str.replace(",", "."));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        }

        if (fieldName.contains("year") || fieldName.contains("năm")) {
            if (product.getRegistrationIssueDate() != null) {
                return (double) product.getRegistrationIssueDate().getYear();
            }
        }

        String productText = buildProductDescriptionText(product);
        Pattern p = Pattern.compile(
                Pattern.quote(fieldName) + "\\s*(?:>=|<=|>|<|=|:|=)\\s*(\\d+(?:\\.\\d+)?)");
        Matcher m = p.matcher(productText);
        if (m.find()) {
            return Double.parseDouble(m.group(1));
        }

        return null;
    }

    private String findStringValue(Product product, String fieldName) {
        Map<String, Object> specs = product.getTechnicalSpecs();
        if (specs != null) {
            for (Map.Entry<String, Object> entry : specs.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(fieldName)
                        || entry.getKey().toLowerCase().contains(fieldName)) {
                    return String.valueOf(entry.getValue());
                }
            }
        }

        return switch (fieldName.toLowerCase()) {
            case "manufacturer", "hãng" -> product.getManufacturer();
            case "brand", "nhãn" -> product.getBrand();
            case "model" -> product.getModel();
            case "origin", "xuấtxứ", "origin_country" -> product.getOriginCountry();
            case "category", "loại" -> product.getCategory();
            default -> null;
        };
    }

    private String findActualValue(Product product, TenderRequirement req) {
        String fieldName = extractFieldName(req.getDescription());
        if (fieldName.isEmpty()) return null;

        String operator = req.getOperator() != null ? req.getOperator().toLowerCase().trim() : "";
        if (operator.matches("[><=]=?") && !operator.equals("=") && !operator.equals("==")) {
            Double numValue = findNumericValue(product, fieldName, req.getDescription());
            if (numValue != null) {
                return String.valueOf(numValue) + (req.getUnit() != null ? " " + req.getUnit() : "");
            }
            return null;
        }

        String strValue = findStringValue(product, fieldName);
        return strValue;
    }

    private String buildProductDescriptionText(Product product) {
        StringBuilder sb = new StringBuilder();
        if (product.getName() != null) sb.append(product.getName().toLowerCase()).append(" ");
        if (product.getDescription() != null) sb.append(product.getDescription().toLowerCase()).append(" ");
        if (product.getManufacturer() != null) sb.append(product.getManufacturer().toLowerCase()).append(" ");
        if (product.getBrand() != null) sb.append(product.getBrand().toLowerCase()).append(" ");
        if (product.getModel() != null) sb.append(product.getModel().toLowerCase()).append(" ");
        if (product.getCategory() != null) sb.append(product.getCategory().toLowerCase()).append(" ");
        if (product.getOriginCountry() != null) sb.append(product.getOriginCountry().toLowerCase()).append(" ");

        Map<String, Object> specs = product.getTechnicalSpecs();
        if (specs != null) {
            for (Map.Entry<String, Object> entry : specs.entrySet()) {
                sb.append(entry.getKey().toLowerCase()).append(" ");
                if (entry.getValue() != null) {
                    sb.append(String.valueOf(entry.getValue()).toLowerCase()).append(" ");
                }
            }
        }

        return sb.toString();
    }

    private Tender getTender(UUID id) {
        return tenderRepository.findById(id)
                .filter(t -> Boolean.FALSE.equals(t.getDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Tender", "id", id));
    }

    private Product getProduct(UUID id) {
        return productRepository.findById(id)
                .filter(p -> Boolean.FALSE.equals(p.getDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }
}
