package com.servicehubai.chat.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_messages")
public class ChatMessageEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSessionEntity session;

    @Column(nullable = false, length = 20)
    private String sender;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    protected ChatMessageEntity() {
    }

    public ChatMessageEntity(ChatSessionEntity session, String sender, String message) {
        this.session = session;
        this.sender = sender;
        this.message = message;
    }

    public UUID getId() {
        return id;
    }

    public ChatSessionEntity getSession() {
        return session;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
