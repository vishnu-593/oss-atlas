package org.ossatlas.discovery_service.integration.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GhLicense(
        @JsonProperty("spdx_id") String spdxId,
        String name
) {}
