package com.exed1ons.aibot.pesistence.repository;

import com.exed1ons.aibot.pesistence.entity.ContextMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<ContextMessage, Long> {
}
