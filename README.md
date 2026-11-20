# OSS Atlas

**Discover. Explore. Compare. Open Source.**

OSS Atlas is an open-source software discovery platform that helps users discover, explore and compare open-source software from different sources.

Our goal is to make finding suitable open-source alternatives easier through a structured software catalogue and intelligent repository discovery.

## Features

- Browse a structured catalogue of open-source software.
- Search and filter projects by category, licence and technology.
- Explore detailed software information.
- Discover additional repositories through GitHub integration.
- Identify already-indexed and newly discovered projects.

## Technology Stack

| Component | Technology |
|---|---|
| Frontend | React, TypeScript, Vite |
| Backend | Java 21, Spring Boot |
| Database | PostgreSQL |
| Integration | GitHub REST API |
| Infrastructure | Docker Compose |

## Project Structure

```text
oss-atlas/
├── frontend/
├── services/
│   ├── catalogue-service/
│   ├── discovery-service/
│   └── source-integration-service/
├── infrastructure/
│   └── docker/
├── docs/
├── LICENSE
└── README.md
```

## Local Development

### Prerequisites

- Java 21
- Maven
- Node.js and npm
- Docker
- Git

### Start PostgreSQL

```bash
docker compose -f infrastructure/docker/docker-compose.yml up -d
```

### Start the frontend

```bash
cd frontend
npm install
npm run dev
```

### Start backend services

From each service directory, run:

```bash
./mvnw spring-boot:run
```

Refer to individual service configuration for database settings and ports.

## Project Status

OSS Atlas is currently under active development.

## Contributing

Contributions, ideas and feedback are welcome. Please open an issue or pull request before making significant changes.

## License

OSS Atlas is licensed under the MIT License. See [LICENSE](LICENSE) for details.
