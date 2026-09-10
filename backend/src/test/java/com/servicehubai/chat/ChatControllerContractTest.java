package com.servicehubai.chat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void answersFaqWithDeterministicFallback() throws Exception {
        String token = registerAndLogin("chat-faq-" + System.nanoTime() + "@example.com");

        mockMvc.perform(post("/api/v1/chat/messages")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"How can I apply for a scholarship?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intent").value("FAQ_QUERY"))
                .andExpect(jsonPath("$.answer").isNotEmpty())
                .andExpect(jsonPath("$.confirmationRequired").value(false));
    }

    @Test
    void requiresConfirmationBeforeCreatingCase() throws Exception {
        String token = registerAndLogin("chat-create-" + System.nanoTime() + "@example.com");

        mockMvc.perform(post("/api/v1/chat/messages")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Please create a support ticket for my hostel WiFi issue\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intent").value("CREATE_SERVICE_REQUEST"))
                .andExpect(jsonPath("$.confirmationRequired").value(true))
                .andExpect(jsonPath("$.createdRequest").doesNotExist());
    }

    private String registerAndLogin(String email) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\",\"displayName\":\"Chat Student\"}"))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(login, "$.accessToken");
    }
}