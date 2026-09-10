package com.servicehubai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServiceHubApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void applicationContextLoads() {
        assertThat(true).isTrue();
    }

    @Test
    void healthEchoesCorrelationId() throws Exception {
        mockMvc.perform(get("/api/v1/health").header("X-Correlation-ID", "smoke-test-correlation"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-ID", "smoke-test-correlation"))
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void openApiDocumentIsAvailable() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Campus Services Hub API"));
    }
}
