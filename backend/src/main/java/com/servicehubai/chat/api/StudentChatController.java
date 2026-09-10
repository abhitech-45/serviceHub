package com.servicehubai.chat.api;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.servicehubai.chat.api.ChatDtos.Message;
import com.servicehubai.chat.api.ChatDtos.Reply;
import com.servicehubai.chat.application.StudentChatService;

@RestController
@RequestMapping("/api/v1/chat")
public class StudentChatController {

    private final StudentChatService chatService;

    public StudentChatController(StudentChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/messages")
    Reply message(Authentication authentication, @Valid @RequestBody Message input) {
        return chatService.respond(authentication.getName(), input);
    }
}