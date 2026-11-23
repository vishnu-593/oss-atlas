package org.ossatlas.discovery_service.refresh;

import java.time.Instant;

public record RefreshSummary(
        Instant startTime,
        Instant endTime,
        int projectsExamined,
        int projectsImported,
        int projectsUpdated,
        int projectsSkipped,
        int failures
) {}
