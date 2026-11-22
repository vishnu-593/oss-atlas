package org.ossatlas.discovery_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ossatlas.discovery_service.dto.DiscoveryResult;
import org.ossatlas.discovery_service.dto.SearchRequest;
import org.ossatlas.discovery_service.dto.SearchResponse;
import org.ossatlas.discovery_service.service.DiscoveryService;
import org.ossatlas.discovery_service.service.SearchService;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:4173"})
@RequiredArgsConstructor
public class DiscoveryController {

    private final DiscoveryService discoveryService;
    private final SearchService searchService;

    @GetMapping("/api/discover")
    public DiscoveryResult discover(
            @RequestParam String q,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return discoveryService.discover(q, page, size);
    }

    @PostMapping("/api/discovery/search")
    public SearchResponse search(@RequestBody @Valid SearchRequest request) {
        return searchService.search(request);
    }
}
