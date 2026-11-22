package org.ossatlas.discovery_service.service;

import org.ossatlas.discovery_service.dto.DiscoveryResult;

public interface DiscoveryService {
    DiscoveryResult discover(String query, int page, int size);
}
