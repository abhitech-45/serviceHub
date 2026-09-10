package com.servicehubai.request.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.servicehubai.request.domain.RequestStatusHistoryEntity;

public interface RequestStatusHistoryRepository extends JpaRepository<RequestStatusHistoryEntity, UUID> {
    List<RequestStatusHistoryEntity> findByRequestReferenceOrderByOccurredAtAsc(String reference);
}