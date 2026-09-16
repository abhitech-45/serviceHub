package com.servicehubai.chat.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ai_usage")
public class AiUsageEntity {
    @Id
    @GeneratedValue
    private UUID id;
    private String userEmail;
    private String provider;
    private Instant occurredAt = Instant.now();
    private boolean successful;
    private boolean timedOut;
    private long durationMs;

    protected AiUsageEntity() {
    }

    public AiUsageEntity(String userEmail, String provider, boolean successful, boolean timedOut, long durationMs) {
        this.userEmail = userEmail;
        this.provider = provider;
        this.successful = successful;
        this.timedOut = timedOut;
        this.durationMs = durationMs;
    }

    public String getProvider() { return provider; }
    public Instant getOccurredAt() { return occurredAt; }
    public boolean isSuccessful() { return successful; }
    public boolean isTimedOut() { return timedOut; }
    public long getDurationMs() { return durationMs; }
}
