package com.servicehubai.admin.domain;

import java.time.Instant;
import java.util.UUID;

import com.servicehubai.request.domain.RequestPriority;
import com.servicehubai.request.domain.RequestStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin_request_actions")
public class AdminRequestActionEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 24)
    private String requestReference;

    @Column(nullable = false, length = 320)
    private String actorEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RequestStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestPriority priority;

    @Column(length = 320)
    private String assigneeEmail;

    @Column(length = 3000)
    private String remarks;

    @Column(length = 5000)
    private String resolutionNotes;

    @Column(nullable = false)
    private Instant occurredAt = Instant.now();

    protected AdminRequestActionEntity() {
    }

    public AdminRequestActionEntity(String requestReference, String actorEmail, RequestStatus status,
            RequestPriority priority, String assigneeEmail, String remarks, String resolutionNotes) {
        this.requestReference = requestReference;
        this.actorEmail = actorEmail;
        this.status = status;
        this.priority = priority;
        this.assigneeEmail = assigneeEmail;
        this.remarks = remarks;
        this.resolutionNotes = resolutionNotes;
    }
}