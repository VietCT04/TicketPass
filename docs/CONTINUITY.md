# Continuity

## Current Project State

TicketPass is an early monorepo scaffold with a Next.js frontend, Spring Boot API, shared package placeholder, an authentication contract based on email/password plus server-side opaque sessions, and seller listing contract/status docs.

## Latest Completed Work

- Date: 2026-07-10
- GitHub Issue: `#4` - https://github.com/VietCT04/TicketPass/issues/4
- Summary: Documented listing status meanings, allowed transitions, terminal statuses, duplicate-sale prevention invariants, and implementation expectations.
- Files changed:
  - `docs/flows/LISTING_STATUS_FLOW.md`
  - `docs/API.md`
  - `docs/DATABASE.md`
  - `docs/SECURITY.md`
  - `docs/CONTINUITY.md`

## Active Work

- Current GitHub Issue: Auth work is in review on separate PRs.
- Current goal: Review/merge auth contract and backend signup/login PRs before implementing seller-owned APIs.
- Current blocker: Seller listing API issue `#3` should wait until authentication is implemented and accepted.

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

1. Review and merge the auth contract and signup/login PRs.
2. Continue auth work with GitHub Issue `#11`.
3. Return to GitHub Issue `#3` and derive `seller_id` from the authenticated user.
