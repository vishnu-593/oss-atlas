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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
        int openIssues = repo.openIssues();
        int score = healthScore(repo.updatedAt(), repo.stars(), repo.description(),
                repo.license() != null ? repo.license().spdxId() : null,
                repo.topics(), openIssues);
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
                repo.updatedAt(),
                openIssues,
                score,
                healthLabel(score)
        );
    }

    static int healthScore(Instant updatedAt, int stars, String description,
                           String license, List<String> topics, int openIssues) {
        return recency(updatedAt) + community(stars)
                + documentation(description, license, topics) + issueHealth(openIssues);
    }

    static String healthLabel(int score) {
        if (score >= 85) return "Healthy";
        if (score >= 65) return "Active";
        if (score >= 45) return "Moderate";
        if (score >= 20) return "Low Activity";
        return "Inactive";
    }

    private static int recency(Instant updatedAt) {
        if (updatedAt == null) return 5;
        long days = ChronoUnit.DAYS.between(updatedAt, Instant.now());
        if (days < 30)  return 40;
        if (days < 90)  return 30;
        if (days < 180) return 20;
        if (days < 365) return 10;
        return 0;
    }

    private static int community(int stars) {
        if (stars >= 50_000) return 25;
        if (stars >= 10_000) return 20;
        if (stars >= 1_000)  return 15;
        if (stars >= 500)    return 10;
        if (stars >= 100)    return 6;
        if (stars >= 10)     return 3;
        return 0;
    }

    private static int documentation(String description, String license, List<String> topics) {
        int pts = 0;
        if (description != null && !description.isBlank()) pts += 6;
        if (license     != null && !license.isBlank())     pts += 5;
        if (topics      != null && !topics.isEmpty())      pts += 4;
        return pts;
    }

    private static int issueHealth(int openIssues) {
        if (openIssues == 0)    return 20;
        if (openIssues < 5)     return 16;
        if (openIssues < 20)    return 12;
        if (openIssues < 50)    return 7;
        if (openIssues < 200)   return 3;
        if (openIssues < 1000)  return 1;
        return 0;
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
