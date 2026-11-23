package org.ossatlas.discovery_service.dto;

public record ImportResult(
        int received,
        int imported,
        int updated,
        int duplicates,
        String message,
        String error
) {}
