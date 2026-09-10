package com.servicehubai.user.api;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.servicehubai.user.api.AuthDtos.AuthResponse;
import com.servicehubai.user.api.AuthDtos.LoginRequest;
import com.servicehubai.user.api.AuthDtos.RegisterRequest;
import com.servicehubai.user.api.AuthDtos.UserSummary;
import com.servicehubai.user.application.AuthService;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/register")
    ResponseEntity<UserSummary> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/auth/login")
    AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/auth/logout")
    ResponseEntity<Void> logout(Authentication authentication, @RequestHeader(name = "X-Refresh-Token", required = false) String refreshToken) {
        authService.logout(authentication.getName(), refreshToken);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/me")
    UserSummary currentUser(Authentication authentication) {
        return authService.currentUser(authentication.getName());
    }
}
