package org.ossatlas.discovery_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ossatlas.discovery_service.dto.DiscoveryProject;
import org.ossatlas.discovery_service.dto.SearchRequest;
import org.ossatlas.discovery_service.dto.SearchResponse;
import org.ossatlas.discovery_service.integration.github.GhRepo;
import org.ossatlas.discovery_service.integration.github.GhSearchResponse;
import org.ossatlas.discovery_service.integration.github.GitHubApiClient;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    static final int DEFAULT_LIMIT = 10;
    static final int MAX_LIMIT = 100;

    private final GitHubApiClient gitHubApiClient;

    @Override
    public SearchResponse search(SearchRequest request) {
        int limit = request.limit() != null
                ? Math.min(request.limit(), MAX_LIMIT)
                : DEFAULT_LIMIT;

        String githubQuery = buildQuery(request);
        GhSearchResponse ghResponse = gitHubApiClient.search(githubQuery, limit, 1);

        List<DiscoveryProject> projects = deduplicate(
                ghResponse.items().stream().map(this::toProject).toList()
        );

        return new SearchResponse(request.query(), projects.size(), projects);
    }

    String buildQuery(SearchRequest request) {
        StringBuilder sb = new StringBuilder(request.query().trim());
        if (hasText(request.language())) sb.append(" language:").append(request.language());
        if (request.minStars() != null)  sb.append(" stars:>=").append(request.minStars());
        if (hasText(request.license()))  sb.append(" license:").append(request.license().toLowerCase(Locale.ROOT));
        if (hasText(request.topic()))    sb.append(" topic:").append(request.topic());
        return sb.toString();
    }

    public DiscoveryProject toProject(GhRepo repo) {
        return new DiscoveryProject(
                repo.name(),
                repo.description(),
                repo.htmlUrl(),
                repo.owner() != null ? repo.owner().login() : null,
                repo.language(),
                repo.license() != null ? repo.license().spdxId() : null,
                repo.stars(),
                repo.forks(),
                repo.topics() != null ? repo.topics() : List.of(),
                repo.updatedAt()
        );
    }

    public List<DiscoveryProject> deduplicate(List<DiscoveryProject> projects) {
        return projects.stream()
                .collect(Collectors.toMap(
                        p -> normalize(p.githubUrl()),
                        p -> p,
                        (a, b) -> a,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
    }

    static String normalize(String url) {
        if (url == null) return "";
        return url.toLowerCase(Locale.ROOT).replaceAll("/+$", "");
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
