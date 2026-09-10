package com.servicehubai.request.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.servicehubai.request.domain.RequestCommentEntity;

public interface RequestCommentRepository extends JpaRepository<RequestCommentEntity, UUID> {
}
