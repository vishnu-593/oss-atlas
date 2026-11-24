
package org.ossatlas.catalogue_service.dto;

public record ImportProjectsResponse(
        int received,
        int imported,
        int updated,
        int duplicates,
        String message
) {}
