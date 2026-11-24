
package org.ossatlas.catalogue_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ImportProjectsRequest(

        @NotEmpty
        List<@Valid ProjectRequest> projects

) {}
