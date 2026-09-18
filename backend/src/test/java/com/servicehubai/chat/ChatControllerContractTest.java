package com.servicehubai.chat;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Method;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.servicehubai.chat.application.StudentChatService;
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

    @Autowired
    private StudentChatService studentChatService;

    @Test
    void systemPromptAllowsGeneralKnowledgeWhileKeepingCampusSupport() throws Exception {
        Method promptMethod = StudentChatService.class.getDeclaredMethod("systemPrompt");
        promptMethod.setAccessible(true);
        String prompt = (String) promptMethod.invoke(studentChatService);
        String lower = prompt.toLowerCase(Locale.ROOT);

        assertTrue(lower.contains("general knowledge"));
        assertTrue(lower.contains("campus support"));
        assertTrue(lower.contains("ticket creation") || lower.contains("ticket tracking") || lower.contains("create and track support requests"));
    }

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
    void respondsContextuallyWhenStudentIdCardIsLost() throws Exception {
        String token = registerAndLogin("chat-id-card-" + System.nanoTime() + "@example.com");

        mockMvc.perform(post("/api/v1/chat/messages")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"I lost my student ID card\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intent").value("GENERAL_ASSISTANCE"))
                .andExpect(jsonPath("$.answer").value(org.hamcrest.Matchers.containsString("student ID card")))
                .andExpect(jsonPath("$.answer").value(org.hamcrest.Matchers.containsString("create")));
    }

    @Test
    void exposesProviderHealthWhenProviderIsDisabledInTests() throws Exception {
        String token = registerAndLogin("chat-health-" + System.nanoTime() + "@example.com");

        mockMvc.perform(get("/api/v1/chat/health")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.dailyLimit").value(15));
    }

    @Test
    void exposesDisabledProviderConnectivityTestInTests() throws Exception {
        String token = registerAndLogin("chat-test-" + System.nanoTime() + "@example.com");

        mockMvc.perform(post("/api/v1/chat/test")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(false));
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

            @Test
            void returnsNoOpenCasesMessageForTrackTicket() throws Exception {
            String token = registerAndLogin("chat-track-empty-" + System.nanoTime() + "@example.com");

            mockMvc.perform(post("/api/v1/chat/messages")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"message\":\"Track Ticket\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intent").value("CHECK_TICKET_STATUS"))
                .andExpect(jsonPath("$.answer").value("You have no open support cases."));
            }

            @Test
            void confirmsAndCreatesAcademicCase() throws Exception {
            String token = registerAndLogin("chat-create-confirm-" + System.nanoTime() + "@example.com");

            String pending = mockMvc.perform(post("/api/v1/chat/messages")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"message\":\"Please create a support request for my academic attendance issue\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
            String sessionId = JsonPath.read(pending, "$.sessionId");

            mockMvc.perform(post("/api/v1/chat/messages")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"sessionId\":\"" + sessionId + "\",\"message\":\"yes\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdRequest.reference").isNotEmpty())
                .andExpect(jsonPath("$.createdRequest.category").value("Academic Support"));
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