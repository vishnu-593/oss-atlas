package org.ossatlas.discovery_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ossatlas.discovery_service.client.CatalogueClient;
import org.ossatlas.discovery_service.dto.ImportRequest;
import org.ossatlas.discovery_service.dto.SearchRequest;
import org.ossatlas.discovery_service.dto.SearchResponse;
import org.ossatlas.discovery_service.integration.catalogue.CatalogueImportResponse;
import org.ossatlas.discovery_service.service.SearchService;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:4173"})
@RequiredArgsConstructor
public class DiscoveryController {

    private final SearchService searchService;
    private final CatalogueClient catalogueClient;

    @PostMapping("/api/discovery/search")
    public SearchResponse search(@RequestBody @Valid SearchRequest request) {
        return searchService.search(request);
    }

    @PostMapping("/api/discovery/import")
    public CatalogueImportResponse importProjects(@RequestBody @Valid ImportRequest request) {
        return catalogueClient.importProjects(request.projects());
    }
}
