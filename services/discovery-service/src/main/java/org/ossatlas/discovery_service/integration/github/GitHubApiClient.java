package org.ossatlas.discovery_service.integration.github;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GitHubApiClient {

    private final RestClient restClient;

    public GitHubApiClient(
            @Value("${github.api.base-url}") String baseUrl,
            @Value("${github.api.token}")    String token) {

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28");

        if (token != null && !token.isBlank()) {
            builder.defaultHeader("Authorization", "Bearer " + token);
        }

        this.restClient = builder.build();
    }

    public GhSearchResponse search(String query, int perPage) {
        return restClient.get()
                .uri(ub -> ub.path("/search/repositories")
                        .queryParam("q", query)
                        .queryParam("sort", "stars")
                        .queryParam("order", "desc")
                        .queryParam("per_page", perPage)
                        .build())
                .retrieve()
                .body(GhSearchResponse.class);
    }
}
