package org.ossatlas.discovery_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogueProjectSummary(Long id, String repositoryUrl) {}
