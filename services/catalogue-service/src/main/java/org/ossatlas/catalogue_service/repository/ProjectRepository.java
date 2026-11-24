
package org.ossatlas.catalogue_service.repository;

import org.ossatlas.catalogue_service.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Optional<Project> findByGithubUrl(String githubUrl);

    boolean existsByGithubUrl(String githubUrl);

    
    

@Query(value = """
    SELECT *
    FROM projects
    WHERE
        (
            CAST(:keyword AS text) IS NULL OR
            LOWER(name) LIKE LOWER(CONCAT('%', CAST(:keyword AS text), '%')) OR
            LOWER(COALESCE(description, '')) LIKE LOWER(CONCAT('%', CAST(:keyword AS text), '%')) OR
            LOWER(owner) LIKE LOWER(CONCAT('%', CAST(:keyword AS text), '%'))
        )
    AND
        (
            CAST(:language AS text) IS NULL OR
            LOWER(language) = LOWER(CAST(:language AS text))
        )
    AND
        (
            CAST(:license AS text) IS NULL OR
            LOWER(license) = LOWER(CAST(:license AS text))
        )
    AND
        (
            CAST(:topic AS text) IS NULL OR
            topics @> jsonb_build_array(CAST(:topic AS text))
        )
    """,
    countQuery = """
    SELECT COUNT(*)
    FROM projects
    WHERE
        (
            CAST(:keyword AS text) IS NULL OR
            LOWER(name) LIKE LOWER(CONCAT('%', CAST(:keyword AS text), '%')) OR
            LOWER(COALESCE(description, '')) LIKE LOWER(CONCAT('%', CAST(:keyword AS text), '%')) OR
            LOWER(owner) LIKE LOWER(CONCAT('%', CAST(:keyword AS text), '%'))
        )
    AND
        (
            CAST(:language AS text) IS NULL OR
            LOWER(language) = LOWER(CAST(:language AS text))
        )
    AND
        (
            CAST(:license AS text) IS NULL OR
            LOWER(license) = LOWER(CAST(:license AS text))
        )
    AND
        (
            CAST(:topic AS text) IS NULL OR
            topics @> jsonb_build_array(CAST(:topic AS text))
        )
    """,
    nativeQuery = true)
Page<Project> searchProjects(
        @Param("keyword") String keyword,
        @Param("language") String language,
        @Param("license") String license,
        @Param("topic") String topic,
        Pageable pageable
);


}
