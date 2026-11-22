package org.ossatlas.discovery_service.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ossatlas.discovery_service.dto.DiscoveredProject;
import org.ossatlas.discovery_service.dto.DiscoveryResult;
import org.ossatlas.discovery_service.dto.ProjectStatus;
import org.ossatlas.discovery_service.exception.GlobalExceptionHandler;
import org.ossatlas.discovery_service.service.DiscoveryService;
import org.ossatlas.discovery_service.service.SearchService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DiscoveryControllerTest {

    @Mock DiscoveryService discoveryService;
    @Mock SearchService searchService;

    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DiscoveryController(discoveryService, searchService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static final DiscoveryResult SAMPLE = new DiscoveryResult(
            "react", 2, 0, 10,
            List.of(
                    new DiscoveredProject(1L, "facebook/react", "A JS library",
                            "https://github.com/facebook/react", "JavaScript", 231000,
                            "MIT License", ProjectStatus.INDEXED, 4L),
                    new DiscoveredProject(2L, "pmndrs/react-three-fiber", "React renderer for Three.js",
                            "https://github.com/pmndrs/react-three-fiber", "TypeScript", 25000,
                            "MIT License", ProjectStatus.NEW, null)
            )
    );

    @Test
    void discover_returns200WithResults() throws Exception {
        given(discoveryService.discover(eq("react"), anyInt(), anyInt())).willReturn(SAMPLE);

        mockMvc.perform(get("/api/discover").param("q", "react"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("react"))
                .andExpect(jsonPath("$.totalGithubResults").value(2))
                .andExpect(jsonPath("$.results[0].status").value("INDEXED"))
                .andExpect(jsonPath("$.results[0].catalogueId").value(4))
                .andExpect(jsonPath("$.results[1].status").value("NEW"))
                .andExpect(jsonPath("$.results[1].catalogueId").doesNotExist());
    }

    @Test
    void discover_missingQuery_returns400() throws Exception {
        mockMvc.perform(get("/api/discover"))
                .andExpect(status().isBadRequest());
    }
}
