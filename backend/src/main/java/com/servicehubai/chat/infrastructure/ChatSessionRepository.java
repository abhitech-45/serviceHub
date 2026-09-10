package com.servicehubai.chat.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.servicehubai.chat.domain.ChatSessionEntity;

public interface ChatSessionRepository extends JpaRepository<ChatSessionEntity, UUID> {
    Optional<ChatSessionEntity> findByIdAndUserEmailIgnoreCase(UUID id, String email);
}
