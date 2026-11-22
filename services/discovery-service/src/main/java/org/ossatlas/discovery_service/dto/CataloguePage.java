package org.ossatlas.discovery_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CataloguePage(List<CatalogueProjectSummary> content) {}
