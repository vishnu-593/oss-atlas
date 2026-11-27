
package org.ossatlas.catalogue_service.repository;

import org.ossatlas.catalogue_service.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Optional<Project> findByGithubUrl(String githubUrl);

    boolean existsByGithubUrl(String githubUrl);

    @Query(value = "SELECT language AS name, COUNT(*) AS cnt FROM projects " +
                   "WHERE language IS NOT NULL AND language <> '' " +
                   "GROUP BY language ORDER BY cnt DESC LIMIT 8",
           nativeQuery = true)
    List<Object[]> countByLanguage();

    @Query(value = "SELECT license AS name, COUNT(*) AS cnt FROM projects " +
                   "WHERE license IS NOT NULL AND license <> '' " +
                   "GROUP BY license ORDER BY cnt DESC",
           nativeQuery = true)
    List<Object[]> countByLicense();

    @Query(value = """
        SELECT s.h AS name, COUNT(*) AS cnt
        FROM (
            SELECT
                CASE
                    WHEN last_updated >= NOW() - INTERVAL '3 months'  THEN 'Active'
                    WHEN last_updated >= NOW() - INTERVAL '12 months' THEN 'Healthy'
                    WHEN last_updated >= NOW() - INTERVAL '24 months' THEN 'Slow'
                    ELSE 'Inactive'
                END AS h
            FROM projects
            WHERE last_updated IS NOT NULL
        ) s
        GROUP BY s.h
        """,
           nativeQuery = true)
    List<Object[]> countByHealth();

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
