package com.servicehubai.chat.infrastructure;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ChatRetentionJob {

    private final ChatMessageRepository messageRepository;
    private final ChatSessionRepository sessionRepository;

    public ChatRetentionJob(ChatMessageRepository messageRepository, ChatSessionRepository sessionRepository) {
        this.messageRepository = messageRepository;
        this.sessionRepository = sessionRepository;
    }

    @Scheduled(cron = "0 15 2 * * *")
    @Transactional
    public void removeExpiredHistory() {
        Instant cutoff = Instant.now().minus(30, ChronoUnit.DAYS);
        messageRepository.deleteByTimestampBefore(cutoff);
        sessionRepository.deleteByStartedAtBefore(cutoff);
    }
}
