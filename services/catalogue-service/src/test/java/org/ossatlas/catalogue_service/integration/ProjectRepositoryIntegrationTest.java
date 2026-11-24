
package org.ossatlas.catalogue_service.integration;

import org.junit.jupiter.api.Test;
import org.ossatlas.catalogue_service.entity.Project;
import org.ossatlas.catalogue_service.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
class ProjectRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("oss_atlas")
                    .withUsername("oss_atlas")
                    .withPassword("local_dev_password");

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void shouldSaveAndRetrieveProject() {
        Project project = new Project();

        project.setName("Spring Boot");
        project.setDescription("Java application framework");
        project.setGithubUrl("https://github.com/example/spring-boot");
        project.setOwner("example");
        project.setLanguage("Java");
        project.setLicense("Apache-2.0");
        project.setStars(1000);
        project.setForks(200);
        project.setTopics(List.of("java", "spring"));
        project.setLastUpdated(Instant.parse("2026-11-20T10:00:00Z"));

        Project savedProject = projectRepository.saveAndFlush(project);

        assertThat(savedProject.getId()).isNotNull();

        Project retrievedProject = projectRepository
                .findByGithubUrl("https://github.com/example/spring-boot")
                .orElseThrow();

        assertThat(retrievedProject.getName()).isEqualTo("Spring Boot");
        assertThat(retrievedProject.getLanguage()).isEqualTo("Java");
        assertThat(retrievedProject.getTopics())
                .containsExactly("java", "spring");
    }

    @Test
    void shouldEnforceUniqueGithubUrl() {
        Project first = createProject(
                "Project One",
                "https://github.com/example/unique-project"
        );

        Project second = createProject(
                "Project Two",
                "https://github.com/example/unique-project"
        );

        projectRepository.saveAndFlush(first);

        assertThatThrownBy(() ->
                projectRepository.saveAndFlush(second)
        ).isInstanceOf(RuntimeException.class);
    }

    private Project createProject(String name, String githubUrl) {
        Project project = new Project();

        project.setName(name);
        project.setDescription("Integration test project");
        project.setGithubUrl(githubUrl);
        project.setOwner("example");
        project.setLanguage("Java");
        project.setLicense("MIT");
        project.setStars(100);
        project.setForks(10);
        project.setTopics(List.of("testing"));
        project.setLastUpdated(Instant.now());

        return project;
    }
}
