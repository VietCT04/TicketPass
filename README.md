# TicketPass

TicketPass MVP monorepo with a Next.js frontend, Spring Boot API, shared package space, and local PostgreSQL.

## Structure

```text
apps/web          Next.js frontend
apps/api          Spring Boot backend
packages/shared   Shared code placeholder
docker-compose.yml
```

## Prerequisites

- Node.js 20.19+ or 22.13+
- Java 21
- Maven 3.9+
- Docker

## Run PostgreSQL

```bash
docker compose up -d postgres
```

PostgreSQL is available at `localhost:5432`.

Database defaults:

- Database: `ticketpass`
- User: `ticketpass`
- Password: `ticketpass`

## Run Backend

```bash
cd apps/api
mvn spring-boot:run
```

The API runs at `http://localhost:8080`.

Health check:

```bash
curl http://localhost:8080/api/health
```

Expected response:

```json
{"status":"ok"}
```

## Run Frontend

```bash
cd apps/web
npm install
npm run dev
```

The frontend runs at `http://localhost:3000` and reads the API base URL from `apps/web/.env.local`.

## Useful Commands

From the monorepo root:

```bash
npm install
npm run dev:web
```

From `apps/api`:

```bash
mvn test
```
