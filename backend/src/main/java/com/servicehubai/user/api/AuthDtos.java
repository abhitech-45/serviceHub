package com.servicehubai.user.api;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.servicehubai.user.domain.UserEntity;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 128) String password,
            @NotBlank @Size(max = 120) String displayName) {
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    public record UserSummary(UUID id, String email, String displayName, List<String> roles) {
        public static UserSummary from(UserEntity user) {
            return new UserSummary(user.getId(), user.getEmail(), user.getDisplayName(),
                    user.getRoles().stream().map(Enum::name).sorted().toList());
        }
    }

    public record AuthResponse(String accessToken, String refreshToken, UserSummary user) {
    }
}
