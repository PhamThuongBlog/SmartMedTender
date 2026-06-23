package com.medbid.chatbot.service;

import com.medbid.chatbot.dto.ChatbotRequest;
import com.medbid.chatbot.dto.ChatbotResponse;
import com.medbid.chatbot.entity.ChatbotFaq;
import com.medbid.chatbot.repository.ChatbotFaqRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatbotService {

    private final ChatbotFaqRepository faqRepository;

    public ChatbotResponse ask(ChatbotRequest request) {
        String keyword = request.question().toLowerCase().trim();

        List<ChatbotFaq> matches = faqRepository.searchByKeyword(keyword);

        if (matches.isEmpty()) {
            return new ChatbotResponse(
                    "Xin lỗi, tôi chưa có câu trả lời cho câu hỏi này. Vui lòng liên hệ admin@medtender.vn để được hỗ trợ.",
                    List.of(),
                    0.0
            );
        }

        ChatbotFaq bestMatch = matches.get(0);
        double confidence = matches.size() == 1 ? 0.9 : 0.7;

        List<ChatbotResponse.RelatedQuestion> related = matches.stream()
                .skip(1)
                .limit(3)
                .map(f -> new ChatbotResponse.RelatedQuestion(f.getQuestion(), f.getCategory()))
                .toList();

        return new ChatbotResponse(bestMatch.getAnswer(), related, confidence);
    }

    public List<ChatbotFaq> getAllFaqs() {
        return faqRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    public List<ChatbotFaq> getFaqsByCategory(String category) {
        return faqRepository.findByCategoryAndIsActiveTrue(category);
    }

    @Transactional
    public ChatbotFaq createFaq(ChatbotFaq faq) {
        return faqRepository.save(faq);
    }

    @Transactional
    public ChatbotFaq updateFaq(UUID id, ChatbotFaq updated) {
        ChatbotFaq existing = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found: " + id));
        existing.setQuestion(updated.getQuestion());
        existing.setAnswer(updated.getAnswer());
        existing.setCategory(updated.getCategory());
        existing.setKeywords(updated.getKeywords());
        existing.setSortOrder(updated.getSortOrder());
        existing.setIsActive(updated.getIsActive());
        return faqRepository.save(existing);
    }

    @Transactional
    public void deleteFaq(UUID id) {
        faqRepository.deleteById(id);
    }
}
