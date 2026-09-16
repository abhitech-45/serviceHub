package com.servicehubai.chat.api;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import jakarta.validation.Valid;

import com.servicehubai.chat.api.ChatDtos.Message;
import com.servicehubai.chat.api.ChatDtos.Reply;
import com.servicehubai.chat.application.StudentChatService;
import com.servicehubai.chat.application.AiProvider;
import com.servicehubai.chat.infrastructure.AiUsageRepository;

@RestController
@RequestMapping("/api/v1/chat")
public class StudentChatController {

    private final StudentChatService chatService;
    private final ObjectProvider<AiProvider> provider;
    private final AiUsageRepository usageRepository;
    private final int dailyLimit;

    public StudentChatController(StudentChatService chatService, ObjectProvider<AiProvider> provider,
            AiUsageRepository usageRepository, @Value("${servicehub.ai.llm.daily-limit:15}") int dailyLimit) {
        this.chatService = chatService;
        this.provider = provider;
        this.usageRepository = usageRepository;
        this.dailyLimit = dailyLimit;
    }

    @PostMapping("/messages")
    Reply message(Authentication authentication, @Valid @RequestBody Message input) {
        return chatService.respond(authentication.getName(), input);
    }

    @GetMapping("/health")
    Health health() {
        AiProvider ai = provider.getIfAvailable();
        long dailyUsage = usageRepository.countByOccurredAtGreaterThanEqual(
                LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant());
        return new Health(ai == null ? "Disabled" : ai.displayName(), ai != null && ai.enabled(),
                ai != null && ai.connected(), dailyUsage, dailyLimit,
                ai == null ? null : ai.lastError(), ai == null ? null : ai.model(), ai == null ? null : ai.endpoint());
    }

    @PostMapping("/test")
    TestReply test() {
        AiProvider ai = provider.getIfAvailable();
        if (ai == null) return new TestReply(false, "AI provider is disabled.");
        try {
            return new TestReply(true, ai.test());
        } catch (RuntimeException exception) {
            return new TestReply(false, "AI service unavailable. Reason: " + exception.getMessage());
        }
    }

    public record Health(String provider, boolean enabled, boolean connected, long dailyUsage, long dailyLimit,
            String lastProviderError, String model, String endpoint) { }
    public record TestReply(boolean connected, String response) { }
}