package com.medbid.chatbot.dto;

import java.util.List;

public record ChatbotResponse(
        String answer,
        List<RelatedQuestion> relatedQuestions,
        double confidence
) {
    public record RelatedQuestion(String question, String category) {}
}
