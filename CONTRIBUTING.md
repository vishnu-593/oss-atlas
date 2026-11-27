# Contributing to OSS Atlas

Thank you for your interest in contributing! Here's everything you need to get started.

## Contributor License Agreement (CLA)

Before your first Pull Request can be merged, you must sign our [Contributor License Agreement](CLA.md).

**How to sign:** Post the following comment on your PR:

> `I have read the CLA Document and I hereby sign the CLA`

The CLA bot will detect this comment and mark your PR as compliant. You only need to sign once — all future PRs from your account are automatically recognized.

---

## Get Started

### 1. Fork and clone

```bash
git clone https://github.com/YOUR_USERNAME/oss-atlas.git
cd oss-atlas
```

### 2. Prerequisites

- Java 21
- Maven (or use the included `./mvnw` wrapper)
- Node.js 20+ and npm
- Docker and Docker Compose

### 3. Start infrastructure

```bash
docker compose -f infrastructure/docker/docker-compose.yml up -d
```

### 4. Start the backend services

From each service directory (`catalogue-service`, `discovery-service`, `source-integration-service`):

```bash
./mvnw spring-boot:run
```

### 5. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

### 6. Verify everything works

```bash
# Backend tests (from each service directory)
./mvnw test

# Frontend lint
cd frontend && npm run lint
```

---

## Making a Contribution

1. Browse [`good first issue`](https://github.com/vishnu-593/oss-atlas/labels/good%20first%20issue) for beginner-friendly tasks. Leave a comment so others know it's taken.
2. Create a branch: `git checkout -b feat/your-feature`
3. Write tests alongside your implementation
4. Run `./mvnw test` and `npm run lint` — both must pass
5. Add a changelog fragment: create `changelog/<your-PR-number>.<type>.md` with one line describing your change
6. Open a PR against `main` referencing the issue (`Closes #N`)
7. Sign the CLA when prompted by the bot

---

## Code Style

**Backend (Java):**
- Follow standard Java naming conventions
- Use constructor injection, not field injection
- Keep controllers thin — business logic belongs in services

**Frontend (TypeScript/React):**
- Use functional components with hooks
- Prefer named exports
- Run `npm run lint` before pushing — CI fails on oxlint errors

---

## Changelog Fragments

Rather than editing `CHANGELOG.md` directly (which causes merge conflicts), create a small file per PR:

```
changelog/42.feat.md      ← new feature
changelog/43.fix.md       ← bug fix
changelog/44.docs.md      ← documentation
changelog/45.chore.md     ← maintenance / refactor
```

Each file contains a single line describing the change, e.g.:

```
feat: add technology filter to catalogue search (#42)
```

---

## AI Use Policy

You may use AI tools (GitHub Copilot, Claude, etc.) to assist with writing code, tests, or documentation. When you do:

- You are responsible for reviewing and understanding every line you submit
- Do not submit AI-generated code you have not read and verified
- Do not use AI to generate security-sensitive logic (auth, data validation) without careful manual review

---

## Questions?

Open a GitHub Discussion or leave a comment on the relevant issue.
