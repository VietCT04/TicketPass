# Continuity

## Current Project State

TicketPass is an early monorepo scaffold with a Next.js frontend, Spring Boot API, shared package placeholder, initial seller listing contract docs, and an approved authentication user story.

## Latest Completed Work

- Date: 2026-07-09
- GitHub Issue: `#2` - https://github.com/VietCT04/TicketPass/issues/2
- Summary: Defined the seller listing API contract, normalized `events` and `listings` database contract, listing statuses, transfer methods, security rules, and open concerns for platform-specific transferability.
- Files changed:
  - `docs/API.md`
  - `docs/DATABASE.md`
  - `docs/SECURITY.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

## Active Work

- Current GitHub Issue: None for authentication yet.
- Current goal: Create focused GitHub Issues from `US-0002: Authenticate User` before implementing seller-owned APIs.
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

1. Create focused GitHub Issues from `docs/user-stories/US-0002-authenticate-user.md`.
2. Implement the authentication foundation.
3. Return to GitHub Issue `#3` and derive `seller_id` from the authenticated user.
