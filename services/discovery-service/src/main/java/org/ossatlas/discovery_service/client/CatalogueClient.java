package org.ossatlas.discovery_service.client;

import lombok.extern.slf4j.Slf4j;
import org.ossatlas.discovery_service.dto.CataloguePage;
import org.ossatlas.discovery_service.dto.DiscoveryProject;
import org.ossatlas.discovery_service.integration.catalogue.CatalogueImportItem;
import org.ossatlas.discovery_service.integration.catalogue.CatalogueImportRequest;
import org.ossatlas.discovery_service.integration.catalogue.CatalogueImportResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Slf4j
@Service
public class CatalogueClient {

    private final RestClient restClient;

    public CatalogueClient(@Value("${oss-atlas.catalogue.base-url}") String baseUrl) {
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

    public CataloguePage listForRefresh(int page, int size) {
        return restClient.get()
                .uri(ub -> ub.path("/internal/projects/refresh")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .body(CataloguePage.class);
    }

    public CatalogueImportResponse importProjects(List<DiscoveryProject> projects) {
        if (projects.isEmpty()) {
            return new CatalogueImportResponse(0, 0, 0, 0, "No projects to import");
        }
        List<CatalogueImportItem> items = projects.stream().map(this::toImportItem).toList();
        try {
            return restClient.post()
                    .uri("/internal/projects/import")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CatalogueImportRequest(items))
                    .retrieve()
                    .body(CatalogueImportResponse.class);
        } catch (RestClientException e) {
            log.error("Catalogue import request failed: {}", e.getMessage());
            throw e;
        }
    }

    private CatalogueImportItem toImportItem(DiscoveryProject p) {
        return new CatalogueImportItem(
                p.name(), p.description(), p.githubUrl(), p.owner(),
                p.language(), p.license(), p.stars(), p.forks(),
                p.topics(), p.lastUpdated(), p.openIssues()
        );
    }
}
