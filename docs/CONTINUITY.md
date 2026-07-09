# Continuity

## Current Project State

TicketPass is an early monorepo scaffold with a Next.js frontend, Spring Boot API, shared package placeholder, initial seller listing contract docs, and an approved authentication user story.

## Latest Completed Work

- Date: 2026-07-09
- GitHub Issues: `#9` through `#14`
- Summary: Created focused authentication issues from `US-0002: Authenticate User` and linked them back to the user story.
- Files changed:
  - `docs/user-stories/US-0002-authenticate-user.md`
  - `docs/CONTINUITY.md`

## Active Work

- Current GitHub Issue: `#9` - Define authentication model and API contract
- Current goal: Define the auth model, session strategy, and API contract before backend implementation.
- Current blocker: Seller listing API issue `#3` should wait until authentication is implemented.

## Important User Stories

- `docs/user-stories/US-0001-list-transferable-ticket.md`: Seller can list a transferable ticket safely without exposing sensitive ticket data too early.
- `docs/user-stories/US-0002-authenticate-user.md`: User can sign up, log in, log out, maintain secure sessions, and access protected TicketPass account features.

## Known Concerns

- See `docs/CONCERNS.md`.
- Platform-specific transferability rules are unresolved.
- Seller transferability confirmation is not proof.
- Event reuse and deduplication rules are not defined for MVP.

## Next Recommended Steps

1. Complete GitHub Issue `#9` to define the authentication model and API contract.
2. Implement GitHub Issues `#10` through `#14` in order.
3. Return to GitHub Issue `#3` and derive `seller_id` from the authenticated user.
