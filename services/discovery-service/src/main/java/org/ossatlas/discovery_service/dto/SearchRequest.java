package org.ossatlas.discovery_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SearchRequest(
        @NotBlank(message = "query is required") String query,
        String language,
        @Min(0) Integer minStars,
        String license,
        String topic,
        @Min(1) @Max(50) Integer limit
) {}
