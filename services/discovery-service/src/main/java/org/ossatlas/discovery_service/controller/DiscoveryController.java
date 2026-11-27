package org.ossatlas.discovery_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ossatlas.discovery_service.client.CatalogueClient;
import org.ossatlas.discovery_service.dto.ImportRequest;
import org.ossatlas.discovery_service.dto.SearchRequest;
import org.ossatlas.discovery_service.dto.SearchResponse;
import org.ossatlas.discovery_service.integration.catalogue.CatalogueImportResponse;
import org.ossatlas.discovery_service.refresh.RefreshJob;
import org.ossatlas.discovery_service.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:4173"})
@RequiredArgsConstructor
public class DiscoveryController {

    private final SearchService searchService;
    private final CatalogueClient catalogueClient;
    private final RefreshJob refreshJob;

    @PostMapping("/api/discovery/search")
    public SearchResponse search(@RequestBody @Valid SearchRequest request) {
        return searchService.search(request);
    }

    @PostMapping("/api/discovery/import")
    public CatalogueImportResponse importProjects(@RequestBody @Valid ImportRequest request) {
        return catalogueClient.importProjects(request.projects());
    }

    @PostMapping("/api/discovery/admin/refresh")
    public ResponseEntity<Map<String, String>> triggerRefresh() {
        new Thread(refreshJob::run).start();
        return ResponseEntity.accepted().body(Map.of("status", "refresh started"));
    }
}
