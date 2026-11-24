
package org.ossatlas.catalogue_service.controller;

import jakarta.validation.Valid;
import org.ossatlas.catalogue_service.dto.ImportProjectsRequest;
import org.ossatlas.catalogue_service.dto.ImportProjectsResponse;
import org.ossatlas.catalogue_service.dto.ProjectResponse;
import org.ossatlas.catalogue_service.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/projects")
public class ProjectImportController {

    private final ProjectService projectService;

    public ProjectImportController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/import")
    public ImportProjectsResponse importProjects(
            @Valid @RequestBody ImportProjectsRequest request) {

        return projectService.importProjects(request);
    }

    @GetMapping("/refresh")
    public Page<ProjectResponse> getProjectsForRefresh(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        return projectService.getAllProjects(
                page, size, "updatedAt", "asc"
        );
    }
}
