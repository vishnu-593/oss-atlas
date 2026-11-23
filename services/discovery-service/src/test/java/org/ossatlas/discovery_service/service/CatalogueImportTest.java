package org.ossatlas.discovery_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ossatlas.discovery_service.client.CatalogueClient;
import org.ossatlas.discovery_service.dto.SearchRequest;
import org.ossatlas.discovery_service.dto.SearchResponse;
import org.ossatlas.discovery_service.integration.catalogue.CatalogueImportResponse;
import org.ossatlas.discovery_service.integration.github.GhLicense;
import org.ossatlas.discovery_service.integration.github.GhOwner;
import org.ossatlas.discovery_service.integration.github.GhRepo;
import org.ossatlas.discovery_service.integration.github.GhSearchResponse;
import org.ossatlas.discovery_service.integration.github.GitHubApiClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CatalogueImportTest {

    @Mock GitHubApiClient gitHubApiClient;
    @Mock CatalogueClient catalogueClient;
    @InjectMocks SearchServiceImpl service;

    private static GhSearchResponse oneRepo() {
        GhRepo repo = new GhRepo("my-lib", "desc", "https://github.com/o/my-lib",
                new GhOwner("o"), "Java", 500, 50,
                List.of("java"), Instant.parse("2026-11-01T00:00:00Z"),
                new GhLicense("MIT", "MIT License"));
        return new GhSearchResponse(1, List.of(repo));
    }

    @BeforeEach
    void stubGitHub() {
        given(gitHubApiClient.search(anyString(), anyInt())).willReturn(oneRepo());
    }

    // ---- 1. Successful Catalogue import ----

    @Test
    void search_forwardsProjectsToCatalogue() {
        given(catalogueClient.importProjects(anyList()))
                .willReturn(new CatalogueImportResponse(1, 1, 0, 0, "Project import completed"));

        SearchResponse resp = service.search(new SearchRequest("java tools", null, null, null, null, null));

        verify(catalogueClient).importProjects(anyList());
        assertThat(resp.importResult()).isNotNull();
        assertThat(resp.importResult().imported()).isEqualTo(1);
        assertThat(resp.importResult().error()).isNull();
    }

    @Test
    void search_importResultReflectsUpdatedCount() {
        given(catalogueClient.importProjects(anyList()))
                .willReturn(new CatalogueImportResponse(1, 0, 1, 0, "Project import completed"));

        SearchResponse resp = service.search(new SearchRequest("java tools", null, null, null, null, null));

        assertThat(resp.importResult().imported()).isZero();
        assertThat(resp.importResult().updated()).isEqualTo(1);
    }

    // ---- 2. Catalogue unavailable ----

    @Test
    void search_catalogueDown_returnsProjectsWithErrorInImportResult() {
        given(catalogueClient.importProjects(anyList()))
                .willThrow(new RestClientException("Connection refused"));

        SearchResponse resp = service.search(new SearchRequest("java tools", null, null, null, null, null));

        assertThat(resp.projects()).hasSize(1);
        assertThat(resp.importResult()).isNotNull();
        assertThat(resp.importResult().imported()).isZero();
        assertThat(resp.importResult().error()).contains("Connection refused");
    }

    @Test
    void search_catalogueDown_stillReturnsDiscoveredProjects() {
        given(catalogueClient.importProjects(anyList()))
                .willThrow(new RestClientException("timeout"));

        SearchResponse resp = service.search(new SearchRequest("spring", null, null, null, null, null));

        assertThat(resp.projects()).isNotEmpty();
        assertThat(resp.query()).isEqualTo("spring");
        assertThat(resp.totalFound()).isGreaterThan(0);
    }

    // ---- 6. Duplicate project handling ----

    @Test
    void search_deduplicatedProjectsSentToCatalogue() {
        GhRepo dup1 = new GhRepo("my-lib", "d", "https://github.com/o/my-lib",
                new GhOwner("o"), "Java", 10, 1, List.of(), Instant.now(), null);
        GhRepo dup2 = new GhRepo("my-lib", "d", "https://github.com/o/my-lib/",
                new GhOwner("o"), "Java", 10, 1, List.of(), Instant.now(), null);
        given(gitHubApiClient.search(anyString(), anyInt()))
                .willReturn(new GhSearchResponse(2, List.of(dup1, dup2)));
        given(catalogueClient.importProjects(anyList()))
                .willReturn(new CatalogueImportResponse(1, 1, 0, 0, "Project import completed"));

        SearchResponse resp = service.search(new SearchRequest("test", null, null, null, null, null));

        assertThat(resp.projects()).hasSize(1);
        assertThat(resp.importResult().received()).isEqualTo(1);
    }
}
