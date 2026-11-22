package org.ossatlas.discovery_service.client;

import org.ossatlas.discovery_service.dto.GithubSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class SourceIntegrationClient {

    private final RestClient restClient;

    public SourceIntegrationClient(
            @Value("${source-integration.service.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public GithubSearchResponse search(String query, int page, int size) {
        return restClient.get()
                .uri(ub -> ub.path("/api/github/search")
                        .queryParam("q", query)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .body(GithubSearchResponse.class);
    }
}
