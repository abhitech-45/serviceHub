package com.servicehubai.chat.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    @ConditionalOnBean(ChatClient.Builder.class)
    ChatClient studentSupportChatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}