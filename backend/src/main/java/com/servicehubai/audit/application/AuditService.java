package com.servicehubai.audit.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.servicehubai.audit.domain.AuditLogEntity;
import com.servicehubai.audit.infrastructure.AuditLogRepository;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(String actorEmail, String action, String outcome) {
        repository.save(new AuditLogEntity(actorEmail, action, outcome));
    }
}
