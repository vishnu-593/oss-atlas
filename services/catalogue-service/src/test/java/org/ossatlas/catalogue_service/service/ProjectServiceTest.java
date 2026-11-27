
package org.ossatlas.catalogue_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ossatlas.catalogue_service.dto.ProjectRequest;
import org.ossatlas.catalogue_service.dto.ProjectResponse;
import org.ossatlas.catalogue_service.entity.Project;
import org.ossatlas.catalogue_service.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.ossatlas.catalogue_service.dto.ImportProjectsRequest;
import org.ossatlas.catalogue_service.dto.ImportProjectsResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.*;

class ProjectServiceTest {

    private ProjectRepository projectRepository;
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectRepository = mock(ProjectRepository.class);
        projectService = new ProjectService(projectRepository);
    }

    private ProjectRequest createRequest() {
        return new ProjectRequest(
                "Spring Boot",
                "Framework for Java applications",
                "https://github.com/spring-projects/spring-boot",
                "spring-projects",
                "Java",
                "Apache-2.0",
                75000,
                40000,
                List.of("java", "spring", "backend"),
                Instant.parse("2026-11-20T10:00:00Z"),
                null
        );
    }

    @Test
    void createProjectSuccessfully() {
        ProjectRequest request = createRequest();

        when(projectRepository.existsByGithubUrl(request.githubUrl()))
                .thenReturn(false);

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> {
                    Project project = invocation.getArgument(0);
                    project.setId(UUID.randomUUID());
                    return project;
                });

        ProjectResponse response = projectService.createProject(request);

        assertNotNull(response);
        assertEquals("Spring Boot", response.name());
        assertEquals(request.githubUrl(), response.githubUrl());
        assertEquals("Java", response.language());

        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void createProjectRejectsDuplicateGithubUrl() {
        ProjectRequest request = createRequest();

        when(projectRepository.existsByGithubUrl(request.githubUrl()))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> projectService.createProject(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void getProjectByIdSuccessfully() {
        UUID id = UUID.randomUUID();

        Project project = new Project();
        project.setId(id);
        project.setName("Spring Boot");
        project.setDescription("Java framework");
        project.setGithubUrl("https://github.com/spring-projects/spring-boot");
        project.setOwner("spring-projects");
        project.setLanguage("Java");
        project.setLicense("Apache-2.0");
        project.setStars(75000);
        project.setForks(40000);
        project.setTopics(List.of("java", "spring"));

        when(projectRepository.findById(id))
                .thenReturn(Optional.of(project));

        ProjectResponse response = projectService.getProjectById(id);

        assertEquals(id, response.id());
        assertEquals("Spring Boot", response.name());
    }

    @Test
    void getProjectByIdThrowsNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();

        when(projectRepository.findById(id))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> projectService.getProjectById(id)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    private Project createProject(UUID id) {
    Project project = new Project();

    project.setId(id);
    project.setName("Spring Boot");
    project.setDescription("Framework for Java applications");
    project.setGithubUrl(
            "https://github.com/spring-projects/spring-boot"
    );
    project.setOwner("spring-projects");
    project.setLanguage("Java");
    project.setLicense("Apache-2.0");
    project.setStars(75000);
    project.setForks(40000);
    project.setTopics(List.of("java", "spring", "backend"));

    return project;
}

    @Test
    void updateProjectSuccessfully() {
    UUID id = UUID.randomUUID();
    Project project = createProject(id);
    ProjectRequest request = createRequest();

    when(projectRepository.findById(id))
            .thenReturn(Optional.of(project));

    when(projectRepository.findByGithubUrl(request.githubUrl()))
            .thenReturn(Optional.of(project));

    when(projectRepository.save(any(Project.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    ProjectResponse response = projectService.updateProject(id, request);

    assertEquals(id, response.id());
    assertEquals("Spring Boot", response.name());
    verify(projectRepository).save(project);
}

@Test
void updateProjectThrowsNotFoundWhenMissing() {
    UUID id = UUID.randomUUID();

    when(projectRepository.findById(id))
            .thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectService.updateProject(id, createRequest())
    );

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    verify(projectRepository, never()).save(any(Project.class));
}

@Test
void updateProjectRejectsDuplicateGithubUrl() {
    UUID id = UUID.randomUUID();
    UUID anotherId = UUID.randomUUID();

    Project project = createProject(id);
    Project anotherProject = createProject(anotherId);

    ProjectRequest request = new ProjectRequest(
            "Another project",
            "Description",
            anotherProject.getGithubUrl(),
            "another-owner",
            "Java",
            "MIT",
            100,
            10,
            List.of("java"),
            Instant.parse("2026-11-20T10:00:00Z"),
            null
    );

    when(projectRepository.findById(id))
            .thenReturn(Optional.of(project));

    when(projectRepository.findByGithubUrl(request.githubUrl()))
            .thenReturn(Optional.of(anotherProject));

    ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectService.updateProject(id, request)
    );

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    verify(projectRepository, never()).save(any(Project.class));
}

@Test
void deleteProjectSuccessfully() {
    UUID id = UUID.randomUUID();

    when(projectRepository.existsById(id)).thenReturn(true);

    assertDoesNotThrow(() -> projectService.deleteProject(id));

    verify(projectRepository).deleteById(id);
}

@Test
void deleteProjectThrowsNotFoundWhenMissing() {
    UUID id = UUID.randomUUID();

    when(projectRepository.existsById(id)).thenReturn(false);

    ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectService.deleteProject(id)
    );

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    verify(projectRepository, never()).deleteById(id);
}

@Test
void importProjectsSuccessfully() {
    ProjectRequest request = createRequest();

    when(projectRepository.findByGithubUrl(request.githubUrl()))
            .thenReturn(Optional.empty());

    when(projectRepository.save(any(Project.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    ImportProjectsResponse response = projectService.importProjects(
            new ImportProjectsRequest(List.of(request))
    );

    assertEquals(1, response.received());
    assertEquals(1, response.imported());
    assertEquals(0, response.updated());
    assertEquals(0, response.duplicates());

    verify(projectRepository).save(any(Project.class));
}

@Test
void importProjectsUpdatesExistingProject() {
    ProjectRequest request = createRequest();
    Project existingProject = createProject(UUID.randomUUID());

    when(projectRepository.findByGithubUrl(request.githubUrl()))
            .thenReturn(Optional.of(existingProject));

    when(projectRepository.save(any(Project.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    ImportProjectsResponse response = projectService.importProjects(
            new ImportProjectsRequest(List.of(request))
    );

    assertEquals(1, response.received());
    assertEquals(0, response.imported());
    assertEquals(1, response.updated());
    assertEquals(0, response.duplicates());

    verify(projectRepository).save(existingProject);
}

@Test
void importProjectsCountsDuplicateUrlsWithinRequest() {
    ProjectRequest request = createRequest();

    when(projectRepository.findByGithubUrl(request.githubUrl()))
            .thenReturn(Optional.empty());

    when(projectRepository.save(any(Project.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    ImportProjectsResponse response = projectService.importProjects(
            new ImportProjectsRequest(List.of(request, request))
    );

    assertEquals(2, response.received());
    assertEquals(1, response.imported());
    assertEquals(0, response.updated());
    assertEquals(1, response.duplicates());

    verify(projectRepository, times(1)).save(any(Project.class));
}

@Test
void getAllProjectsWithPaginationSuccessfully() {
    Project project = createProject(UUID.randomUUID());

    Pageable pageable = PageRequest.of(0, 10);

    Page<Project> projectPage = new PageImpl<>(
            List.of(project),
            pageable,
            1
    );

    when(projectRepository.findAll(any(Pageable.class)))
            .thenReturn(projectPage);

    Page<ProjectResponse> response = projectService.getAllProjects(
            0, 10, "createdAt", "desc"
    );

    assertEquals(1, response.getTotalElements());
    assertEquals("Spring Boot", response.getContent().get(0).name());

    verify(projectRepository).findAll(any(Pageable.class));
}

@Test
void searchProjectsSuccessfully() {
    Project project = createProject(UUID.randomUUID());

    Pageable pageable = PageRequest.of(0, 10);

    Page<Project> projectPage = new PageImpl<>(
            List.of(project),
            pageable,
            1
    );

   when(projectRepository.searchProjects(
        eq("Spring"),
        eq("Java"),
        isNull(),
        eq("java"),
        any(Pageable.class)
)).thenReturn(projectPage);

    Page<ProjectResponse> response = projectService.searchProjects(
            " Spring ",
            " Java ",
            null,
            " java ",
            0,
            10,
            "stars",
            "desc"
    );

    assertEquals(1, response.getTotalElements());
    assertEquals("Spring Boot", response.getContent().get(0).name());
}

@Test
void searchProjectsRejectsInvalidPageSize() {
    ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectService.searchProjects(
                    null, null, null, null,
                    0, 500, "stars", "desc"
            )
    );

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    verifyNoInteractions(projectRepository);
}

@Test
void searchProjectsRejectsInvalidSortField() {
    ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectService.searchProjects(
                    null, null, null, null,
                    0, 10, "githubUrl", "desc"
            )
    );

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    verifyNoInteractions(projectRepository);
}

@Test
void searchProjectsRejectsInvalidDirection() {
    ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectService.searchProjects(
                    null, null, null, null,
                    0, 10, "stars", "sideways"
            )
    );

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    verifyNoInteractions(projectRepository);
}

@Test
void searchProjectsRejectsNegativePageNumber() {
    ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectService.searchProjects(
                    null, null, null, null,
                    -1, 10, "stars", "desc"
            )
    );

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    verifyNoInteractions(projectRepository);
}

}
