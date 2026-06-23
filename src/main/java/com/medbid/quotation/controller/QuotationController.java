package com.medbid.quotation.controller;

import com.medbid.quotation.dto.QuotationCreateRequest;
import com.medbid.quotation.dto.QuotationDto;
import com.medbid.quotation.service.QuotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quotations")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;

    @GetMapping
    public ResponseEntity<Page<QuotationDto>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(quotationService.findAll(pageable));
    }

    @PostMapping
    public ResponseEntity<QuotationDto> create(@Valid @RequestBody QuotationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quotationService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuotationDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(quotationService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuotationDto> update(@PathVariable UUID id,
                                                @Valid @RequestBody QuotationCreateRequest request) {
        return ResponseEntity.ok(quotationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        quotationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tender/{tenderId}")
    public ResponseEntity<List<QuotationDto>> getByTender(@PathVariable UUID tenderId) {
        return ResponseEntity.ok(quotationService.findByTenderId(tenderId));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<QuotationDto>> getByProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(quotationService.findByProductId(productId));
    }

    @GetMapping("/suggest/{productId}/tender/{tenderId}")
    public ResponseEntity<QuotationService.SuggestedPrice> suggestPrice(
            @PathVariable UUID productId,
            @PathVariable UUID tenderId) {
        return ResponseEntity.ok(quotationService.suggestPrice(productId, tenderId));
    }

    @GetMapping("/chart/{productId}")
    public ResponseEntity<List<QuotationService.PriceChartPoint>> getPriceChart(
            @PathVariable UUID productId) {
        return ResponseEntity.ok(quotationService.getPriceChart(productId));
    }

    @GetMapping("/statistics/{productId}")
    public ResponseEntity<QuotationService.PriceStatistics> getPriceStatistics(
            @PathVariable UUID productId) {
        return ResponseEntity.ok(quotationService.getPriceStatistics(productId));
    }
}
