package org.ossatlas.discovery_service.dto;

import java.time.Instant;
import java.util.List;

public record DiscoveryProject(
        String name,
        String description,
        String githubUrl,
        String owner,
        String language,
        String license,
        int stars,
        int forks,
        List<String> topics,
        Instant lastUpdated
) {}
