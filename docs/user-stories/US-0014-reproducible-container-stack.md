# US-0014 — Run TicketPass As A Reproducible Container Stack

## User Story

As a maintainer, I want TicketPass packaged and configured as a reproducible container stack so that the web application, API, and database can run consistently without relying on developer-specific host tooling.

## Context

TicketPass is a monorepo containing a Next.js frontend, a Spring Boot API, and PostgreSQL. The current Docker Compose file starts PostgreSQL only. Running the complete application still requires host-installed Node.js, Java, and Maven, plus manually coordinated configuration and startup.

A version-controlled container baseline will make onboarding, integration demonstrations, and later single-host deployment work more predictable. This story establishes a secure full-stack container foundation only. It does not claim production high availability or choose a cloud platform.

## Scope

- Define the supported web, API, and PostgreSQL container topology.
- Add production-like multi-stage images for the Spring Boot API and Next.js web application.
- Run application containers as non-root users with minimal runtime contents.
- Install dependencies deterministically from committed build manifests and lockfiles.
- Supply database, origin, URL, cookie, scheduler, and mock-payment settings through explicit configuration boundaries.
- Keep secrets, credentials, and real environment files outside committed images and Compose definitions.
- Expand the PostgreSQL-only Compose setup into a health-aware full-stack environment.
- Preserve PostgreSQL data in a named volume and document reset behavior.
- Define startup ordering, health checks, restart behavior, and graceful shutdown.
- Document build, start, stop, logs, health inspection, configuration, and rollback operations.
- Clearly distinguish local mock-payment behavior from production-safe deployment behavior.
- Document that the current `NEXT_PUBLIC_API_BASE_URL` is embedded into the frontend browser bundle at build time.

## Out of Scope

- Cloud-provider selection or infrastructure as code.
- Kubernetes, autoscaling, high availability, load balancing, service mesh, or zero-downtime deployment.
- TLS certificate, DNS, public-domain, CDN, or reverse-proxy provisioning.
- Production secrets manager integration.
- Database backup, restore, replication, or disaster-recovery automation.
- Centralized monitoring, alerting, tracing, or log aggregation.
- Production payment-provider integration.
- Pull-request CI, required test gates, image publishing, vulnerability scanning, or automated deployment.
- Refactoring the frontend to runtime-neutral API discovery.

## Acceptance Criteria

- [ ] The supported full-stack container topology and network boundaries are documented.
- [ ] The API and web applications each have an approved multi-stage image contract.
- [ ] Application containers run as non-root users and exclude unnecessary build tooling from runtime images.
- [ ] Required build-time and runtime configuration is explicit.
- [ ] No secret, credential, or production key is embedded in an image or committed environment file.
- [ ] The stack defines health-aware startup for PostgreSQL, API, and web services.
- [ ] PostgreSQL data persistence and destructive reset behavior are documented.
- [ ] Mock payment is clearly local-only and cannot be mistaken for production payment behavior.
- [ ] The frontend API-origin rebuild limitation is documented.
- [ ] Operational commands and single-host limitations are documented.
- [ ] Relevant README, security, concern, deployment, and continuity documentation is updated by the focused issues.

## Focused Issues

- `#104` — Define reproducible container-stack contract.
- `#105` — Containerize TicketPass API. Implemented: multi-stage Java 21 image, non-root read-only runtime payload, external container profile, non-sensitive health check, graceful shutdown, and disabled-by-default mock payment.
- `#106` — Containerize TicketPass web application.
- `#107` — Add full-stack Compose and deployment runbook.

## Delivery Order

1. Define and approve the container, configuration, networking, health, and security contract in `#104`.
2. Build the API image and externalize its runtime configuration in `#105`. Completed.
3. Build the web image and document its build-time API-origin requirement in `#106`.
4. Wire PostgreSQL, API, and web together and document operations in `#107`.
5. Create separate future stories for CI/CD, image publishing, cloud infrastructure, observability, backup/restore, and production high availability.

## Concerns

- `NEXT_PUBLIC_API_BASE_URL` is browser-visible build-time configuration, so changing the public API origin currently requires rebuilding the web image.
- Cookie, CORS, and trusted-origin settings must remain aligned with the selected frontend and API origins.
- The API currently contains localhost-oriented defaults that are suitable for development but must not be treated as production-safe configuration.
- Flyway startup behavior across multiple API replicas is outside this single-instance baseline.
- Base-image pinning improves reproducibility but requires an explicit security-update process.
- Docker Compose provides a useful single-host baseline but does not provide high availability, managed secrets, backups, or production observability.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, image builds, Compose startup, or other verification commands for this documentation story. Verification remains deferred to the final application phase.
