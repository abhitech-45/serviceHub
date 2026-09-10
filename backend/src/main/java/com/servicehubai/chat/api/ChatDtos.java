package com.servicehubai.chat.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.servicehubai.request.api.RequestDtos.Response;

public final class ChatDtos {

    private ChatDtos() {
    }

        public record Message(UUID sessionId, @NotBlank @Size(max = 2000) String message) {
    }

        public record Reply(UUID sessionId, String intent, String category, String suggestedPriority, String answer,
            boolean confirmationRequired, Response createdRequest, List<Response> matchingRequests,
            Instant respondedAt) {
    }
}