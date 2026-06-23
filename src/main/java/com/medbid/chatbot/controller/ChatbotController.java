package com.medbid.chatbot.controller;

import com.medbid.chatbot.dto.ChatbotRequest;
import com.medbid.chatbot.dto.ChatbotResponse;
import com.medbid.chatbot.entity.ChatbotFaq;
import com.medbid.chatbot.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/ask")
    public ResponseEntity<ChatbotResponse> ask(@Valid @RequestBody ChatbotRequest request) {
        return ResponseEntity.ok(chatbotService.ask(request));
    }

    @GetMapping("/faqs")
    public ResponseEntity<List<ChatbotFaq>> getAllFaqs(
            @RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(chatbotService.getFaqsByCategory(category));
        }
        return ResponseEntity.ok(chatbotService.getAllFaqs());
    }

    @PostMapping("/faqs")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ChatbotFaq> createFaq(@RequestBody ChatbotFaq faq) {
        return ResponseEntity.ok(chatbotService.createFaq(faq));
    }

    @PutMapping("/faqs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ChatbotFaq> updateFaq(@PathVariable UUID id, @RequestBody ChatbotFaq faq) {
        return ResponseEntity.ok(chatbotService.updateFaq(id, faq));
    }

    @DeleteMapping("/faqs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteFaq(@PathVariable UUID id) {
        chatbotService.deleteFaq(id);
        return ResponseEntity.noContent().build();
    }
}
