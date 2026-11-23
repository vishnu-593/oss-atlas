package org.ossatlas.discovery_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ossatlas.discovery_service.client.CatalogueClient;
import org.ossatlas.discovery_service.dto.DiscoveryProject;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock GitHubApiClient gitHubApiClient;
    @Mock CatalogueClient catalogueClient;
    @InjectMocks SearchServiceImpl service;

    private static final CatalogueImportResponse OK_IMPORT =
            new CatalogueImportResponse(1, 1, 0, 0, "Project import completed");

    @BeforeEach
    void stubCatalogue() {
        org.mockito.Mockito.lenient()
                .when(catalogueClient.importProjects(org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(OK_IMPORT);
    }

    // ---- helpers ----

    private static GhRepo repo(String name, String url, String language, int stars) {
        return new GhRepo(name, "desc for " + name, url,
                new GhOwner("owner"), language, stars, 10,
                List.of("tag1"), Instant.parse("2026-11-20T10:00:00Z"),
                new GhLicense("MIT", "MIT License"));
    }

    private static GhRepo repoMinimal(String name, String url) {
        return new GhRepo(name, null, url, null, null, 0, 0, null,
                null, null);
    }

    private static GhSearchResponse response(GhRepo... repos) {
        return new GhSearchResponse(repos.length, List.of(repos));
    }

    private static SearchRequest req(String query) {
        return new SearchRequest(query, null, null, null, null, null);
    }

    // ---- 1. Successful GitHub search ----

    @Test
    void search_returnsMappedProjects() {
        given(gitHubApiClient.search(anyString(), anyInt()))
                .willReturn(response(repo("project-a", "https://github.com/o/project-a", "Java", 500)));

        SearchResponse result = service.search(req("java tools"));

        assertThat(result.query()).isEqualTo("java tools");
        assertThat(result.totalFound()).isEqualTo(1);
        assertThat(result.projects()).hasSize(1);
        DiscoveryProject p = result.projects().get(0);
        assertThat(p.name()).isEqualTo("project-a");
        assertThat(p.githubUrl()).isEqualTo("https://github.com/o/project-a");
        assertThat(p.language()).isEqualTo("Java");
        assertThat(p.stars()).isEqualTo(500);
        assertThat(p.license()).isEqualTo("MIT");
    }

    // ---- 2. Empty search results ----

    @Test
    void search_emptyResults_returnsEmptyList() {
        given(gitHubApiClient.search(anyString(), anyInt()))
                .willReturn(new GhSearchResponse(0, List.of()));

        SearchResponse result = service.search(req("obscure-query-xyz"));

        assertThat(result.totalFound()).isZero();
        assertThat(result.projects()).isEmpty();
    }

    // ---- 3. Keyword construction (keywords only) ----

    @Test
    void buildQuery_onlyKeywords() {
        String q = service.buildQuery(new SearchRequest("machine learning", null, null, null, null, null));
        assertThat(q).isEqualTo("machine learning");
    }

    // ---- 4. Language filter ----

    @Test
    void buildQuery_withLanguage_appendsQualifier() {
        String q = service.buildQuery(new SearchRequest("ml", "Python", null, null, null, null));
        assertThat(q).isEqualTo("ml language:Python");
    }

    @Test
    void search_withLanguage_passesQualifierToGitHub() {
        given(gitHubApiClient.search(anyString(), anyInt()))
                .willReturn(new GhSearchResponse(0, List.of()));
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);

        service.search(new SearchRequest("ml", "Python", null, null, null, null));

        org.mockito.Mockito.verify(gitHubApiClient).search(queryCaptor.capture(), anyInt());
        assertThat(queryCaptor.getValue()).contains("language:Python");
    }

    // ---- 5. Minimum stars ----

    @Test
    void buildQuery_withMinStars_appendsQualifier() {
        String q = service.buildQuery(new SearchRequest("ml", null, 100, null, null, null));
        assertThat(q).isEqualTo("ml stars:>=100");
    }

    @Test
    void search_withMinStars_passesQualifierToGitHub() {
        given(gitHubApiClient.search(anyString(), anyInt()))
                .willReturn(new GhSearchResponse(0, List.of()));
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);

        service.search(new SearchRequest("ml", null, 500, null, null, null));

        org.mockito.Mockito.verify(gitHubApiClient).search(queryCaptor.capture(), anyInt());
        assertThat(queryCaptor.getValue()).contains("stars:>=500");
    }

    // ---- 6. License filter ----

    @Test
    void buildQuery_withLicense_lowercasesAndAppendsQualifier() {
        String q = service.buildQuery(new SearchRequest("ml", null, null, "MIT", null, null));
        assertThat(q).isEqualTo("ml license:mit");
    }

    @Test
    void buildQuery_withLicense_alreadyLowercase() {
        String q = service.buildQuery(new SearchRequest("tools", null, null, "apache-2.0", null, null));
        assertThat(q).isEqualTo("tools license:apache-2.0");
    }

    // ---- 7. Topic filter ----

    @Test
    void buildQuery_withTopic_appendsQualifier() {
        String q = service.buildQuery(new SearchRequest("ml", null, null, null, "deep-learning", null));
        assertThat(q).isEqualTo("ml topic:deep-learning");
    }

    // ---- 8. All filters combined ----

    @Test
    void buildQuery_allFilters_buildsFullQuery() {
        String q = service.buildQuery(
                new SearchRequest("machine learning", "Python", 100, "MIT", "deep-learning", null));
        assertThat(q).isEqualTo("machine learning language:Python stars:>=100 license:mit topic:deep-learning");
    }

    // ---- 9. Missing metadata ----

    @Test
    void search_missingMetadata_usesDefaults() {
        given(gitHubApiClient.search(anyString(), anyInt()))
                .willReturn(response(repoMinimal("sparse-project", "https://github.com/o/sparse")));

        SearchResponse result = service.search(req("sparse"));

        DiscoveryProject p = result.projects().get(0);
        assertThat(p.owner()).isNull();
        assertThat(p.language()).isNull();
        assertThat(p.license()).isNull();
        assertThat(p.topics()).isEmpty();
        assertThat(p.stars()).isZero();
        assertThat(p.forks()).isZero();
        assertThat(p.lastUpdated()).isNull();
    }

    // ---- 10. Deduplication ----

    @Test
    void search_deduplicatesByNormalizedUrl() {
        GhRepo dup1 = repo("project-a", "https://github.com/o/project-a",   "Java", 100);
        GhRepo dup2 = repo("project-a", "https://github.com/o/project-a/", "Java", 100); // trailing slash
        GhRepo dup3 = repo("project-a", "HTTPS://GITHUB.COM/O/PROJECT-A",  "Java", 100); // uppercase
        GhRepo unique = repo("other", "https://github.com/o/other", "Go", 50);

        given(gitHubApiClient.search(anyString(), anyInt()))
                .willReturn(new GhSearchResponse(4, List.of(dup1, dup2, dup3, unique)));

        SearchResponse result = service.search(req("test"));

        assertThat(result.projects()).hasSize(2);
        assertThat(result.projects()).extracting(DiscoveryProject::name)
                .containsExactly("project-a", "other");
    }

    // ---- 11. GitHub API failure ----

    @Test
    void search_githubApiFailure_propagatesRestClientException() {
        given(gitHubApiClient.search(anyString(), anyInt()))
                .willThrow(new RestClientException("connection refused"));

        assertThatThrownBy(() -> service.search(req("test")))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("connection refused");
    }

    // ---- 12. Result limit ----

    @Test
    void search_respectsDefaultLimit_whenLimitNull() {
        given(gitHubApiClient.search(anyString(), eq(SearchServiceImpl.DEFAULT_LIMIT)))
                .willReturn(new GhSearchResponse(0, List.of()));

        service.search(new SearchRequest("test", null, null, null, null, null));

        org.mockito.Mockito.verify(gitHubApiClient).search(anyString(), eq(SearchServiceImpl.DEFAULT_LIMIT));
    }

    @Test
    void search_capsLimitAtMaximum() {
        given(gitHubApiClient.search(anyString(), eq(SearchServiceImpl.MAX_LIMIT)))
                .willReturn(new GhSearchResponse(0, List.of()));

        service.search(new SearchRequest("test", null, null, null, null, 999));

        org.mockito.Mockito.verify(gitHubApiClient).search(anyString(), eq(SearchServiceImpl.MAX_LIMIT));
    }

    @Test
    void search_usesProvidedLimit() {
        given(gitHubApiClient.search(anyString(), eq(5)))
                .willReturn(new GhSearchResponse(0, List.of()));

        service.search(new SearchRequest("test", null, null, null, null, 5));

        org.mockito.Mockito.verify(gitHubApiClient).search(anyString(), eq(5));
    }
}
