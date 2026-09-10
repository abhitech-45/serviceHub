package com.servicehubai.audit.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "actor_email", length = 320)
    private String actorEmail;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(nullable = false, length = 30)
    private String outcome;

    @Column(nullable = false)
    private Instant occurredAt;

    protected AuditLogEntity() {
    }

    public AuditLogEntity(String actorEmail, String action, String outcome) {
        this.actorEmail = actorEmail;
        this.action = action;
        this.outcome = outcome;
        this.occurredAt = Instant.now();
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public String getAction() {
        return action;
    }

    public String getOutcome() {
        return outcome;
    }
}
