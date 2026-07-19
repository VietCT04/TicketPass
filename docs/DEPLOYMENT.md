# Deployment

## Status

This document defines the approved container-stack contract from GitHub issue `#104`. It is documentation only. The current `docker-compose.yml` starts PostgreSQL only; API and web images, full-stack Compose wiring, and the operational commands described here remain follow-up work in issues `#105`, `#106`, and `#107`.

## Supported Baseline

The first supported container baseline is a single-host, three-service stack for local integration, demonstrations, and later single-host deployment work:

```text
browser -> web:3000 -> api:8080
api -> postgres:5432 on the internal Compose network
```

The browser uses the externally reachable API URL. A Docker service address such as `http://api:8080` is internal to Compose and must never be embedded in a browser bundle.

This is not a high-availability or production-complete architecture. Kubernetes, cloud infrastructure, TLS, DNS, reverse proxies, monitoring, backup/restore, disaster recovery, image publishing, and CI/CD are separate work.

## API Image Contract

Issue `#105` will add a multi-stage Java 21 image built from `apps/api/pom.xml`:

```text
build stage: Maven 3.9 with JDK 21
runtime stage: JRE 21 only
```

- Use a deterministic Maven dependency/package flow. Image packaging may skip tests under the standing verification direction.
- The runtime image contains the packaged Spring Boot artifact only, not source code or Maven.
- Run as a dedicated non-root user and expose only port `8080`.
- Retain Spring Boot graceful shutdown behavior and expose a non-sensitive health endpoint, preferably `/actuator/health`.
- Receive every deployment value from external configuration. Do not include database credentials, cookie values, webhook secrets, or environment files in the image.
- Use explicit base-image families and versions. Digest pinning needs a documented security-update process before adoption.

## Web Image Contract

Issue `#106` will add a multi-stage web image using a supported Node.js release, preferably Node 22. It must build from the root workspace lockfile with:

```text
npm ci
npm run build:web
```

- Run the Next.js production server, never `next dev`.
- Use standalone output when needed to avoid shipping the full build environment.
- Run as a dedicated non-root user and expose only port `3000`.
- Exclude `.env*`, local caches, credentials, and development-only files.

`NEXT_PUBLIC_API_BASE_URL` is public build-time configuration. The deployment build must provide it explicitly and must not silently fall back to `http://localhost:8080`. Changing the public API origin requires rebuilding the web image; runtime-neutral API discovery is future work.

## Configuration Boundary

Future container and Compose work must use externally supplied environment values. Committed examples may contain placeholders and non-secret local values only.

| Area | Variables | Rules |
| --- | --- | --- |
| PostgreSQL | `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | The Compose-network database host is `postgres`, for example `jdbc:postgresql://postgres:5432/ticketpass`. |
| API database | `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | Values are runtime configuration, never image contents. |
| API browser and cookie policy | `TICKETPASS_SECURITY_ALLOWED_ORIGINS_0`, `TICKETPASS_AUTH_COOKIE_SECURE`, `TICKETPASS_AUTH_COOKIE_DOMAIN` (optional), `TICKETPASS_FRONTEND_BASE_URL` | The configured frontend origin must exactly match CORS and trusted-origin validation. Additional origins use indexed Spring binding values. |
| Web build | `NEXT_PUBLIC_API_BASE_URL` | Public, explicitly supplied at build time, and browser-reachable. |
| Mock payment | `MOCK_PAYMENT_ENABLED`, `MOCK_PAYMENT_WEBHOOK_SECRET`, `MOCK_PAYMENT_PROVIDER_BASE_URL`, `MOCK_PAYMENT_WEBHOOK_URL`, `MOCK_PAYMENT_ALLOW_NON_LOOPBACK` | Local-stack only. The secret comes from an untracked environment file. Missing required secrets must fail startup while the mock provider is enabled. |

Scheduler interval and batch-size configuration may retain documented safe defaults unless a future implementation identifies a deployment-specific requirement.

For the local full-stack example only, the expected values are web `http://localhost:3000`, API `http://localhost:8080`, host-only cookies, and `TICKETPASS_AUTH_COOKIE_SECURE=false`. Non-local deployments require HTTPS, secure cookies, exact trusted origins, externally supplied credentials, and `MOCK_PAYMENT_ENABLED=false` until a production provider exists.

Do not add production-looking default passwords or secrets just to let Compose start without configuration.

## Compose Contract

Issue `#107` will expand the PostgreSQL-only Compose definition with `api` and `web` services. It must provide:

- the existing named PostgreSQL data volume and an explicit application network;
- `pg_isready` PostgreSQL health checks;
- API startup dependent on healthy PostgreSQL and API health checks using the approved health endpoint;
- web readiness/startup linked to API health where Compose supports it;
- a single-host-appropriate restart policy and untracked real `.env` file with a committed `.env.example` containing placeholders only;
- no fixed sleep commands; health-based ordering only;
- persistence across ordinary `docker compose down`; database removal requires an explicit destructive volume-removal command.

Port `5432` may remain exposed for local development, but direct database exposure is not production-safe. Compose health ordering does not create high availability.

## Health And Readiness

Health responses must not reveal credentials, SQL, internal configuration, payment data, or stack traces.

```text
postgres healthy: accepts database connections
api healthy: Spring application started and its health endpoint reports healthy
web healthy: production web server responds
```

Flyway runs during API startup. The API must not become healthy until required migrations and application initialization succeed. Replica coordination, migration rollback, backup/restore, and disaster recovery are outside the single-instance baseline.

## Secrets And Build Contexts

Future image and Compose work must never copy `.env`, `.env.local`, credentials, private keys, database dumps, ticket files, or payment secrets into build contexts or images. Appropriate `.dockerignore` rules must exclude `node_modules`, `.next`, `target`, IDE files, logs, caches, and local secrets.

Use environment variables now and external secret injection later. Startup output and the operational runbook must never print secret values.

## Future Operations Runbook

Issue `#107` will document commands to configure the untracked environment file, build images, start the stack, inspect health and logs, stop without deleting data, rebuild after source or configuration changes, identify image/source versions, roll back explicitly tagged images, and intentionally reset the local database.

The runbook must label destructive commands, including database-volume removal. No operation in the normal shutdown path may silently delete persisted PostgreSQL data.
