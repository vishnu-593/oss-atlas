# Discovery Service

Handles live GitHub repository discovery for OSS Atlas. Accepts search criteria, queries the GitHub REST API, normalizes results, deduplicates, and imports into the Catalogue Service. A scheduled job also refreshes existing catalogue records and can discover new projects on a configurable schedule.

**Port:** `8082`

---

## Prerequisites

- Java 21
- Maven 3.9+
- Catalogue Service running on port 8081
- A GitHub personal access token (optional but recommended to avoid rate limits)

---

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/vishnu-593/oss-atlas.git
cd oss-atlas/services/discovery-service
```

### 2. Set your GitHub token (optional)

Without a token the GitHub search API allows 10 requests per minute. With a token it allows 30 per minute.

**Mac/Linux:**
```bash
export GITHUB_TOKEN=your_token_here
```

**Windows (Command Prompt):**
```cmd
set GITHUB_TOKEN=your_token_here
```

### 3. Run the service

```bash
mvn spring-boot:run
```

The service starts on `http://localhost:8082`.

---

## API

### POST /api/discovery/search

Search GitHub for repositories, normalize results, deduplicate, and import into the Catalogue.

**Request body:**

```json
{
  "query": "machine learning",
  "language": "Python",
  "minStars": 100,
  "license": "MIT",
  "topic": "deep-learning",
  "limit": 20
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `query` | string | Yes | Search keywords |
| `language` | string | No | Filter by programming language |
| `minStars` | integer (≥ 0) | No | Minimum star count |
| `license` | string | No | SPDX license identifier e.g. `MIT`, `apache-2.0` |
| `topic` | string | No | GitHub topic tag |
| `limit` | integer (1–50) | No | Max results to return. Default: 10, Maximum: 50 |

**Response:**

```json
{
  "query": "machine learning",
  "totalFound": 3,
  "projects": [
    {
      "name": "example-repo",
      "description": "An example repository",
      "githubUrl": "https://github.com/owner/example-repo",
      "owner": "owner",
      "language": "Python",
      "license": "MIT",
      "stars": 1200,
      "forks": 150,
      "topics": ["python", "machine-learning"],
      "lastUpdated": "2026-11-20T10:00:00Z"
    }
  ],
  "importResult": {
    "received": 3,
    "imported": 2,
    "updated": 1,
    "duplicates": 0,
    "message": "Project import completed",
    "error": null
  }
}
```

If the Catalogue is unavailable, `projects` is still returned with the GitHub results, and `importResult.error` contains the error message.

**Status codes:**

| Code | Meaning |
|---|---|
| 200 | Success |
| 400 | Invalid request — missing or blank `query`, `limit` out of range, negative `minStars` |
| 503 | GitHub API unavailable |

**Example curl:**

```bash
curl -X POST http://localhost:8082/api/discovery/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "spring boot",
    "language": "Java",
    "minStars": 100,
    "limit": 5
  }'
```

---

## Scheduled Refresh

The service runs a background job that refreshes existing Catalogue records with current GitHub metadata and can optionally discover new projects.

### Configuration

Set these in `application.properties` or override with environment variables:

```properties
# Enable or disable the refresh job
oss-atlas.refresh.enabled=true

# Cron expression (default: daily at 11 PM IST)
oss-atlas.refresh.cron=0 0 23 * * *

# Timezone for the cron schedule
oss-atlas.refresh.zone=Asia/Kolkata

# Number of Catalogue projects to process per page
oss-atlas.refresh.page-size=50

# Optional: discovery queries to find new projects
oss-atlas.refresh.queries[0].query=open source developer tools
oss-atlas.refresh.queries[0].language=Java
oss-atlas.refresh.queries[0].min-stars=100
oss-atlas.refresh.queries[0].limit=10
```

### What the refresh job does

1. **Refresh existing projects** — pages through all Catalogue projects that have a `repositoryUrl`, fetches current metadata from GitHub (`GET /repos/{owner}/{repo}`), and sends updates to the Catalogue bulk import API.

2. **Discover new projects** — for each configured `queries` entry, searches GitHub and imports any new repositories not already in the Catalogue.

### Reliability

- **No overlapping runs** — a second scheduled trigger is skipped if the previous run is still active.
- **GitHub rate limit handling** — a 403 response stops the current batch and logs a warning; remaining items are skipped rather than failing the whole job.
- **Partial failures** — a failure on one project is logged and counted; the job continues with the remaining projects.
- **Catalogue errors** — import failures are logged and counted; they do not cause the job to crash.

### Refresh summary log

At the end of every run a summary line is logged:

```
Discovery refresh complete — examined=50 imported=5 updated=43 skipped=2 failures=0
```

---

## Running Tests

```bash
mvn test
```

41 tests across five test classes:

| Class | Tests | What it covers |
|---|---|---|
| `SearchServiceImplTest` | 17 | Query building, metadata mapping, deduplication, error handling, limits |
| `CatalogueImportTest` | 5 | Catalogue import success, unavailability, duplicate handling |
| `RefreshJobTest` | 9 | Scheduled refresh, empty results, GitHub failure, rate limit, disabled flag, overlap prevention, partial failures, discovery queries |
| `SearchControllerTest` | 7 | Valid requests, validation errors (400) |
| `DiscoveryControllerTest` | 2 | Existing GET /api/discover endpoint |

---

## Package Structure

```
src/main/java/org/ossatlas/discovery_service/
├── controller/                  # HTTP endpoints
├── service/                     # Business logic (search + dedup)
├── client/                      # HTTP clients for downstream services
├── integration/
│   ├── github/                  # GitHub API client and response types
│   └── catalogue/               # Catalogue import request/response types
├── refresh/                     # Scheduled refresh job and configuration
├── dto/                         # Request and response objects
└── exception/                   # Global error handling
```

---

## Notes

- The service has no database. It queries GitHub live on each request.
- GitHub search qualifiers used: `language:`, `stars:>=`, `license:`, `topic:`
- Results are deduplicated by normalized `githubUrl` within each response.
- GitHub tokens are never hardcoded — always read from the `GITHUB_TOKEN` environment variable.
- Discovery must never access PostgreSQL directly — all persistence goes through the Catalogue Service API.
