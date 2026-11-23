package org.ossatlas.discovery_service.refresh;

public record DiscoveryQuery(
        String query,
        String language,
        Integer minStars,
        String license,
        String topic,
        Integer limit
) {}
