
package org.ossatlas.catalogue_service.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        String githubUrl,
        String owner,
        String language,
        String license,
        Integer stars,
        Integer forks,
        List<String> topics,
        Instant lastUpdated,
        Instant createdAt,
        Instant updatedAt,
        Integer openIssues,
        Integer healthScore,
        String healthLabel
) {}
