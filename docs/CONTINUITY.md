# Continuity

## Current Project State

TicketPass is an early monorepo scaffold with a Next.js frontend, Spring Boot API, shared package placeholder, backend email/password auth with server-side opaque sessions, logout revocation, current-user session validation, authenticated seller listing creation, and frontend signup/login/logout screens.

## Latest Completed Work

- Date: 2026-07-12
- GitHub Issue: `#12` - https://github.com/VietCT04/TicketPass/issues/12
- Summary: Implemented frontend signup, login, logout, and current-user UI state using the existing cookie-backed backend auth contract. Added duplicate-submit prevention, signed-out handling for `GET /api/me` `401`, and `.tools/` git exclusion for local GitHub CLI binaries.
- Files changed:
  - `.gitignore`
  - `AGENTS.md`
  - `apps/web/eslint.config.mjs`
  - `apps/web/next-env.d.ts`
  - `apps/web/package.json`
  - `apps/web/src/app/page.tsx`
  - `apps/web/src/app/signup/page.tsx`
  - `apps/web/src/app/login/page.tsx`
  - `apps/web/src/components/AuthForm.tsx`
  - `apps/web/src/components/AuthStatus.tsx`
  - `apps/web/src/lib/auth.ts`
  - `apps/web/tsconfig.json`
  - `docs/SECURITY.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-11
- GitHub Issue: None - workflow update
- Summary: Updated `AGENTS.md` so implementation proposals use GitHub Issue comments as the approval and revision loop, with the conversation as a fallback only when GitHub is unavailable.
- Files changed:
  - `AGENTS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-11
- GitHub Issue: `#3` - https://github.com/VietCT04/TicketPass/issues/3
- Summary: Implemented authenticated seller listing creation with server-derived seller ownership, normalized event/listing persistence, `quantity = 1`, initial `ACTIVE` status, server-side validation, Flyway listing tables, and focused backend tests.
- Files changed:
  - `apps/api/src/main/java/com/ticketpass/api/auth/SecurityConfig.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/*`
  - `apps/api/src/main/resources/db/migration/V2__create_listing_tables.sql`
  - `apps/api/src/test/java/com/ticketpass/api/listing/*`
  - `docs/API.md`
  - `docs/DATABASE.md`
  - `docs/SECURITY.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-11
- GitHub Issue: `#14` - https://github.com/VietCT04/TicketPass/issues/14
- Summary: Documented the Spring Security `AuthenticatedUser` pattern for seller-owned APIs, including server-derived seller ownership, no client-provided ownership fields, no duplicate session parsing, and issue `#3` readiness.
- Files changed:
  - `docs/API.md`
  - `docs/SECURITY.md`
  - `docs/flows/SELLER_LISTING_FLOW.md`
  - `docs/CONTINUITY.md`

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

- Current GitHub Issue: `#12` - Build frontend signup login logout flow
- Current goal: Review and merge the issue `#12` pull request.
- Current blocker: None. Frontend lint and build passed locally.

## Important User Stories

- `docs/user-stories/US-0001-list-transferable-ticket.md`: Seller can list a transferable ticket safely without exposing sensitive ticket data too early.
- `docs/user-stories/US-0002-authenticate-user.md`: User can sign up, log in, log out, maintain secure sessions, and access protected TicketPass account features.

## Known Concerns

- See `docs/CONCERNS.md`.
- Password policy is defined for MVP but still needs review before public launch.
- Session cookie CSRF hardening needs review.
- Account recovery and verification features are deferred.
- Local verification requires Java 21; current Maven runtime uses Java 19 and cannot compile the project.
- MVP does not classify seller listing `public_notes` for sensitive ticket payload content.
- Platform-specific transferability rules are unresolved.
- Seller transferability confirmation is not proof.
- Event reuse and deduplication rules are not defined for MVP.

## Next Recommended Steps

1. Review the pull request for GitHub Issue `#12`.
2. Confirm frontend verification in CI.
3. After issue `#12` merges, continue with issue `#13` for protected frontend pages.
