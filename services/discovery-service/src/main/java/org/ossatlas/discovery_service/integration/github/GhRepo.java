package org.ossatlas.discovery_service.integration.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GhRepo(
        String name,
        String description,
        @JsonProperty("html_url")         String htmlUrl,
        GhOwner owner,
        String language,
        @JsonProperty("stargazers_count") int stars,
        @JsonProperty("forks_count")      int forks,
        List<String> topics,
        @JsonProperty("updated_at")       Instant updatedAt,
        GhLicense license,
        @JsonProperty("open_issues_count") int openIssues
) {}
