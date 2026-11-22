package org.ossatlas.discovery_service.dto;

public record DiscoveredProject(
        Long githubId,
        String fullName,
        String description,
        String htmlUrl,
        String language,
        int stars,
        String license,
        ProjectStatus status,
        Long catalogueId
) {}
