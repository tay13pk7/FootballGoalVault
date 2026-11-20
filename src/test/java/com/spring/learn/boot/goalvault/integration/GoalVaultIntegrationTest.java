package com.spring.learn.boot.goalvault.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * FULL-CONTEXT integration test — @SpringBootTest boots the ENTIRE ApplicationContext (all layers,
 * autoconfiguration, security, JPA, seed data) and hits a real MVC endpoint via MockMvc.
 * Chosen over @WebMvcTest here because we want to verify the controller + service + real H2 wiring
 * (dev-profile seed data is present) end-to-end, not just the web slice.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GoalVaultIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void metaEndpointReturnsAppMetadata() throws Exception {
        mockMvc.perform(get("/api/v1/meta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("GoalVault"))
                .andExpect(jsonPath("$.defaultPageSize").value(20));
    }

    @Test
    void teamsEndpointReturnsPagedContent() throws Exception {
        mockMvc.perform(get("/api/v1/teams?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists());
    }
}
