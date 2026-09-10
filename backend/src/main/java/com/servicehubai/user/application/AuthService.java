package com.servicehubai.user.application;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.servicehubai.audit.application.AuditService;
import com.servicehubai.common.exception.ApiException;
import com.servicehubai.security.JwtService;
import com.servicehubai.user.api.AuthDtos.AuthResponse;
import com.servicehubai.user.api.AuthDtos.LoginRequest;
import com.servicehubai.user.api.AuthDtos.RegisterRequest;
import com.servicehubai.user.api.AuthDtos.UserSummary;
import com.servicehubai.user.domain.UserEntity;
import com.servicehubai.user.infrastructure.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final ConcurrentHashMap<String, String> refreshSessions = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    @Transactional
    public UserSummary register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            auditService.record(email, "REGISTER", "REJECTED_DUPLICATE");
            throw new ApiException(HttpStatus.CONFLICT, "Registration rejected", "An account already exists for these details.");
        }
        UserEntity user = userRepository.save(new UserEntity(email, passwordEncoder.encode(request.password()), request.displayName().trim()));
        auditService.record(email, "REGISTER", "SUCCESS");
        return UserSummary.from(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        UserEntity user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null || !user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            auditService.record(email, "LOGIN", "REJECTED");
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication failed", "Invalid email or password.");
        }
        String accessToken = jwtService.issueAccessToken(user);
        String refreshToken = UUID.randomUUID().toString();
        refreshSessions.put(refreshToken, user.getEmail());
        auditService.record(email, "LOGIN", "SUCCESS");
        return new AuthResponse(accessToken, refreshToken, UserSummary.from(user));
    }

    public void logout(String email, String refreshToken) {
        if (refreshToken == null || !email.equals(refreshSessions.get(refreshToken))) {
            auditService.record(email, "LOGOUT", "REJECTED");
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Logout rejected", "The refresh session is invalid.");
        }
        refreshSessions.remove(refreshToken);
        auditService.record(email, "LOGOUT", "SUCCESS");
    }

    @Transactional(readOnly = true)
    public UserSummary currentUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(UserSummary::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found", "The authenticated user no longer exists."));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
