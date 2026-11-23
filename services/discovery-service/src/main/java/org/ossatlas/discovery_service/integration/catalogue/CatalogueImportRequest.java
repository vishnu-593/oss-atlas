package org.ossatlas.discovery_service.integration.catalogue;

import java.util.List;

public record CatalogueImportRequest(List<CatalogueImportItem> projects) {}
