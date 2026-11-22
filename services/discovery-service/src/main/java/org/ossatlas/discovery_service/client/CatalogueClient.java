package org.ossatlas.discovery_service.client;

import org.ossatlas.discovery_service.dto.CataloguePage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CatalogueClient {

    private final RestClient restClient;

    public CatalogueClient(
            @Value("${catalogue.service.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public CataloguePage listAll(int size) {
        return restClient.get()
                .uri(ub -> ub.path("/api/projects")
                        .queryParam("page", 0)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .body(CataloguePage.class);
    }
}
