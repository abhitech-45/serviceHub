package com.servicehubai.user.domain;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(nullable = false)
    private boolean active = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = EnumSet.of(Role.CUSTOMER);

    protected UserEntity() {
    }

    public UserEntity(String email, String passwordHash, String displayName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }

    public static UserEntity administrator(String email, String passwordHash, String displayName) {
        UserEntity user = new UserEntity(email, passwordHash, displayName);
        user.roles = EnumSet.of(Role.ADMINISTRATOR);
        return user;
    }

    public static UserEntity supportAgent(String email, String passwordHash, String displayName) {
        UserEntity user = new UserEntity(email, passwordHash, displayName);
        user.roles = EnumSet.of(Role.SUPPORT_AGENT);
        return user;
    }

    public static UserEntity supportAgentDirectoryEntry(String email, String displayName) {
        return supportAgent(email, "!directory-only!" + UUID.randomUUID(), displayName);
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return active;
    }

    public Set<Role> getRoles() {
        return Set.copyOf(roles);
    }
}
