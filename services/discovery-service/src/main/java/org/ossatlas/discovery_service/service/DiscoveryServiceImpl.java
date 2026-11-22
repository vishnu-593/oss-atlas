package org.ossatlas.discovery_service.service;

import lombok.RequiredArgsConstructor;
import org.ossatlas.discovery_service.client.CatalogueClient;
import org.ossatlas.discovery_service.client.SourceIntegrationClient;
import org.ossatlas.discovery_service.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscoveryServiceImpl implements DiscoveryService {

    private final SourceIntegrationClient sourceClient;
    private final CatalogueClient catalogueClient;

    @Override
    public DiscoveryResult discover(String query, int page, int size) {
        GithubSearchResponse ghResults = sourceClient.search(query, page, size);

        // Fetch all indexed projects to build a URL → catalogueId lookup
        CataloguePage cataloguePage = catalogueClient.listAll(1000);
        Map<String, Long> indexedByUrl = cataloguePage.content().stream()
                .filter(p -> p.repositoryUrl() != null && !p.repositoryUrl().isBlank())
                .collect(Collectors.toMap(
                        p -> normalize(p.repositoryUrl()),
                        CatalogueProjectSummary::id,
                        (a, b) -> a
                ));

        List<DiscoveredProject> results = ghResults.items().stream()
                .map(repo -> {
                    Long catalogueId = indexedByUrl.get(normalize(repo.htmlUrl()));
                    return new DiscoveredProject(
                            repo.id(),
                            repo.fullName(),
                            repo.description(),
                            repo.htmlUrl(),
                            repo.language(),
                            repo.stars(),
                            repo.license() != null ? repo.license().name() : null,
                            catalogueId != null ? ProjectStatus.INDEXED : ProjectStatus.NEW,
                            catalogueId
                    );
                })
                .toList();

        return new DiscoveryResult(query, ghResults.totalCount(), page, size, results);
    }

    private static String normalize(String url) {
        return url.toLowerCase().replaceAll("/+$", "");
    }
}
