package com.servicehubai.request.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "request_status_history")
public class RequestStatusHistoryEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private ServiceRequestEntity request;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RequestStatus status;

    @Column(name = "updated_by", nullable = false, length = 320)
    private String updatedBy;

    @Column(length = 3000)
    private String remarks;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt = Instant.now();

    protected RequestStatusHistoryEntity() {
    }

    public RequestStatusHistoryEntity(ServiceRequestEntity request, RequestStatus status, String updatedBy, String remarks) {
        this.request = request;
        this.status = status;
        this.updatedBy = updatedBy;
        this.remarks = remarks;
    }

    public RequestStatus getStatus() { return status; }
    public String getUpdatedBy() { return updatedBy; }
    public String getRemarks() { return remarks; }
    public Instant getOccurredAt() { return occurredAt; }
}