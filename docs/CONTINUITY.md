# Continuity

## Current Project State

TicketPass is an early monorepo scaffold with a Next.js frontend, Spring Boot API, shared package placeholder, backend email/password auth with server-side opaque sessions, logout revocation, current-user session validation, authenticated seller listing creation, frontend signup/login/logout screens, a documented public event browse API contract, a documented authenticated seller event autocomplete API contract, and a documented event-linked listing creation contract requiring sellers to select an existing event before listing creation.

## Latest Completed Work

- Date: 2026-07-14
- GitHub Issue: None - workflow update
- Summary: Updated `AGENTS.md` GitHub Issues workflow to prevent reposting an approved proposal when the same approved proposal is already present in the issue comments. Future implementation notes should go in the linked PR or only add a short issue comment when new information is not already captured.
- Files changed:
  - `AGENTS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-14
- GitHub Issue: `#32` - https://github.com/VietCT04/TicketPass/issues/32
- Summary: Defined the docs-only `POST /api/listings` event-linked creation contract. Listing creation now submits `event_id` instead of seller-provided event identity fields, keeps `event_platform` at the listing/ticket level, requires server-side selected-event existence and future-start validation, prevents listing creation from modifying event records, and stores new MVP listings as `VND` with whole-dong `asking_price_minor` semantics. Backend and database implementation belongs to `#34`.
- Files changed:
  - `docs/API.md`
  - `docs/DATABASE.md`
  - `docs/SECURITY.md`
  - `docs/flows/SELLER_LISTING_FLOW.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-14
- GitHub Issue: `#31` - https://github.com/VietCT04/TicketPass/issues/31
- Summary: Defined the docs-only authenticated seller `GET /api/events/autocomplete` contract, including required auth, `q` validation, 10-result MVP limit, no pagination, 300 ms frontend debounce guidance, searchable event fields, deterministic ranking, future-event eligibility, safe response fields, and sensitive-data exclusions. Documented that backend implementation belongs to `#33` and frontend autocomplete belongs to `#35`.
- Files changed:
  - `docs/API.md`
  - `docs/DATABASE.md`
  - `docs/SECURITY.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-14
- GitHub Issue: None - issue creation and workflow alignment
- Summary: Created focused follow-up issues for `US-0004` covering the event autocomplete contract (`#31`), event-linked listing contract (`#32`), backend autocomplete implementation (`#33`), backend `event_id` listing implementation (`#34`), and frontend autocomplete selector (`#35`). Updated issue `#6` so the seller listing form is blocked until the required event-selection and listing-contract dependencies are complete.
- Files changed:
  - `docs/user-stories/US-0004-search-select-existing-event.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-14
- GitHub Issue: None - user story creation
- Summary: Added `US-0004` defining that sellers must search and select an existing event through autocomplete, listing creation must use `event_id`, free-text event creation is not accepted, and missing-event reporting is deferred.
- Files changed:
  - `docs/user-stories/US-0004-search-select-existing-event.md`

- Date: 2026-07-13
- GitHub Issue: `#25` - https://github.com/VietCT04/TicketPass/issues/25
- Summary: Defined the docs-only public `GET /api/events` contract for event-first browsing, including a single browse-eligible listing rule, 1-based pagination, deterministic ordering, VND-only MVP aggregates, nullable `image_url`, safe response fields, and current schema limitations.
- Files changed:
  - `docs/API.md`
  - `docs/DATABASE.md`
  - `docs/SECURITY.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-12
- GitHub Issue: None - issue creation
- Summary: Created focused GitHub Issues from approved user story `US-0003` for event-first marketplace browsing: API contract and visibility rules (`#25`), backend public browse events API (`#26`), and frontend browse events page (`#27`). Added unresolved US-0003 concerns for event lifecycle rules, aggregate freshness, and event image source/moderation.
- Files changed:
  - `docs/user-stories/US-0003-browse-events.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-12
- GitHub Issue: None - user story proposal
- Summary: Replaced the buyer browse listings proposal with an event-first browse events user story covering events with active visible listings, safe event summaries, optional server-derived listing aggregates, pagination, and server-side visibility enforcement.
- Files changed:
  - `docs/user-stories/US-0003-browse-events.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-12
- GitHub Issue: None - workflow update
- Summary: Updated `AGENTS.md` testing rules so backend and frontend test suites are not run after coding by default. Agents may still write or update tests, but test execution now requires an explicit user request. Non-test verification such as lint, build, typecheck, and formatting checks remains allowed when relevant.
- Files changed:
  - `AGENTS.md`
  - `docs/CONTINUITY.md`

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

- Current GitHub Issue: `#32` - https://github.com/VietCT04/TicketPass/issues/32
- Current goal: Review and merge the event-linked listing creation contract PR.
- Current blocker: Issue `#6` is blocked until `#32`, `#34`, and `#35` are complete. Issues `#34` and `#35` require their contract dependencies first.

## Important User Stories

- `docs/user-stories/US-0001-list-transferable-ticket.md`: Seller can list a transferable ticket safely without exposing sensitive ticket data too early.
- `docs/user-stories/US-0002-authenticate-user.md`: User can sign up, log in, log out, maintain secure sessions, and access protected TicketPass account features.
- `docs/user-stories/US-0003-browse-events.md`: Buyer can browse events that have active publicly visible ticket listings with safe event summaries and basic pagination.
- `docs/user-stories/US-0004-search-select-existing-event.md`: Seller must search and select an existing event through autocomplete, and listing creation must reference that event through `event_id`.

## Known Concerns

- See `docs/CONCERNS.md`.
- Password policy is defined for MVP but still needs review before public launch.
- Session cookie CSRF hardening needs review.
- Account recovery and verification features are deferred.
- Local verification requires Java 21; current Maven runtime uses Java 19 and cannot compile the project.
- MVP does not classify seller listing `public_notes` for sensitive ticket payload content.
- Platform-specific transferability rules are unresolved.
- Seller transferability confirmation is not proof.
- Existing duplicate events may appear as separate autocomplete results until deduplication rules are implemented.
- Event cancellation and rescheduling rules are not defined for browse or seller selection.
- Browse event aggregate freshness rules need review.
- Event image source and moderation rules are not defined.
- Listing creation is documented as VND-only for MVP, but backend/database implementation remains pending in `#34`.
- Sellers cannot list tickets for missing events until a later event request/reporting user story is defined.
- Event autocomplete query performance may require indexes or a dedicated search strategy after issue `#33` implementation review.
- `event_platform` is documented as listing/ticket-specific, but schema and implementation migration remains pending in `#34`.

## Next Recommended Steps

1. Merge the issue `#32` contract PR after review.
2. Implement issue `#33` for the authenticated event autocomplete backend endpoint.
3. Implement issue `#34` after the issue `#32` contract PR is merged.
4. Implement the frontend event selector in `#35`.
5. Unblock and implement the seller listing form in `#6`.
