package com.servicehubai.request.domain;

import java.time.Instant;
import java.util.UUID;

import com.servicehubai.user.domain.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "service_requests")
public class ServiceRequestEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 24)
    private String reference;

    @Column(nullable = false, length = 160)
    private String subject;

    @Column(nullable = false, length = 5000)
    private String description;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(name = "sub_category", nullable = false, length = 100)
    private String subCategory;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RequestPriority priority;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private UserEntity assignee;

    @Column(name = "resolution_notes", length = 5000)
    private String resolutionNotes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ServiceRequestEntity() {
    }

        public ServiceRequestEntity(String reference, String subject, String description, String category,
            String subCategory, RequestPriority priority, UserEntity owner) {
        this.reference = reference;
        this.subject = subject;
        this.description = description;
        this.category = category;
        this.subCategory = subCategory;
        this.priority = priority;
        this.owner = owner;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getReference() { return reference; }
    public String getSubject() { return subject; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getSubCategory() { return subCategory; }
    public RequestPriority getPriority() { return priority; }
    public RequestStatus getStatus() { return status; }
    public UserEntity getOwner() { return owner; }
    public UserEntity getAssignee() { return assignee; }
    public String getResolutionNotes() { return resolutionNotes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void update(String subject, String description, String category, String subCategory, RequestPriority priority) {
        this.subject = subject;
        this.description = description;
        this.category = category;
        this.subCategory = subCategory;
        this.priority = priority;
    }

    public void adminUpdate(RequestStatus status, RequestPriority priority) {
        this.status = status;
        this.priority = priority;
    }

    public void adminUpdate(RequestStatus status, RequestPriority priority, UserEntity assignee, String resolutionNotes) {
        this.status = status;
        this.priority = priority;
        this.assignee = assignee;
        this.resolutionNotes = resolutionNotes;
    }
}
