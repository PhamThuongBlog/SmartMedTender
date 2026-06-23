package com.medbid.matching;

import com.medbid.matching.dto.ComplianceDetail;
import com.medbid.matching.dto.GapAnalysisResponse;
import com.medbid.matching.dto.OverrideRequest;
import com.medbid.matching.dto.SmartMatchResponse;
import com.medbid.matching.entity.MatchResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;
    private final MatchResultRepository matchResultRepository;

    @PostMapping
    public ResponseEntity<MatchResponse> matchProduct(@Valid @RequestBody MatchRequest request) {
        MatchResponse response = matchingService.matchProduct(
                request.tenderId(),
                request.productId() != null ? request.productId() : request.tenderId()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{tenderId}/product/{productId}")
    public ResponseEntity<MatchResponse> matchSpecificProduct(
            @PathVariable UUID tenderId,
            @PathVariable UUID productId) {
        return ResponseEntity.ok(matchingService.matchProduct(tenderId, productId));
    }

    @GetMapping("/{tenderId}/best")
    public ResponseEntity<List<MatchResponse>> findBestMatches(
            @PathVariable UUID tenderId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(matchingService.findBestMatches(tenderId, Math.min(limit, 50)));
    }

    /**
     * Smart product suggestion with compliance + price scoring.
     */
    @GetMapping("/{tenderId}/smart-suggest")
    public ResponseEntity<List<SmartMatchResponse>> smartSuggest(
            @PathVariable UUID tenderId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(matchingService.smartSuggest(tenderId, Math.min(limit, 50)));
    }

    /**
     * Document/certificate compliance check for a product against tender requirements.
     */
    @GetMapping("/{tenderId}/product/{productId}/compliance")
    public ResponseEntity<List<ComplianceDetail>> checkCompliance(
            @PathVariable UUID tenderId,
            @PathVariable UUID productId) {
        return ResponseEntity.ok(matchingService.checkCompliance(tenderId, productId));
    }

    /**
     * Gap analysis: missing criteria, missing documents, expired certificates, recommendations.
     */
    @GetMapping("/{tenderId}/gap-analysis")
    public ResponseEntity<GapAnalysisResponse> analyzeGaps(
            @PathVariable UUID tenderId,
            @RequestParam UUID productId) {
        return ResponseEntity.ok(matchingService.analyzeGaps(tenderId, productId));
    }

    /**
     * Manual override a match result.
     */
    @PutMapping("/results/override")
    public ResponseEntity<MatchResult> overrideResult(
            @Valid @RequestBody OverrideRequest request,
            @RequestParam(required = false) UUID userId) {
        return ResponseEntity.ok(matchingService.overrideResult(
                request.matchResultId(), request.passed(), request.reason(), userId));
    }

    /**
     * Get saved match results for a tender+product combination.
     */
    @GetMapping("/results")
    public ResponseEntity<List<MatchResult>> getResults(
            @RequestParam UUID tenderId,
            @RequestParam UUID productId) {
        return ResponseEntity.ok(matchResultRepository.findByTenderIdAndProductId(tenderId, productId));
    }

    /**
     * Batch manual override for multiple match results.
     */
    @PutMapping("/results/override/batch")
    public ResponseEntity<Map<String, Object>> batchOverride(
            @RequestBody List<OverrideRequest> overrides,
            @RequestParam(required = false) UUID userId) {
        int count = 0;
        for (OverrideRequest req : overrides) {
            matchingService.overrideResult(req.matchResultId(), req.passed(), req.reason(), userId);
            count++;
        }
        return ResponseEntity.ok(Map.of("status", "OK", "message", "Đã ghi đè " + count + " kết quả", "count", count));
    }
}
