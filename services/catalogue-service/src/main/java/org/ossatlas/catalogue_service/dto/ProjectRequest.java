
package org.ossatlas.catalogue_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record ProjectRequest(

        @NotBlank
        @Size(max = 255)
        String name,

        String description,

        @NotBlank
        @Size(max = 500)
        String githubUrl,

        @NotBlank
        @Size(max = 255)
        String owner,

        String language,

        String license,

        @NotNull
        @Min(0)
        Integer stars,

        @NotNull
        @Min(0)
        Integer forks,

        List<String> topics,

        Instant lastUpdated,

        @Min(0)
        Integer openIssues

) {}
