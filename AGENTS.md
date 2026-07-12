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
- `docs`: product, architecture, API, database, security, flows, user stories, concerns, and continuity docs

## Mandatory Working Rule

Before making changes, read the relevant docs.

After making changes, update the relevant docs.

Code and docs must stay synchronized.

If a change affects behavior, API, database, security, payment, escrow, ticket reveal, or dispute logic, documentation must be updated in the same change.

## Minimal Change Rule

Make the smallest correct change possible.

Do not refactor unrelated code.

Do not rename files, move folders, rewrite modules, or introduce new patterns unless the GitHub Issue explicitly requires it.

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
| User story/product requirement | `docs/user-stories/*.md` |
| Unresolved risk/uncertainty | `docs/CONCERNS.md` |
| Work handoff/progress | `docs/CONTINUITY.md` |

If no documentation update is needed, explicitly mention why in the final response.

## GitHub Issues Workflow

All implementation work should be linked to a GitHub Issue in the project repository.

Open GitHub Issues represent unresolved work. Closed GitHub Issues represent completed work.

Do not create local ticket files under `docs/tickets/`. Use GitHub Issues instead.

User stories may describe broad product behavior, but GitHub Issues should be small implementation slices.

Prefer several focused issues over one large issue when work spans multiple areas such as database, API, frontend, security, audit logging, payments, escrow, ticket reveal, or disputes.

Each implementation issue should usually have one primary outcome, one main affected area, and acceptance criteria that can be verified independently.

Split an issue when it includes multiple deployable steps, multiple sensitive business rules, or changes across unrelated layers.

Before resolving a GitHub Issue, write a proposal as a GitHub Issue comment and wait for user approval.

Use the GitHub Issue comment thread as the approval and revision loop. If the user comments on that proposal in GitHub, respond with a revised proposal as another GitHub Issue comment, and repeat until the user approves the scope.

If GitHub is unavailable, write the proposal in the conversation and later mirror the approved proposal back to the GitHub Issue when access is restored.

The proposal should summarize the intended scope, files to update, contract or schema decisions, open questions, and what will be commented back to GitHub.

After approval, update the related files and update `docs/CONTINUITY.md`.

When commenting on GitHub after approval, use the approved proposal as the source of truth. Do not replace it with a separately invented completion summary. The GitHub comment should preserve the approved scope, decisions, open questions, and next steps. It may add a short factual note listing files changed, tests run, and any implementation result that differs from the proposal.

If a previous GitHub Issue comment does not match the approved proposal, add a corrective follow-up comment with the approved proposal and note that it supersedes the earlier comment.

When completing an issue:

1. Implement the smallest correct change.
2. Update all affected docs.
3. Add concerns to `docs/CONCERNS.md` if any remain.
4. Update `docs/CONTINUITY.md`.
5. Add completion notes in the GitHub Issue or linked pull request.
6. Close the GitHub Issue only when the work is complete and verified.

## Issue Creation Rule

If a new task comes from a user story, create one or more focused GitHub Issues from that user story.

User stories live in:

```txt
docs/user-stories/
```

Each GitHub Issue should include:

```md
# Short Title

## Source

User Story: `docs/user-stories/US-0001-example.md`

## Context

Explain why this task exists.

## Goal

Explain the desired outcome.

## Scope

Describe the narrow implementation slice this issue covers.

## Out of Scope

List related work that should be handled by separate issues.

## Concerns

List known risks, uncertainties, or decisions that need human review.
```

## User Story Workflow

User stories describe product behavior from a user's perspective and are the source for implementation issues.

Use user stories for product-facing behavior involving:

- Buyer workflows
- Seller workflows
- Admin workflows
- Payment and escrow behavior
- Ticket listing, purchase, reveal, and dispute behavior
- Authentication and authorization behavior
- Notifications, audit logs, and trust/safety behavior

User story folder:

```txt
docs/user-stories/
```

User story filename format:

```txt
US-0001-short-title.md
```

User story template:

```md
# US-0001: Short Title

## User Story

As a [user type], I want [goal], so that [benefit].

## Context

Explain why this behavior matters.

## Acceptance Criteria

- [ ] Criterion 1
- [ ] Criterion 2
- [ ] Sensitive flows are handled server-side where relevant
- [ ] Relevant docs are updated


## Risks

## Follow-up Issues

- GitHub Issue: `#123`
```

When a user story is ready for implementation, create follow-up GitHub Issues if work is required.

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
- GitHub Issue
- Summary
- Files changed

## Active Work

- Current GitHub Issue
- Current goal
- Current blocker if any

## Important User Stories

- User story links
- Summary of active or recently completed product stories

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

## Testing Rules

Add or update tests when practical, especially for security-sensitive, payment, escrow, ticket reveal, dispute, authorization, and API validation behavior.

For now, do not run backend or frontend test suites after coding unless the user explicitly asks for them.

Agents may still write or update backend unit tests, frontend tests, or other test code when practical, but running those tests is not required by default.

Run non-test verification commands, such as lint, build, typecheck, or formatting checks, when they are relevant and the environment supports them.

If the user explicitly approves review at the pull-request or CI level, or explicitly says local testing is not needed, do not keep attempting local test runs. In that case, clearly state in the final response and pull request that local tests were not run by user direction.

If local tests cannot run because of environment limitations, dependency access, toolchain mismatch, or another blocker, document the reason in the final response and add or update `docs/CONCERNS.md` when the risk is meaningful.

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
