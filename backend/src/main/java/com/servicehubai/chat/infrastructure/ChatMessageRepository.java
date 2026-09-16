package com.servicehubai.chat.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.servicehubai.chat.domain.ChatMessageEntity;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, UUID> {
    List<ChatMessageEntity> findBySessionIdOrderByTimestampAsc(UUID sessionId);
    void deleteByTimestampBefore(java.time.Instant cutoff);
}
