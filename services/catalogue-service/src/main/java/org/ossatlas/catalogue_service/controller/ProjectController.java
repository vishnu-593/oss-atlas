
package org.ossatlas.catalogue_service.controller;

import jakarta.validation.Valid;
import org.ossatlas.catalogue_service.dto.ProjectRequest;
import org.ossatlas.catalogue_service.dto.ProjectResponse;
import org.ossatlas.catalogue_service.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import org.ossatlas.catalogue_service.dto.ImportProjectsRequest;
import org.ossatlas.catalogue_service.dto.ImportProjectsResponse;

import org.springframework.data.domain.Page;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:4173"})
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // Create project
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(
            @Valid @RequestBody ProjectRequest request) {

        return projectService.createProject(request);
    }

    // Get all projects
    
@GetMapping
public Page<ProjectResponse> getAllProjects(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String direction
) {
    return projectService.getAllProjects(
            page, size, sortBy, direction
    );
}


    // Get project by ID
    @GetMapping("/{id}")
    public ProjectResponse getProjectById(@PathVariable UUID id) {

        return projectService.getProjectById(id);
    }

    // Update project
    @PutMapping("/{id}")
    public ProjectResponse updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectRequest request) {

        return projectService.updateProject(id, request);
    }

    // Delete project
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable UUID id) {

        projectService.deleteProject(id);
    }



@GetMapping("/search")
public Page<ProjectResponse> searchProjects(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String language,
        @RequestParam(required = false) String license,
        @RequestParam(required = false) String topic,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "stars") String sortBy,
        @RequestParam(defaultValue = "desc") String direction) {

    return projectService.searchProjects(
            keyword,
            language,
            license,
            topic,
            page,
            size,
            sortBy,
            direction
    );
}

}
