package org.ossatlas.discovery_service.integration.catalogue;

import java.time.Instant;
import java.util.List;

public record CatalogueImportItem(
        String name,
        String description,
        String githubUrl,
        String owner,
        String language,
        String license,
        Integer stars,
        Integer forks,
        List<String> topics,
        Instant lastUpdated
) {}
