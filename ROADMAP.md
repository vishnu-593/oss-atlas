# Project Roadmap

This roadmap reflects the current direction of OSS Atlas based on open issues and planned work.
It is a living document — priorities shift based on contributions and feedback.

> **Note:** Dates are targets, not commitments. Want to help? Pick any issue tagged
> [`good first issue`](https://github.com/vishnu-593/oss-atlas/labels/good%20first%20issue).

Status key: 🔵 In Progress · ✅ Completed · 📋 Planned

---

## v0.1.0 — Foundation ✅ Released

Core platform: catalogue browsing, search and filtering, GitHub discovery integration,
health score calculation, project comparison, resilient pagination.

---

## v0.2 — Search & Discovery 📋

> Goal: make finding the right open-source project fast and accurate.

| Issue | Title | Labels |
|---|---|---|
| — | Advanced filters: stars, forks, last commit date | `enhancement` `good first issue` |
| — | Full-text search across project descriptions | `enhancement` |
| — | Saved searches / bookmarks | `enhancement` |
| — | Paginated discovery results with infinite scroll | `enhancement` `good first issue` |
| — | Sort catalogue by health score, popularity, recency | `enhancement` `good first issue` |

---

## v0.3 — Project Detail & Compare 📋

> Goal: give users everything they need to evaluate a project without leaving OSS Atlas.

| Issue | Title | Labels |
|---|---|---|
| — | Dependency graph visualization | `enhancement` |
| — | Licence compatibility checker | `enhancement` |
| — | Side-by-side comparison of up to 4 projects | `enhancement` |
| — | Contributor activity chart | `enhancement` `good first issue` |
| — | Direct link to open issues / PRs on GitHub | `enhancement` `good first issue` |

---

## v0.4 — Integrations & API 📋

> Goal: expose OSS Atlas data as a first-class API and integrate with the wider ecosystem.

| Issue | Title | Labels |
|---|---|---|
| — | Public REST API with OpenAPI docs | `enhancement` |
| — | Webhook notifications for new discoveries | `enhancement` |
| — | GitLab repository source integration | `enhancement` `integration` |
| — | npm / PyPI package metadata enrichment | `enhancement` `integration` |
| — | Export catalogue entries as JSON / CSV | `enhancement` `good first issue` |

---

## v0.5 — Scale & Production 📋

> Goal: production-ready for real traffic and observability.

| Issue | Title | Labels |
|---|---|---|
| — | Rate limiting on discovery endpoints | `enhancement` `maintenance` |
| — | Prometheus metrics + Grafana dashboards | `enhancement` |
| — | Kubernetes deployment manifests | `maintenance` |
| — | Automated nightly re-indexing job | `enhancement` |
| — | End-to-end test suite (Playwright) | `testing` |
