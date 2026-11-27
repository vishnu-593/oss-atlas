package org.ossatlas.discovery_service.refresh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ossatlas.discovery_service.client.CatalogueClient;
import org.ossatlas.discovery_service.dto.CataloguePage;
import org.ossatlas.discovery_service.dto.CatalogueProjectSummary;
import org.ossatlas.discovery_service.dto.DiscoveryProject;
import org.ossatlas.discovery_service.integration.catalogue.CatalogueImportResponse;
import org.ossatlas.discovery_service.integration.github.GhOwner;
import org.ossatlas.discovery_service.integration.github.GhRepo;
import org.ossatlas.discovery_service.integration.github.GhSearchResponse;
import org.ossatlas.discovery_service.integration.github.GitHubApiClient;
import org.ossatlas.discovery_service.service.SearchServiceImpl;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshJobTest {

    @Mock RefreshProperties props;
    @Mock CatalogueClient catalogueClient;
    @Mock GitHubApiClient gitHubApiClient;
    @Mock SearchServiceImpl searchService;
    @Mock RefreshCursorRepository cursorRepository;
    @InjectMocks RefreshJob job;

    @BeforeEach
    void defaults() {
        org.mockito.Mockito.lenient().when(props.isEnabled()).thenReturn(true);
        org.mockito.Mockito.lenient().when(props.getPageSize()).thenReturn(50);
        org.mockito.Mockito.lenient().when(props.getQueries()).thenReturn(List.of());
        org.mockito.Mockito.lenient().when(cursorRepository.findById(anyString()))
                .thenReturn(java.util.Optional.empty());
        // Phase 1 always calls listForRefresh; default to empty so discovery-only tests pass
        org.mockito.Mockito.lenient().when(catalogueClient.listForRefresh(anyInt(), anyInt()))
                .thenReturn(emptyPage());
    }

    private static CataloguePage emptyPage() {
        return new CataloguePage(List.of());
    }

    private static CataloguePage pageOf(CatalogueProjectSummary... items) {
        return new CataloguePage(List.of(items));
    }

    private static GhRepo ghRepo(String name) {
        return new GhRepo(name, "desc", "https://github.com/o/" + name,
                new GhOwner("o"), "Java", 100, 10, List.of(), Instant.now(), null, 0);
    }

    private static DiscoveryProject discoveryProject(String name) {
        return new DiscoveryProject(name, "desc", "https://github.com/o/" + name,
                "o", "Java", "MIT", 100, 10, List.of(), Instant.now(), 0, 68, "Active");
    }

    // ---- 3. Successful scheduled refresh ----

    @Test
    @Disabled("Phase 1 disabled — re-enable when Core API rate limit is handled")
    void run_refreshesExistingCatalogueProjects() {
        CatalogueProjectSummary summary =
                new CatalogueProjectSummary("1", "https://github.com/owner/repo");
        given(catalogueClient.listForRefresh(0, 50)).willReturn(pageOf(summary));
        given(gitHubApiClient.getRepo("owner", "repo")).willReturn(ghRepo("repo"));
        given(searchService.toProject(any())).willReturn(discoveryProject("repo"));
        given(catalogueClient.importProjects(anyList()))
                .willReturn(new CatalogueImportResponse(1, 0, 1, 0, "Project import completed"));

        job.run();

        verify(gitHubApiClient).getRepo("owner", "repo");
        verify(catalogueClient).importProjects(anyList());
    }

    // ---- 4. Empty refresh results ----

    @Test
    @Disabled("Phase 1 disabled — re-enable when Core API rate limit is handled")
    void run_emptyCatalogue_completesSilently() {
        given(catalogueClient.listForRefresh(0, 50)).willReturn(emptyPage());

        assertThatCode(() -> job.run()).doesNotThrowAnyException();
        verify(catalogueClient, never()).importProjects(anyList());
    }

    // ---- 5. GitHub failure during refresh ----

    @Test
    @Disabled("Phase 1 disabled — re-enable when Core API rate limit is handled")
    void run_githubFailure_continuesAndLogsFailure() {
        CatalogueProjectSummary summary =
                new CatalogueProjectSummary("1", "https://github.com/owner/repo");
        given(catalogueClient.listForRefresh(0, 50)).willReturn(pageOf(summary));
        given(gitHubApiClient.getRepo("owner", "repo"))
                .willThrow(new RestClientException("connection refused"));

        assertThatCode(() -> job.run()).doesNotThrowAnyException();
        verify(catalogueClient, never()).importProjects(anyList());
    }

    @Test
    @Disabled("Phase 1 disabled — re-enable when Core API rate limit is handled")
    void run_githubRateLimit_stopsAndLogsWarning() {
        CatalogueProjectSummary summary =
                new CatalogueProjectSummary("1", "https://github.com/owner/repo");
        given(catalogueClient.listForRefresh(0, 50)).willReturn(pageOf(summary));
        RestClientResponseException rateLimited = new RestClientResponseException(
                "rate limited", 403, "Forbidden", null,
                "{\"message\":\"rate limit\"}".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        given(gitHubApiClient.getRepo("owner", "repo")).willThrow(rateLimited);

        assertThatCode(() -> job.run()).doesNotThrowAnyException();
    }

    // ---- 7. Scheduler disabled ----

    @Test
    void run_whenDisabled_doesNotCallGitHubOrCatalogue() {
        given(props.isEnabled()).willReturn(false);

        job.run();

        verifyNoInteractions(gitHubApiClient, catalogueClient);
    }

    // ---- 8. No overlapping executions ----

    @Test
    void run_secondCallWhileRunning_isSkipped() throws InterruptedException {
        // Force the flag into "running" state
        java.lang.reflect.Field f;
        try {
            f = RefreshJob.class.getDeclaredField("running");
            f.setAccessible(true);
            ((java.util.concurrent.atomic.AtomicBoolean) f.get(job)).set(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        job.run();

        verifyNoInteractions(catalogueClient);
    }

    // ---- 9. Partial failures and logging ----

    @Test
    @Disabled("Phase 1 disabled — re-enable when Core API rate limit is handled")
    void run_partialFailures_remainingProjectsStillImported() {
        CatalogueProjectSummary good = new CatalogueProjectSummary("1", "https://github.com/owner/good");
        CatalogueProjectSummary bad  = new CatalogueProjectSummary("2", "https://github.com/owner/bad");
        given(catalogueClient.listForRefresh(0, 50)).willReturn(pageOf(good, bad));
        given(gitHubApiClient.getRepo("owner", "good")).willReturn(ghRepo("good"));
        given(gitHubApiClient.getRepo("owner", "bad"))
                .willThrow(new RestClientException("not found"));
        given(searchService.toProject(any())).willReturn(discoveryProject("good"));
        given(catalogueClient.importProjects(anyList()))
                .willReturn(new CatalogueImportResponse(1, 0, 1, 0, "Project import completed"));

        assertThatCode(() -> job.run()).doesNotThrowAnyException();
        verify(catalogueClient).importProjects(anyList());
    }

    @Test
    @Disabled("Phase 1 disabled — re-enable when Core API rate limit is handled")
    void run_catalogueImportFails_doesNotCrash() {
        CatalogueProjectSummary summary =
                new CatalogueProjectSummary("1", "https://github.com/owner/repo");
        given(catalogueClient.listForRefresh(0, 50)).willReturn(pageOf(summary));
        given(gitHubApiClient.getRepo("owner", "repo")).willReturn(ghRepo("repo"));
        given(searchService.toProject(any())).willReturn(discoveryProject("repo"));
        given(catalogueClient.importProjects(anyList()))
                .willThrow(new RestClientException("catalogue down"));

        assertThatCode(() -> job.run()).doesNotThrowAnyException();
    }

    // ---- Discovery queries ----

    @Test
    void run_withDiscoveryQuery_searchesGitHubAndImports() {
        DiscoveryQuery dq = new DiscoveryQuery("spring boot", null, null, null, null, 5);
        given(props.getQueries()).willReturn(List.of(dq));
        GhRepo repo = ghRepo("spring-example");
        given(cursorRepository.findById(anyString())).willReturn(java.util.Optional.empty());
        given(gitHubApiClient.search(contains("spring boot"), eq(5), eq(1)))
                .willReturn(new GhSearchResponse(1, List.of(repo)));
        given(searchService.toProject(repo)).willReturn(discoveryProject("spring-example"));
        given(searchService.deduplicate(anyList())).willAnswer(inv -> inv.getArgument(0));
        given(catalogueClient.importProjects(anyList()))
                .willReturn(new CatalogueImportResponse(1, 1, 0, 0, "Project import completed"));

        job.run();

        verify(gitHubApiClient).search(contains("spring boot"), eq(5), eq(1));
        verify(catalogueClient).importProjects(anyList());
    }
}
