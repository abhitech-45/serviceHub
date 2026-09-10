package com.servicehubai.request.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.servicehubai.request.domain.ServiceRequestEntity;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequestEntity, UUID> {
    List<ServiceRequestEntity> findByOwnerEmailIgnoreCaseOrderByCreatedAtDesc(String email);
    Optional<ServiceRequestEntity> findByReferenceAndOwnerEmailIgnoreCase(String reference, String email);
    Optional<ServiceRequestEntity> findByReference(String reference);
    long countByStatusNot(com.servicehubai.request.domain.RequestStatus status);
    long countByPriorityIn(List<com.servicehubai.request.domain.RequestPriority> priorities);
}
