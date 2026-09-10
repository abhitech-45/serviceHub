package com.servicehubai.request;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class RequestControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void customerCanCreateReadUpdateAndCommentOnOwnRequest() throws Exception {
        String email = "request-owner-" + System.nanoTime() + "@example.com";
        String token = registerAndLogin(email);
        String createBody = """
                {"subject":"VPN access","description":"I cannot connect to the company VPN.","category":"IT Helpdesk","subCategory":"Wi-Fi Issues","priority":"HIGH"}
                """;

        String response = mockMvc.perform(post("/api/v1/requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.ownerEmail").value(email))
                .andExpect(jsonPath("$.history.length()").value(1))
                .andExpect(jsonPath("$.history[0].status").value("OPEN"))
                .andReturn().getResponse().getContentAsString();
        String reference = JsonPath.read(response, "$.reference");

        mockMvc.perform(get("/api/v1/requests/{reference}", reference)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reference").value(reference));

        mockMvc.perform(patch("/api/v1/requests/{reference}", reference)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subject\":\"VPN access updated\",\"description\":\"Updated details\",\"category\":\"IT Helpdesk\",\"subCategory\":\"Wi-Fi Issues\",\"priority\":\"CRITICAL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("CRITICAL"))
                .andExpect(jsonPath("$.history.length()").value(1));

        mockMvc.perform(post("/api/v1/requests/{reference}/comments", reference)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"I am available for troubleshooting.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments[0].body").value("I am available for troubleshooting."));

        mockMvc.perform(get("/api/v1/requests")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reference").value(reference));
    }

    @Test
    void customerCannotReadAnotherCustomersRequest() throws Exception {
        String ownerToken = registerAndLogin("owner-" + System.nanoTime() + "@example.com");
        String otherToken = registerAndLogin("other-" + System.nanoTime() + "@example.com");
        String response = mockMvc.perform(post("/api/v1/requests")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subject\":\"Private request\",\"description\":\"Private details\",\"category\":\"IT Helpdesk\",\"subCategory\":\"Student Portal Access\",\"priority\":\"LOW\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String reference = JsonPath.read(response, "$.reference");

        mockMvc.perform(get("/api/v1/requests/{reference}", reference)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("The request does not exist or is not accessible."));
    }

        @Test
        void invalidCampusSubCategoryIsRejected() throws Exception {
                String token = registerAndLogin("invalid-taxonomy-" + System.nanoTime() + "@example.com");

                mockMvc.perform(post("/api/v1/requests")
                                                .header("Authorization", "Bearer " + token)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("{\"subject\":\"Invalid category\",\"description\":\"Invalid taxonomy\",\"category\":\"Academic Support\",\"subCategory\":\"Password Reset\",\"priority\":\"LOW\"}"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("Invalid service category"));
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
