package com.medbid.chatbot.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatbotRequest(
        @NotBlank(message = "Câu hỏi không được để trống")
        String question
) {}
