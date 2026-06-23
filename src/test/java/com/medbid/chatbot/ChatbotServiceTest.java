package com.medbid.chatbot;

import com.medbid.chatbot.dto.ChatbotRequest;
import com.medbid.chatbot.dto.ChatbotResponse;
import com.medbid.chatbot.entity.ChatbotFaq;
import com.medbid.chatbot.repository.ChatbotFaqRepository;
import com.medbid.chatbot.service.ChatbotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock private ChatbotFaqRepository faqRepository;

    @InjectMocks private ChatbotService chatbotService;

    @Test
    void shouldReturnAnswerWhenQuestionMatches() {
        ChatbotFaq faq = new ChatbotFaq();
        faq.setQuestion("Làm thế nào để upload hồ sơ mời thầu?");
        faq.setAnswer("Vào menu HSMT → Upload HSMT...");
        faq.setCategory("HSMT");

        when(faqRepository.searchByKeyword("upload hồ sơ")).thenReturn(List.of(faq));

        ChatbotResponse response = chatbotService.ask(new ChatbotRequest("upload hồ sơ"));

        assertNotNull(response);
        assertTrue(response.answer().contains("HSMT"));
        assertTrue(response.confidence() > 0);
    }

    @Test
    void shouldReturnDefaultWhenNoMatch() {
        when(faqRepository.searchByKeyword("xyz unknown")).thenReturn(List.of());

        ChatbotResponse response = chatbotService.ask(new ChatbotRequest("xyz unknown"));

        assertNotNull(response);
        assertTrue(response.answer().contains("Xin lỗi"));
        assertEquals(0.0, response.confidence());
    }
}
