UPDATE projects
SET health_score =
  -- Recency (0–40)
  CASE
    WHEN last_updated > NOW() - INTERVAL '30 days'  THEN 40
    WHEN last_updated > NOW() - INTERVAL '90 days'  THEN 30
    WHEN last_updated > NOW() - INTERVAL '180 days' THEN 20
    WHEN last_updated > NOW() - INTERVAL '365 days' THEN 10
    ELSE 0
  END
  +
  -- Community (0–30)
  CASE
    WHEN stars >= 10000 THEN 30
    WHEN stars >= 1000  THEN 25
    WHEN stars >= 500   THEN 20
    WHEN stars >= 100   THEN 15
    WHEN stars >= 10    THEN 8
    ELSE 3
  END
  +
  -- Documentation (0–20)
  CASE WHEN description IS NOT NULL AND description <> '' THEN 8 ELSE 0 END
  + CASE WHEN license IS NOT NULL AND license <> ''       THEN 6 ELSE 0 END
  + CASE WHEN topics IS NOT NULL AND topics::text <> '[]' THEN 6 ELSE 0 END
  +
  -- Issue health: open_issues defaults to 0 for existing rows, treat as healthy
  10
WHERE health_score = 0;
