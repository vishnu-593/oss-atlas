package org.ossatlas.discovery_service.integration.catalogue;

public record CatalogueImportResponse(
        int received,
        int imported,
        int updated,
        int duplicates,
        String message
) {}
