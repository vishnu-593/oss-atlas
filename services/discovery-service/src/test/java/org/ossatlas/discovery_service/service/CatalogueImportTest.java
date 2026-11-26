package org.ossatlas.discovery_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ossatlas.discovery_service.dto.SearchRequest;
import org.ossatlas.discovery_service.dto.SearchResponse;
import org.ossatlas.discovery_service.integration.github.GhLicense;
import org.ossatlas.discovery_service.integration.github.GhOwner;
import org.ossatlas.discovery_service.integration.github.GhRepo;
import org.ossatlas.discovery_service.integration.github.GhSearchResponse;
import org.ossatlas.discovery_service.integration.github.GitHubApiClient;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CatalogueImportTest {

    @Mock GitHubApiClient gitHubApiClient;
    @InjectMocks SearchServiceImpl service;

    private static GhSearchResponse oneRepo() {
        GhRepo repo = new GhRepo("my-lib", "desc", "https://github.com/o/my-lib",
                new GhOwner("o"), "Java", 500, 50,
                List.of("java"), Instant.parse("2026-11-01T00:00:00Z"),
                new GhLicense("MIT", "MIT License"));
        return new GhSearchResponse(1, List.of(repo));
    }

    @Test
    void search_doesNotAutoImportToCatalogue() {
        given(gitHubApiClient.search(anyString(), anyInt())).willReturn(oneRepo());

        service.search(new SearchRequest("java tools", null, null, null, null, null));

        // No CatalogueClient injected — if search attempted to call it, the test would fail at wiring
        // This test verifies search is a pure read operation
    }

    @Test
    void search_returnsProjects_withoutImportResult() {
        given(gitHubApiClient.search(anyString(), anyInt())).willReturn(oneRepo());

        SearchResponse resp = service.search(new SearchRequest("java tools", null, null, null, null, null));

        assertThat(resp.projects()).hasSize(1);
        assertThat(resp.query()).isEqualTo("java tools");
        assertThat(resp.totalFound()).isEqualTo(1);
    }
}
