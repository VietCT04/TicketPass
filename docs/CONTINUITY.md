# Continuity

## Current Project State

TicketPass is an early monorepo scaffold with a Next.js frontend, Spring Boot API, shared package placeholder, initial seller listing contract docs, and an authentication contract based on email/password plus server-side opaque sessions.

## Latest Completed Work

- Date: 2026-07-10
- GitHub Issue: `#9` - https://github.com/VietCT04/TicketPass/issues/9
- Summary: Defined the authentication API contract, user/session database contract, opaque session strategy, security rules, and auth concerns for `US-0002`; corrected `AGENTS.md` so future GitHub Issue comments preserve the approved proposal as the source of truth.
- Files changed:
  - `AGENTS.md`
  - `docs/API.md`
  - `docs/DATABASE.md`
  - `docs/SECURITY.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

## Active Work

- Current GitHub Issue: `#10` - Implement backend signup and login API
- Current goal: Implement backend signup and login using the documented auth contract from issue `#9`.
- Current blocker: Seller listing API issue `#3` should wait until authentication is implemented.

## Important User Stories

- `docs/user-stories/US-0001-list-transferable-ticket.md`: Seller can list a transferable ticket safely without exposing sensitive ticket data too early.
- `docs/user-stories/US-0002-authenticate-user.md`: User can sign up, log in, log out, maintain secure sessions, and access protected TicketPass account features.

## Known Concerns

- See `docs/CONCERNS.md`.
- Password policy needs review.
- Session cookie CSRF hardening needs review.
- Account recovery and verification features are deferred.
- Platform-specific transferability rules are unresolved.
- Seller transferability confirmation is not proof.
- Event reuse and deduplication rules are not defined for MVP.

## Next Recommended Steps

1. Implement GitHub Issue `#10` using the auth API and database contract from `docs/API.md` and `docs/DATABASE.md`.
2. Implement GitHub Issues `#11` through `#14` in order.
3. Return to GitHub Issue `#3` and derive `seller_id` from the authenticated user.
