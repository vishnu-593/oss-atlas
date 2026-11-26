package org.ossatlas.discovery_service.refresh;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "refresh_cursors")
@Getter @Setter @NoArgsConstructor
public class RefreshCursor {

    @Id
    @Column(name = "query_name")
    private String queryName;

    @Column(name = "last_page", nullable = false)
    private int lastPage = 1;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
