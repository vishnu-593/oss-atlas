package org.ossatlas.discovery_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubRepoSummary(
        Long id,
        String name,
        @JsonProperty("full_name")        String fullName,
        String description,
        @JsonProperty("html_url")         String htmlUrl,
        String language,
        @JsonProperty("stargazers_count") int stars,
        GithubLicense license
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GithubLicense(String key, String name) {}
}
