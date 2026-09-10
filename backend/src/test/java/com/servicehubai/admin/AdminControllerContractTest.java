package com.servicehubai.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.jayway.jsonpath.JsonPath;
import com.servicehubai.user.domain.UserEntity;
import com.servicehubai.user.infrastructure.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @BeforeEach
        void seedAdmin() {
                if (!userRepository.existsByEmailIgnoreCase("admin@servicehub.local")) {
                        userRepository.save(UserEntity.administrator("admin@servicehub.local",
                                        passwordEncoder.encode("Admin@12345"), "ServiceHub Administrator"));
                }
        }

    @Test
    void localAdminCanReadOverview() throws Exception {
        String login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@servicehub.local\",\"password\":\"Admin@12345\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(login, "$.accessToken");

        mockMvc.perform(get("/api/v1/admin/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").isNumber())
                .andExpect(jsonPath("$.totalRequests").isNumber());

    }

    @Test
    void customerCannotReadAdminOverview() throws Exception {
        String email = "customer-admin-check-" + System.nanoTime() + "@example.com";
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\",\"displayName\":\"Customer\"}"))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(login, "$.accessToken");

        mockMvc.perform(get("/api/v1/admin/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminUpdateAppendsHistoryAndResolutionDetails() throws Exception {
        String customerEmail = "lifecycle-customer-" + System.nanoTime() + "@example.com";
        String customerToken = registerAndLogin(customerEmail);
        String created = mockMvc.perform(post("/api/v1/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subject\":\"Lifecycle test\",\"description\":\"Track me\",\"category\":\"Academic Support\",\"subCategory\":\"Course Registration\",\"priority\":\"MEDIUM\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String reference = JsonPath.read(created, "$.reference");
        String adminToken = adminToken();

        mockMvc.perform(patch("/api/v1/admin/requests/{reference}", reference)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"UNDER_REVIEW\",\"priority\":\"HIGH\",\"remarks\":\"Admin started review\",\"resolutionNotes\":\"Pending investigation\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.resolutionNotes").value("Pending investigation"))
                .andExpect(jsonPath("$.history.length()").value(2))
                .andExpect(jsonPath("$.history[1].remarks").value("Admin started review"))
                .andExpect(jsonPath("$.history[1].updatedBy").value("admin@servicehub.local"));
    }

    private String adminToken() throws Exception {
        String login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@servicehub.local\",\"password\":\"Admin@12345\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(login, "$.accessToken");
    }

    private String registerAndLogin(String email) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\",\"displayName\":\"Customer\"}"))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(login, "$.accessToken");
    }
}
