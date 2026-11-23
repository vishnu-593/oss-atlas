package org.ossatlas.discovery_service.refresh;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "oss-atlas.refresh")
public class RefreshProperties {
    private boolean enabled = true;
    private String cron = "0 0 23 * * *";
    private String zone = "Asia/Kolkata";
    private int pageSize = 50;
    private List<DiscoveryQuery> queries = new ArrayList<>();
}
