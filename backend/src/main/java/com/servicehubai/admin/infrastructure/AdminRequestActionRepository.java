package com.servicehubai.admin.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.servicehubai.admin.domain.AdminRequestActionEntity;

public interface AdminRequestActionRepository extends JpaRepository<AdminRequestActionEntity, UUID> {
}