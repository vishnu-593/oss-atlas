package org.ossatlas.discovery_service.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ossatlas.discovery_service.client.CatalogueClient;
import org.ossatlas.discovery_service.exception.GlobalExceptionHandler;
import org.ossatlas.discovery_service.integration.catalogue.CatalogueImportResponse;
import org.ossatlas.discovery_service.service.SearchService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DiscoveryControllerTest {

    @Mock SearchService searchService;
    @Mock CatalogueClient catalogueClient;

    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DiscoveryController(searchService, catalogueClient))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void import_validRequest_returns200WithImportResult() throws Exception {
        CatalogueImportResponse importResponse =
                new CatalogueImportResponse(1, 1, 0, 0, "Project import completed");
        given(catalogueClient.importProjects(anyList())).willReturn(importResponse);

        mockMvc.perform(post("/api/discovery/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projects": [{
                                    "name": "spring-boot",
                                    "description": "Java framework",
                                    "githubUrl": "https://github.com/spring-projects/spring-boot",
                                    "owner": "spring-projects",
                                    "language": "Java",
                                    "license": "Apache-2.0",
                                    "stars": 75000,
                                    "forks": 40000,
                                    "topics": ["java","spring"],
                                    "lastUpdated": "2026-11-01T00:00:00Z"
                                  }]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.received").value(1))
                .andExpect(jsonPath("$.imported").value(1));
    }

    @Test
    void import_emptyProjects_returns400() throws Exception {
        mockMvc.perform(post("/api/discovery/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projects": []}
                                """))
                .andExpect(status().isBadRequest());
    }
}
