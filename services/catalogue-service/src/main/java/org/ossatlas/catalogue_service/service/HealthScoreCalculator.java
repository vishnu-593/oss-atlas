package org.ossatlas.catalogue_service.service;

import org.ossatlas.catalogue_service.entity.Project;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class HealthScoreCalculator {

    private HealthScoreCalculator() {}

    public static int compute(Project p) {
        return recency(p.getLastUpdated())
                + community(p.getStars())
                + documentation(p.getDescription(), p.getLicense(), p.getTopics())
                + issueHealth(p.getOpenIssues());
    }

    public static String label(int score) {
        if (score >= 85) return "Healthy";
        if (score >= 65) return "Active";
        if (score >= 45) return "Moderate";
        if (score >= 20) return "Low Activity";
        return "Inactive";
    }

    // 0–40 pts: how recently the repo was pushed to
    static int recency(Instant lastUpdated) {
        if (lastUpdated == null) return 5;
        long days = ChronoUnit.DAYS.between(lastUpdated, Instant.now());
        if (days < 30)  return 40;
        if (days < 90)  return 30;
        if (days < 180) return 20;
        if (days < 365) return 10;
        return 0;
    }

    // 0–25 pts: community adoption via stars
    static int community(Integer stars) {
        if (stars == null) return 0;
        if (stars >= 50_000) return 25;
        if (stars >= 10_000) return 20;
        if (stars >= 1_000)  return 15;
        if (stars >= 500)    return 10;
        if (stars >= 100)    return 6;
        if (stars >= 10)     return 3;
        return 0;
    }

    // 0–15 pts: basic documentation signals
    static int documentation(String description, String license, java.util.List<String> topics) {
        int pts = 0;
        if (description != null && !description.isBlank()) pts += 6;
        if (license != null && !license.isBlank())         pts += 5;
        if (topics != null && !topics.isEmpty())           pts += 4;
        return pts;
    }

    // 0–20 pts: open issue count — primary differentiator among active repos
    static int issueHealth(Integer openIssues) {
        if (openIssues == null || openIssues == 0) return 20;
        if (openIssues < 5)    return 16;
        if (openIssues < 20)   return 12;
        if (openIssues < 50)   return 7;
        if (openIssues < 200)  return 3;
        if (openIssues < 1000) return 1;
        return 0;
    }
}
