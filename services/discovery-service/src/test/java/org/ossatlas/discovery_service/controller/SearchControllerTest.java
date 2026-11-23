package org.ossatlas.discovery_service.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ossatlas.discovery_service.dto.SearchResponse;
import org.ossatlas.discovery_service.exception.GlobalExceptionHandler;
import org.ossatlas.discovery_service.service.DiscoveryService;
import org.ossatlas.discovery_service.service.SearchService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock SearchService searchService;
    @Mock DiscoveryService discoveryService;

    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DiscoveryController(discoveryService, searchService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void search_validRequest_returns200WithResults() throws Exception {
        SearchResponse response = new SearchResponse("machine learning", 1, List.of(), null);
        given(searchService.search(any())).willReturn(response);

        mockMvc.perform(post("/api/discovery/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query":"machine learning","language":"Python","minStars":100}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("machine learning"))
                .andExpect(jsonPath("$.totalFound").value(1))
                .andExpect(jsonPath("$.projects").isArray());
    }

    @Test
    void search_missingQuery_returns400() throws Exception {
        mockMvc.perform(post("/api/discovery/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"Python"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_blankQuery_returns400() throws Exception {
        mockMvc.perform(post("/api/discovery/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query":"   "}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_limitAtMax_returns200() throws Exception {
        SearchResponse response = new SearchResponse("test", 0, List.of(), null);
        given(searchService.search(any())).willReturn(response);

        mockMvc.perform(post("/api/discovery/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query":"test","limit":50}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void search_limitAboveMax_returns400() throws Exception {
        mockMvc.perform(post("/api/discovery/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query":"test","limit":51}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_limitZero_returns400() throws Exception {
        mockMvc.perform(post("/api/discovery/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query":"test","limit":0}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_negativeMinStars_returns400() throws Exception {
        mockMvc.perform(post("/api/discovery/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query":"test","minStars":-1}
                                """))
                .andExpect(status().isBadRequest());
    }
}
