
package org.ossatlas.catalogue_service.controller;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.ossatlas.catalogue_service.dto.ProjectRequest;
import org.ossatlas.catalogue_service.dto.ProjectResponse;
import org.ossatlas.catalogue_service.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

  private final ObjectMapper objectMapper = JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .build();

    @MockitoBean
private ProjectService projectService;

    private final UUID projectId =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private ProjectRequest sampleRequest() {
        return new ProjectRequest(
                "Spring Boot",
                "A Java framework",
                "https://github.com/example/spring-boot",
                "example",
                "Java",
                "Apache-2.0",
                1000,
                200,
                List.of("java", "spring"),
                Instant.parse("2026-11-20T10:00:00Z")
        );
    }

    private ProjectResponse sampleResponse() {
        return new ProjectResponse(
                projectId,
                "Spring Boot",
                "A Java framework",
                "https://github.com/example/spring-boot",
                "example",
                "Java",
                "Apache-2.0",
                1000,
                200,
                List.of("java", "spring"),
                Instant.parse("2026-11-20T10:00:00Z"),
                Instant.parse("2026-11-20T10:00:00Z"),
                Instant.parse("2026-11-20T10:00:00Z")
        );
    }

    @Test
    void createProjectReturnsCreated() throws Exception {
        when(projectService.createProject(any(ProjectRequest.class)))
                .thenReturn(sampleResponse());

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Spring Boot"))
                .andExpect(jsonPath("$.language").value("Java"));
    }

    @Test
    void createProjectRejectsInvalidRequest() throws Exception {
        String invalidRequest = """
                {
                  "name": "",
                  "githubUrl": "",
                  "owner": "",
                  "stars": -1,
                  "forks": -1
                }
                """;

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(projectService);
    }

    @Test
    void getAllProjectsReturnsPage() throws Exception {
        Page<ProjectResponse> page = new PageImpl<>(
                List.of(sampleResponse()),
                PageRequest.of(0, 10),
                1
        );

        when(projectService.getAllProjects(0, 10, "createdAt", "desc"))
                .thenReturn(page);

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Spring Boot"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getProjectByIdReturnsProject() throws Exception {
        when(projectService.getProjectById(projectId))
                .thenReturn(sampleResponse());

        mockMvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Spring Boot"));
    }

    @Test
    void updateProjectReturnsUpdatedProject() throws Exception {
        when(projectService.updateProject(
                eq(projectId), any(ProjectRequest.class)))
                .thenReturn(sampleResponse());

        mockMvc.perform(put("/api/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Spring Boot"));
    }

    @Test
    void deleteProjectReturnsNoContent() throws Exception {
        doNothing().when(projectService).deleteProject(projectId);

        mockMvc.perform(delete("/api/projects/{id}", projectId))
                .andExpect(status().isNoContent());

        verify(projectService).deleteProject(projectId);
    }

    @Test
    void searchProjectsReturnsMatchingProjects() throws Exception {
        Page<ProjectResponse> page = new PageImpl<>(
                List.of(sampleResponse()),
                PageRequest.of(0, 10),
                1
        );

        when(projectService.searchProjects(
                "Spring", "Java", "Apache-2.0", "java",
                0, 10, "stars", "desc"
        )).thenReturn(page);

        mockMvc.perform(get("/api/projects/search")
                        .param("keyword", "Spring")
                        .param("language", "Java")
                        .param("license", "Apache-2.0")
                        .param("topic", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Spring Boot"));
    }
}
