package com.servicehubai.chat.infrastructure;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.servicehubai.chat.application.AiProvider;
import com.servicehubai.chat.application.AiProviderException;
import com.servicehubai.chat.domain.ChatSessionEntity;

import jakarta.annotation.PostConstruct;

@Component
@ConditionalOnProperty(name = "servicehub.ai.provider", havingValue = "anthropic")
public class AnthropicProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(AnthropicProvider.class);
    private final RestClient client;
    private final ObjectMapper objectMapper;
    private final String model;
    private final String apiKey;
    private final String baseUrl;
    private volatile boolean connected;
    private volatile String lastError;

    public AnthropicProvider(RestClient.Builder builder,
            org.springframework.core.env.Environment environment) {
        this.baseUrl = environment.getProperty("servicehub.ai.anthropic.base-url", "");
        this.model = environment.getProperty("servicehub.ai.anthropic.model", "");
        this.apiKey = environment.getProperty("servicehub.ai.anthropic.api-key", "");
        this.objectMapper = new ObjectMapper();
        org.springframework.http.client.SimpleClientHttpRequestFactory requestFactory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis());
        requestFactory.setReadTimeout((int) Duration.ofSeconds(10).toMillis());
        this.client = builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    @PostConstruct
    void validateConfiguration() {
        if (apiKey.isBlank()) {
            connected = false;
            lastError = "ANTHROPIC_API_KEY is not configured; provider is disabled.";
            log.warn("[ChatBot] Anthropic provider disabled: ANTHROPIC_API_KEY is missing. Fallback mode remains active.");
            return;
        }
        if (baseUrl.isBlank() || !baseUrl.startsWith("https://")) throw new IllegalStateException("ANTHROPIC_BASE_URL must be a valid HTTPS URL");
        if (model.isBlank()) throw new IllegalStateException("ANTHROPIC_MODEL is required when AI_PROVIDER=anthropic");
        log.info("[ChatBot] Provider: Claude Haiku 4.5, endpoint: {}, timeout: 10s, status: ENABLED", baseUrl);
    }

    @Override
    public String name() { return "anthropic"; }
    @Override
    public String displayName() { return "Claude Haiku 4.5"; }
    @Override
    public boolean enabled() { return !apiKey.isBlank() && !baseUrl.isBlank() && !model.isBlank(); }
    @Override
    public boolean connected() { return connected; }
    @Override
    public String model() { return model; }
    @Override
    public String endpoint() { return baseUrl; }
    @Override
    public String lastError() { return lastError; }

    @Override
    @SuppressWarnings("unchecked")
    public String answer(String systemPrompt, String conversation, String message, ChatSessionEntity session) {
        if (apiKey.isBlank()) throw new AiProviderException("Missing API key");
        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", 700,
                "system", systemPrompt,
                "messages", List.of(Map.of("role", "user", "content", conversation + "\nStudent: " + message)));
        try {
            byte[] payload = objectMapper.writeValueAsBytes(body);
            Map<String, Object> response = client.post().uri("/v1/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(payload.length))
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .body(payload)
                    .retrieve().body(Map.class);
            connected = true;
            lastError = null;
            log.info("[ChatBot] Connection successful");
            if (response == null || !(response.get("content") instanceof List<?> content) || content.isEmpty()) {
                throw new AiProviderException("Provider returned an empty response");
            }
            Object text = ((Map<String, Object>) content.get(0)).get("text");
            return text == null ? null : text.toString();
        } catch (JsonProcessingException exception) {
            connected = false;
            lastError = "Failed to encode Anthropic request payload: " + exception.getMessage();
            log.error("[ChatBot] Provider unavailable: {}", lastError);
            throw new AiProviderException(lastError, exception);
        } catch (RestClientResponseException exception) {
            connected = false;
            String detail = exception.getResponseBodyAsString();
            lastError = exception.getStatusCode() + " " + exception.getStatusText();
            if (detail != null && !detail.isBlank()) {
                lastError += " - " + detail;
            }
            log.error("[ChatBot] Provider unavailable: {}", lastError);
            throw new AiProviderException(lastError, exception);
        } catch (AiProviderException exception) {
            connected = false;
            lastError = exception.reason();
            throw exception;
        } catch (RuntimeException exception) {
            connected = false;
            lastError = "Network connection failed: " + exception.getMessage();
            log.error("[ChatBot] Provider unavailable: {}", lastError);
            throw new AiProviderException(lastError, exception);
        }
    }
}
