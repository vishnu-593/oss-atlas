package org.ossatlas.discovery_service.refresh;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshCursorRepository extends JpaRepository<RefreshCursor, String> {}
