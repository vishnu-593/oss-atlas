package org.ossatlas.discovery_service.dto;

import java.util.List;

public record DiscoveryResult(
        String query,
        int totalGithubResults,
        int page,
        int size,
        List<DiscoveredProject> results
) {}
