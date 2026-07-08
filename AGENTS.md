# AGENTS.md

## Project Overview

TicketPass is a P2P ticket marketplace focused on safer ticket resale through escrow, controlled ticket reveal, audit logs, and dispute handling.

The system must protect both sides:

- Buyers from fake, reused, invalid, or non-transferable tickets.
- Sellers from buyers falsely claiming failure after receiving a valid ticket.

This repository is a monorepo.

## Repository Structure

- `apps/web`: frontend application
- `apps/api`: backend API
- `packages/shared`: shared types, DTOs, enums, validation schemas, and constants
- `docs`: product, architecture, API, database, security, flows, ADRs, tickets, concerns, and continuity docs

## Mandatory Working Rule

Before making changes, read the relevant docs.

After making changes, update the relevant docs.

Code and docs must stay synchronized.

If a change affects behavior, API, database, security, payment, escrow, ticket reveal, or dispute logic, documentation must be updated in the same change.

## Minimal Change Rule

Make the smallest correct change possible.

Do not refactor unrelated code.

Do not rename files, move folders, rewrite modules, or introduce new patterns unless the ticket explicitly requires it.

Prefer surgical fixes over broad redesigns.

When modifying existing code:

1. Understand the current pattern.
2. Follow the existing style.
3. Change only what is necessary.
4. Avoid touching unrelated files.
5. Avoid dependency additions unless clearly justified.

## Documentation Update Rules

Update these docs when relevant:

| Change Type | Required Docs |
|---|---|
| API endpoint/request/response | `docs/API.md` |
| Database schema/model/status enum | `docs/DATABASE.md` |
| Security/auth/permissions | `docs/SECURITY.md` |
| Architecture/design decision | `docs/adr/accepted/*.md` |
| Unresolved risk/uncertainty | `docs/CONCERNS.md` |
| Work handoff/progress | `docs/CONTINUITY.md` |

If no documentation update is needed, explicitly mention why in the final response.

## Ticket Workflow

All implementation work should be linked to a ticket under:

```txt
docs/tickets/open/
```

Open tickets represent unresolved work.

Closed tickets must be moved to:

```txt
docs/tickets/closed/
```

Do not delete completed tickets.

When completing a ticket:

1. Implement the smallest correct change.
2. Update all affected docs.
3. Add concerns to `docs/CONCERNS.md` if any remain.
4. Update `docs/CONTINUITY.md`.
5. Move the ticket from `docs/tickets/open/` to `docs/tickets/closed/`.
6. Add completion notes inside the ticket.

## Ticket Creation Rule

If a new task comes from an accepted ADR, create a ticket from that ADR.

Accepted ADRs live in:

```txt
docs/adr/accepted/
```

Ticket files should be created in:

```txt
docs/tickets/open/
```

Ticket filename format:

```txt
TICKET-0001.md
```

Each ticket should include:

```md
# TICKET-0001: Short Title

## Status

Open

## Source

ADR: `docs/adr/accepted/0001-example.md`

## Context

Explain why this task exists.

## Goal

Explain the desired outcome.

## Concerns

List known risks, uncertainties, or decisions that need human review.

## Completion Notes

Filled when the ticket is closed.
```

## ADR Workflow

ADRs are for important decisions, not normal tasks.

For normal tasks just create tickets.

Use ADRs for decisions involving:

- Monorepo structure
- Tech stack
- Database choice
- Payment/escrow model
- Ticket reveal model
- Dispute model
- Authentication model
- Major architecture changes

ADR folders:

```txt
docs/adr/accepted/
```

ADR filename format:

```txt
0001-use-monorepo.md
```

ADR template:

```md
# ADR 0001: Decision Title

## Context

What problem are we solving?

## Decision

What decision was made?

## Consequences

### Positive

- Benefit 1
- Benefit 2

### Negative

- Tradeoff 1
- Tradeoff 2

## Follow-up Tickets

- `docs/tickets/open/TICKET-0001-example.md`
```

When an ADR becomes accepted, create follow-up implementation tickets if work is required.

## CONCERNS.md Rule

Use `docs/CONCERNS.md` for unresolved risks, assumptions, or questions.

Add to `docs/CONCERNS.md` when:

- A requirement is ambiguous.
- A security risk exists.
- A payment/escrow edge case is unclear.
- A database migration may be risky.
- A user flow has unresolved product risk.
- A temporary workaround was used.
- A test could not be added.
- A dependency or design choice may need review.

Do not hide uncertainty in code comments only. Put important concerns in `docs/CONCERNS.md`.

## CONTINUITY.md Rule

Use `docs/CONTINUITY.md` as the handoff file for future AI agents and developers.

Update it after every meaningful change.

It should contain:

```md
# Continuity

## Current Project State

Brief summary of what currently works.

## Latest Completed Work

- Date
- Ticket
- Summary
- Files changed

## Active Work

- Current ticket
- Current goal
- Current blocker if any

## Important Decisions

- ADR links
- Summary of decisions

## Known Concerns

- Link to `docs/CONCERNS.md`

## Next Recommended Steps

1. Step one
2. Step two
3. Step three
```

## Database Rules

Before changing database structure:

1. Read `docs/DATABASE.md`.
2. Check existing models, migrations, enums, and relationships.
3. Make the smallest schema change possible.
4. Add or update migration files.
5. Update `docs/DATABASE.md`.
6. Update shared types in `packages/shared` if needed.
7. Update API docs if database changes affect API behavior.

Never change database status enums without checking:

- Order state transitions
- Escrow state transitions
- Ticket status transitions
- Dispute status transitions

## API Rules

Before changing API behavior:

1. Read `docs/API.md`.
2. Check existing request/response types.
3. Update shared DTOs or schemas.
4. Validate backend input.
5. Update frontend API usage.
6. Update `docs/API.md`.

Do not invent new endpoints if an existing endpoint can be extended safely.

Do not change API response shapes without updating all consumers.

## Frontend Rules

- Use shared types from `packages/shared`.
- Do not duplicate backend DTOs manually.
- Handle loading, empty, success, and error states.
- Do not expose hidden QR/ticket data before reveal is allowed.
- Do not rely on frontend-only checks for sensitive actions.
- Keep components focused and readable.

## Backend Rules

- Validate all user input.
- Check authorization server-side.
- Keep business logic outside thin route/controller layers where possible.
- Use explicit state transitions for orders, tickets, escrow, and disputes.
- Log important business events.
- Make payment webhook processing idempotent.
- Never trust client-provided payment status.
- Never trust client-provided ownership status.

## Shared Package Rules

Use `packages/shared` for:

- DTOs
- Enums
- Validation schemas
- Shared constants
- API contract types

Do not duplicate these across frontend and backend.

When shared types change, check both frontend and backend usage.

## Security Rules

- Never commit secrets.
- Never expose database URLs, API keys, payment keys, JWT secrets, or private credentials.
- Never store raw card information.
- Never expose full QR ticket data before allowed reveal.
- Admin endpoints must require admin authorization.
- Users must only access their own tickets, orders, payments, and disputes unless they are admins.
- File uploads must be validated by type, size, and ownership.
- Sensitive actions must be auditable.