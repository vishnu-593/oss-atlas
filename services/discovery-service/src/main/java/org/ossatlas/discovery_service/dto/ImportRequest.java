package org.ossatlas.discovery_service.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ImportRequest(
        @NotEmpty List<DiscoveryProject> projects
) {}
