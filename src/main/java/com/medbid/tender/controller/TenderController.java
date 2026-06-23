package com.medbid.tender.controller;

import com.medbid.tender.dto.*;
import com.medbid.tender.service.TenderItemService;
import com.medbid.tender.service.TenderService;
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
@RequestMapping("/api/tenders")
@RequiredArgsConstructor
public class TenderController {

    private final TenderService tenderService;
    private final TenderItemService tenderItemService;

    @GetMapping
    public ResponseEntity<Page<TenderDto>> getAll(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(tenderService.findByStatus(status, pageable));
        }
        return ResponseEntity.ok(tenderService.findAll(pageable));
    }

    @PostMapping
    public ResponseEntity<TenderDto> create(@Valid @RequestBody TenderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tenderService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenderDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(tenderService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TenderDto> update(@PathVariable UUID id,
                                             @Valid @RequestBody TenderCreateRequest request) {
        return ResponseEntity.ok(tenderService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        tenderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TenderDto> updateStatus(@PathVariable UUID id,
                                                   @Valid @RequestBody TenderStatusUpdateRequest request) {
        return ResponseEntity.ok(tenderService.updateStatus(id, request));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<TenderDto> submit(@PathVariable UUID id) {
        return ResponseEntity.ok(tenderService.submit(id));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<TenderDto> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(tenderService.approve(id));
    }

    @PostMapping("/{id}/won")
    public ResponseEntity<TenderDto> markWon(@PathVariable UUID id) {
        return ResponseEntity.ok(tenderService.markWon(id));
    }

    @PostMapping("/{id}/lost")
    public ResponseEntity<TenderDto> markLost(@PathVariable UUID id) {
        return ResponseEntity.ok(tenderService.markLost(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<TenderDto> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(tenderService.cancel(id));
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity<TenderDto> cloneTender(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tenderService.cloneTender(id));
    }

    /**
     * Record tender outcome with winning price (for reuse/reference).
     * Price is saved to PriceHistory for future suggestions.
     */
    @PostMapping("/{id}/outcome")
    public ResponseEntity<TenderDto> recordOutcome(@PathVariable UUID id,
                                                    @Valid @RequestBody TenderOutcomeRequest request) {
        return ResponseEntity.ok(tenderService.recordOutcome(id, request));
    }

    /**
     * Get won/lost tenders for reuse — shows past outcomes with prices.
     */
    @GetMapping("/history")
    public ResponseEntity<Page<TenderDto>> getHistory(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(defaultValue = "WON,LOST") String statuses) {
        return ResponseEntity.ok(tenderService.findByStatuses(statuses, pageable));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<TenderItemDto>> getItems(@PathVariable UUID id) {
        return ResponseEntity.ok(tenderItemService.findByTenderId(id));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<TenderItemDto> addItem(@PathVariable UUID id,
                                                  @Valid @RequestBody TenderItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tenderItemService.addItem(id, request));
    }

    @PutMapping("/{id}/items/{itemId}")
    public ResponseEntity<TenderItemDto> updateItem(@PathVariable UUID id,
                                                     @PathVariable UUID itemId,
                                                     @Valid @RequestBody TenderItemRequest request) {
        return ResponseEntity.ok(tenderItemService.updateItem(id, itemId, request));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id,
                                            @PathVariable UUID itemId) {
        tenderItemService.deleteItem(id, itemId);
        return ResponseEntity.noContent().build();
    }
}
