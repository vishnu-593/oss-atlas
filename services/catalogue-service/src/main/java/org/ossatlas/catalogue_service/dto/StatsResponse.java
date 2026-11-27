package org.ossatlas.catalogue_service.dto;

import java.util.List;

public record StatsResponse(
        long totalProjects,
        List<NameCount> topLanguages,
        List<NameCount> licenseDistribution,
        List<NameCount> healthDistribution
) {
    public record NameCount(String name, long count) {}
}
