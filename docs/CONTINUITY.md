# Continuity

## Current Project State

TicketPass is an early monorepo scaffold with a Next.js frontend, Spring Boot API, shared package placeholder, seller listing contract/flow docs, and an approved authentication user story.

## Latest Completed Work

- Date: 2026-07-10
- GitHub Issue: `#7` - https://github.com/VietCT04/TicketPass/issues/7
- Summary: Documented the seller listing flow, public metadata rules, server-side validation expectations, duplicate-sale relationship, audit expectations, and security boundaries.
- Files changed:
  - `docs/flows/SELLER_LISTING_FLOW.md`
  - `docs/API.md`
  - `docs/DATABASE.md`
  - `docs/SECURITY.md`
  - `docs/CONTINUITY.md`

## Active Work

- Current GitHub Issue: `#9` / `#10` auth work is in review on separate PRs.
- Current goal: Review/merge auth contract and backend signup/login PRs before implementing seller-owned APIs.
- Current blocker: Seller listing API issue `#3` should wait until authentication is implemented and accepted.

## Important User Stories

- `docs/user-stories/US-0001-list-transferable-ticket.md`: Seller can list a transferable ticket safely without exposing sensitive ticket data too early.
- `docs/user-stories/US-0002-authenticate-user.md`: User can sign up, log in, log out, maintain secure sessions, and access protected TicketPass account features.

## Known Concerns

- See `docs/CONCERNS.md`.
- Platform-specific transferability rules are unresolved.
- Seller transferability confirmation is not proof.
- Event reuse and deduplication rules are not defined for MVP.

## Next Recommended Steps

1. Review and merge the auth contract and signup/login PRs.
2. Continue auth work with GitHub Issue `#11`.
3. Return to GitHub Issue `#3` and derive `seller_id` from the authenticated user.
