package com.servicehubai.request.domain;

import java.time.Instant;
import java.util.UUID;

import com.servicehubai.user.domain.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "request_comments")
public class RequestCommentEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private ServiceRequestEntity request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity author;

    @Column(nullable = false, length = 3000)
    private String body;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected RequestCommentEntity() {
    }

    public RequestCommentEntity(ServiceRequestEntity request, UserEntity author, String body) {
        this.request = request;
        this.author = author;
        this.body = body;
    }

    public String getBody() { return body; }
    public String getAuthorEmail() { return author.getEmail(); }
    public String getRequestReference() { return request.getReference(); }
    public Instant getCreatedAt() { return createdAt; }
}
