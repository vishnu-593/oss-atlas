package org.ossatlas.discovery_service.dto;

import java.util.List;

public record SearchResponse(
        String query,
        int totalFound,
        List<DiscoveryProject> projects,
        ImportResult importResult
) {}
