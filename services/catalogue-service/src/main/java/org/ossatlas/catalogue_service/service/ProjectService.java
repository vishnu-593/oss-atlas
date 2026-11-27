
package org.ossatlas.catalogue_service.service;

import org.ossatlas.catalogue_service.dto.ImportProjectsRequest;
import org.ossatlas.catalogue_service.dto.ImportProjectsResponse;
import org.ossatlas.catalogue_service.dto.ProjectRequest;
import org.ossatlas.catalogue_service.dto.ProjectResponse;
import org.ossatlas.catalogue_service.dto.StatsResponse;
import org.ossatlas.catalogue_service.entity.Project;
import org.ossatlas.catalogue_service.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // Create a project
    public ProjectResponse createProject(ProjectRequest request) {
        String githubUrl = request.githubUrl().trim().replaceAll("/+$", "");

        if (projectRepository.existsByGithubUrl(githubUrl)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Project with this GitHub URL already exists"
            );
        }

        Project project = new Project();
        updateProjectFields(project, normalizeRequest(request, githubUrl));

        Project savedProject = projectRepository.save(project);

        return mapToResponse(savedProject);
    }

    // Get project by ID
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID id) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Project not found with ID: " + id
                ));

        return mapToResponse(project);
    }

    // Update project
    public ProjectResponse updateProject(UUID id, ProjectRequest request) {
        String githubUrl = request.githubUrl().trim().replaceAll("/+$", "");

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Project not found with ID: " + id
                ));

        projectRepository.findByGithubUrl(githubUrl)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Another project already uses this GitHub URL"
                    );
                });

        updateProjectFields(project, normalizeRequest(request, githubUrl));

        return mapToResponse(projectRepository.save(project));
    }

    // Delete project
    public void deleteProject(UUID id) {

        if (!projectRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Project not found with ID: " + id
            );
        }

        projectRepository.deleteById(id);
    }

    // Map request fields to entity
    private void updateProjectFields(Project project, ProjectRequest request) {

        project.setName(request.name());
        project.setDescription(request.description());
        project.setGithubUrl(request.githubUrl());
        project.setOwner(request.owner());
        project.setLanguage(request.language());
        project.setLicense(request.license());
        project.setStars(request.stars());
        project.setForks(request.forks());
        project.setTopics(
                request.topics() == null ? List.of() : request.topics()
        );
        project.setLastUpdated(request.lastUpdated());
    }

    private ProjectRequest normalizeRequest(ProjectRequest request, String normalizedUrl) {
        return new ProjectRequest(
                request.name(), request.description(), normalizedUrl,
                request.owner(), request.language(), request.license(),
                request.stars(), request.forks(), request.topics(), request.lastUpdated()
        );
    }

    // Map entity to response DTO
    private ProjectResponse mapToResponse(Project project) {

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getGithubUrl(),
                project.getOwner(),
                project.getLanguage(),
                project.getLicense(),
                project.getStars(),
                project.getForks(),
                project.getTopics(),
                project.getLastUpdated(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    public ImportProjectsResponse importProjects(ImportProjectsRequest request) {

    int received = request.projects().size();
    int imported = 0;
    int updated = 0;
    int duplicates = 0;

    Set<String> processedUrls = new HashSet<>();

    for (ProjectRequest projectRequest : request.projects()) {

        String githubUrl = projectRequest.githubUrl()
                .trim()
                .replaceAll("/+$", "");

        if (!processedUrls.add(githubUrl)) {
            duplicates++;
            continue;
        }

        Project project = projectRepository
                .findByGithubUrl(githubUrl)
                .orElse(null);

        if (project == null) {
            project = new Project();
            imported++;
        } else {
            updated++;
        }

        ProjectRequest normalizedRequest = new ProjectRequest(
                projectRequest.name(),
                projectRequest.description(),
                githubUrl,
                projectRequest.owner(),
                projectRequest.language(),
                projectRequest.license(),
                projectRequest.stars(),
                projectRequest.forks(),
                projectRequest.topics(),
                projectRequest.lastUpdated()
        );

        updateProjectFields(project, normalizedRequest);
        projectRepository.save(project);
    }

    return new ImportProjectsResponse(
            received,
            imported,
            updated,
            duplicates,
            "Project import completed"
    );
}

@Transactional(readOnly = true)
public Page<ProjectResponse> getAllProjects(
        int page,
        int size,
        String sortBy,
        String direction
) {
    Pageable pageable = createPageable(
        page, size, sortBy, direction
);

    return projectRepository.findAll(pageable)
            .map(this::mapToResponse);
}


@Transactional(readOnly = true)
public Page<ProjectResponse> searchProjects(
        String keyword,
        String language,
        String license,
        String topic,
        int page,
        int size,
        String sortBy,
        String direction) {

    Pageable pageable = createPageable(page, size, sortBy, direction);

    return projectRepository.searchProjects(
            normalize(keyword),
            normalize(language),
            normalize(license),
            normalize(topic),
            pageable
    ).map(this::mapToResponse);
}


private String normalize(String value) {
    return value == null || value.isBlank() ? null : value.trim();
}

@Transactional(readOnly = true)
public StatsResponse getStats() {
    long total = projectRepository.count();

    List<StatsResponse.NameCount> languages = projectRepository.countByLanguage()
            .stream()
            .map(r -> new StatsResponse.NameCount((String) r[0], ((Number) r[1]).longValue()))
            .toList();

    List<StatsResponse.NameCount> licenses = projectRepository.countByLicense()
            .stream()
            .map(r -> new StatsResponse.NameCount((String) r[0], ((Number) r[1]).longValue()))
            .toList();

    List<StatsResponse.NameCount> health = projectRepository.countByHealth()
            .stream()
            .map(r -> new StatsResponse.NameCount((String) r[0], ((Number) r[1]).longValue()))
            .sorted(Comparator.comparingInt(nc -> healthOrder(nc.name())))
            .toList();

    return new StatsResponse(total, languages, licenses, health);
}

private int healthOrder(String name) {
    return switch (name) {
        case "Active"   -> 0;
        case "Healthy"  -> 1;
        case "Slow"     -> 2;
        default         -> 3;
    };
}


private Pageable createPageable(
        int page,
        int size,
        String sortBy,
        String direction
) {
    if (page < 0) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Page number cannot be negative"
        );
    }

    if (size < 1 || size > 100) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Page size must be between 1 and 100"
        );
    }

    Set<String> allowedSortFields = Set.of(
            "name", "stars", "forks", "createdAt", "updatedAt"
    );

    if (!allowedSortFields.contains(sortBy)) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid sort field: " + sortBy
        );
    }

    Sort.Direction sortDirection;

    try {
        sortDirection = Sort.Direction.fromString(direction);
    } catch (IllegalArgumentException exception) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Direction must be asc or desc"
        );
    }

    return PageRequest.of(
            page,
            size,
            Sort.by(sortDirection, sortBy)
    );
}

}
