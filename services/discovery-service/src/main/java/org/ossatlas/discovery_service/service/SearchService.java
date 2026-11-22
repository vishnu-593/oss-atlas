package org.ossatlas.discovery_service.service;

import org.ossatlas.discovery_service.dto.SearchRequest;
import org.ossatlas.discovery_service.dto.SearchResponse;

public interface SearchService {
    SearchResponse search(SearchRequest request);
}
