package com.spring.learn.boot.goalvault.controller;

import com.spring.learn.boot.goalvault.dto.TeamResponse;
import com.spring.learn.boot.goalvault.service.TeamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CONTROLLER-SLICE test — @WebMvcTest only loads the MVC infrastructure for TeamController.
 * Faster than @SpringBootTest and isolates the web layer; the service layer is faked with @MockBean.
 * addFilters=false skips Spring Security so we're testing routing/serialization only.
 */
@WebMvcTest(controllers = TeamController.class)
@AutoConfigureMockMvc(addFilters = false)
class TeamControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeamService service;

    @Test
    void getReturnsV1ShapeByDefault() throws Exception {
        when(service.get(any())).thenReturn(new TeamResponse(1L, "TestFC", "London", 1900, "Ground", 0));

        mockMvc.perform(get("/api/v1/teams/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("TestFC"));
    }
}
