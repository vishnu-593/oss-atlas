package org.ossatlas.discovery_service.refresh;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ossatlas.discovery_service.client.CatalogueClient;
import org.ossatlas.discovery_service.dto.CataloguePage;
import org.ossatlas.discovery_service.dto.CatalogueProjectSummary;
import org.ossatlas.discovery_service.dto.DiscoveryProject;
import org.ossatlas.discovery_service.integration.catalogue.CatalogueImportResponse;
import org.ossatlas.discovery_service.integration.github.GhRepo;
import org.ossatlas.discovery_service.integration.github.GhSearchResponse;
import org.ossatlas.discovery_service.integration.github.GitHubApiClient;
import org.ossatlas.discovery_service.service.SearchServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshJob {

    private static final int MAX_GITHUB_PAGE = 10; // GitHub caps search results at 1000 (10 × 100)

    private final RefreshProperties props;
    private final CatalogueClient catalogueClient;
    private final GitHubApiClient gitHubApiClient;
    private final SearchServiceImpl searchService;
    private final RefreshCursorRepository cursorRepository;

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(cron = "${oss-atlas.refresh.cron:0 0 23 * * *}",
               zone  = "${oss-atlas.refresh.zone:Asia/Kolkata}")
    public void run() {
        if (!props.isEnabled()) {
            log.info("Discovery refresh is disabled — skipping scheduled run");
            return;
        }
        if (!running.compareAndSet(false, true)) {
            log.warn("Refresh job skipped — previous run still in progress");
            return;
        }
        Instant start = Instant.now();
        int examined = 0, imported = 0, updated = 0, skipped = 0, failures = 0;
        try {
            log.info("Discovery refresh started");

            // Phase 1 — Refresh existing catalogue projects with latest GitHub metadata
            int page = 0;
            while (true) {
                CataloguePage cataloguePage = catalogueClient.listForRefresh(page, props.getPageSize());
                List<CatalogueProjectSummary> projects = cataloguePage.content();
                if (projects == null || projects.isEmpty()) break;

                List<DiscoveryProject> refreshed = new ArrayList<>();
                for (CatalogueProjectSummary summary : projects) {
                    examined++;
                    if (summary.githubUrl() == null || !summary.githubUrl().contains("github.com")) {
                        skipped++;
                        continue;
                    }
                    try {
                        String[] ownerRepo = extractOwnerRepo(summary.githubUrl());
                        GhRepo ghRepo = gitHubApiClient.getRepo(ownerRepo[0], ownerRepo[1]);
                        refreshed.add(searchService.toProject(ghRepo));
                    } catch (RestClientResponseException e) {
                        if (e.getStatusCode().value() == 403) {
                            log.warn("GitHub rate limit hit during refresh — stopping batch. Examined so far: {}", examined);
                            failures++;
                            break;
                        }
                        log.warn("Failed to fetch GitHub metadata for {}: {}", summary.githubUrl(), e.getMessage());
                        failures++;
                    } catch (Exception e) {
                        log.warn("Unexpected error refreshing {}: {}", summary.githubUrl(), e.getMessage());
                        failures++;
                    }
                }

                if (!refreshed.isEmpty()) {
                    try {
                        CatalogueImportResponse resp = catalogueClient.importProjects(refreshed);
                        imported += resp.imported();
                        updated  += resp.updated();
                    } catch (RestClientException e) {
                        log.error("Catalogue import failed during refresh: {}", e.getMessage());
                        failures += refreshed.size();
                    }
                }

                if (projects.size() < props.getPageSize()) break;
                page++;
            }

            // Phase 2 — Discover new projects using paginated category queries
            for (DiscoveryQuery dq : props.getQueries()) {
                int limit = dq.limit() != null ? Math.min(dq.limit(), 100) : 10;
                String ghQuery = buildQuery(dq);

                // Read cursor from DB — which page to fetch next for this query
                int currentPage = cursorRepository.findById(ghQuery)
                        .map(RefreshCursor::getLastPage)
                        .orElse(1);

                try {
                    GhSearchResponse ghResp = gitHubApiClient.search(ghQuery, limit, currentPage);
                    List<DiscoveryProject> discovered = searchService.deduplicate(
                            ghResp.items().stream().map(searchService::toProject).toList());

                    if (!discovered.isEmpty()) {
                        CatalogueImportResponse resp = catalogueClient.importProjects(discovered);
                        imported += resp.imported();
                        updated  += resp.updated();
                        examined += discovered.size();
                    }

                    // Advance cursor: reset to 1 when results run out or max page reached
                    int nextPage = (ghResp.items().size() < limit || currentPage >= MAX_GITHUB_PAGE)
                            ? 1 : currentPage + 1;
                    saveCursor(ghQuery, nextPage);
                    log.info("Query '{}' — fetched page {}, next page will be {}", dq.query(), currentPage, nextPage);

                } catch (RestClientResponseException e) {
                    if (e.getStatusCode().value() == 403) {
                        log.warn("GitHub rate limit hit during discovery query '{}'", dq.query());
                    } else {
                        log.warn("Discovery query '{}' failed: {}", dq.query(), e.getMessage());
                    }
                    failures++;
                } catch (RestClientException e) {
                    log.warn("Discovery query '{}' failed: {}", dq.query(), e.getMessage());
                    failures++;
                }
            }

        } finally {
            running.set(false);
            RefreshSummary summary = new RefreshSummary(
                    start, Instant.now(), examined, imported, updated, skipped, failures);
            log.info("Discovery refresh complete — examined={} imported={} updated={} skipped={} failures={}",
                    summary.projectsExamined(), summary.projectsImported(), summary.projectsUpdated(),
                    summary.projectsSkipped(), summary.failures());
        }
    }

    private void saveCursor(String queryName, int nextPage) {
        RefreshCursor cursor = cursorRepository.findById(queryName).orElse(new RefreshCursor());
        cursor.setQueryName(queryName);
        cursor.setLastPage(nextPage);
        cursor.setUpdatedAt(Instant.now());
        cursorRepository.save(cursor);
    }

    private static String[] extractOwnerRepo(String repositoryUrl) {
        String path = URI.create(repositoryUrl).getPath();
        String[] parts = path.split("/");
        return new String[]{parts[1], parts[2]};
    }

    private static String buildQuery(DiscoveryQuery dq) {
        StringBuilder sb = new StringBuilder(dq.query().trim());
        if (hasText(dq.language()))   sb.append(" language:").append(dq.language());
        if (dq.minStars() != null)    sb.append(" stars:>=").append(dq.minStars());
        if (hasText(dq.license()))    sb.append(" license:").append(dq.license().toLowerCase());
        if (hasText(dq.topic()))      sb.append(" topic:").append(dq.topic());
        return sb.toString();
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
