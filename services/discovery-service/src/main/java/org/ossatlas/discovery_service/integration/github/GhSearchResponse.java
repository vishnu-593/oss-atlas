package org.ossatlas.discovery_service.integration.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GhSearchResponse(
        @JsonProperty("total_count") int totalCount,
        List<GhRepo> items
) {}
