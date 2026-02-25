package com.exed1ons.aibot.dao.repository;

import com.exed1ons.aibot.dao.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT m FROM ChatMessage m WHERE m.chatId = :chatId ORDER BY m.timestamp DESC")
    List<ChatMessage> findLastMessages(String chatId, Pageable pageable);
}