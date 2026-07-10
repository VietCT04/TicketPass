# Continuity

## Current Project State

TicketPass is an early monorepo scaffold with a Next.js frontend, Spring Boot API, shared package placeholder, seller listing contract, flow, and status docs, and backend email/password auth with server-side opaque sessions, logout revocation, and current-user session validation.

## Latest Completed Work

- Date: 2026-07-11
- GitHub Issue: `#11` - https://github.com/VietCT04/TicketPass/issues/11
- Summary: Implemented session cookie validation through Spring Security, immutable current-user principals, protected `GET /api/me`, idempotent logout with `revoked_at`, centralized cookie clearing, API-style `401` responses, focused auth tests, and related docs.
- Files changed:
  - `apps/api/pom.xml`
  - `apps/api/src/main/java/com/ticketpass/api/auth/*`
  - `apps/api/src/test/java/com/ticketpass/api/auth/*`
  - `AGENTS.md`

- Date: 2026-07-10
- GitHub Issue: `#7` - https://github.com/VietCT04/TicketPass/issues/7
- Summary: Documented the seller listing flow, public metadata rules, server-side validation expectations, duplicate-sale relationship, audit expectations, and security boundaries.
- Files changed:
  - `docs/flows/SELLER_LISTING_FLOW.md`

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

- Current GitHub Issue: Auth backend work is ready for review.
- Current goal: Review and merge the auth backend branch before building seller-owned APIs.
- Current blocker: Seller listing API issue `#3` should wait until authentication is merged into `main`.

## Important User Stories

- `docs/user-stories/US-0001-list-transferable-ticket.md`: Seller can list a transferable ticket safely without exposing sensitive ticket data too early.
- `docs/user-stories/US-0002-authenticate-user.md`: User can sign up, log in, log out, maintain secure sessions, and access protected TicketPass account features.

## Known Concerns

- See `docs/CONCERNS.md`.
- Password policy is defined for MVP but still needs review before public launch.
- Session cookie CSRF hardening needs review.
- Account recovery and verification features are deferred.
- Local verification requires Java 21; current Maven runtime uses Java 19 and cannot compile the project.
- Platform-specific transferability rules are unresolved.
- Seller transferability confirmation is not proof.
- Event reuse and deduplication rules are not defined for MVP.

## Next Recommended Steps

1. Run backend tests with Java 21: `mvn test` from `apps/api`.
2. Review and merge the auth backend work.
3. Continue with GitHub Issue `#14` to lock in the seller-owned API current-user pattern, then return to GitHub Issue `#3`.
