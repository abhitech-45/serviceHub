package com.servicehubai.user.infrastructure;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.servicehubai.user.domain.UserEntity;
import com.servicehubai.user.domain.Role;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<UserEntity> findByActiveTrueAndRolesContainingOrderByDisplayNameAsc(Role role);
}
