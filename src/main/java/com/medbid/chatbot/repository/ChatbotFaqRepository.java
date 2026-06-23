package com.medbid.chatbot.repository;

import com.medbid.chatbot.entity.ChatbotFaq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatbotFaqRepository extends JpaRepository<ChatbotFaq, UUID> {

    List<ChatbotFaq> findByIsActiveTrueOrderBySortOrderAsc();

    List<ChatbotFaq> findByCategoryAndIsActiveTrue(String category);

    @Query(value = """
        SELECT * FROM chatbot_faq
        WHERE is_active = true
        AND (
            LOWER(question) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(keywords) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        ORDER BY sort_order ASC
        LIMIT 10
    """, nativeQuery = true)
    List<ChatbotFaq> searchByKeyword(@Param("keyword") String keyword);
}
