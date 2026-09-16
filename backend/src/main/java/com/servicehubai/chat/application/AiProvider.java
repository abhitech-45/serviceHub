package com.servicehubai.chat.application;

import com.servicehubai.chat.domain.ChatSessionEntity;

public interface AiProvider {
    String name();
    String answer(String systemPrompt, String conversation, String message, ChatSessionEntity session);
    default String displayName() { return name(); }
    default boolean enabled() { return true; }
    default boolean connected() { return false; }
    default String model() { return "unknown"; }
    default String endpoint() { return "unknown"; }
    default String lastError() { return null; }
    default String test() { return answer("You are a connectivity test assistant. Reply briefly.", "", "Hello", null); }
}
