# Continuity

## Current Project State

TicketPass is an early monorepo scaffold with a Next.js frontend, Spring Boot API, shared package placeholder, backend email/password auth with server-side opaque sessions, logout revocation, current-user session validation, a protected frontend `/sell` route, authenticated seller listing creation with a minimal `LISTING_CREATED` audit event, a backend authenticated seller event autocomplete endpoint, backend event-linked listing creation that requires an existing future event, a backend public event browse API, a backend public event-detail API, a frontend homepage event browse page, a frontend `/sell` event selector and seller listing form, frontend signup/login/logout screens, and an approved buyer reservation user story with a focused API/data-contract issue.

## Latest Completed Work

- Date: 2026-07-16
- GitHub Issue: `#53` - https://github.com/VietCT04/TicketPass/issues/53
- Summary: Added `US-0006` for authenticated buyers to place a server-controlled 10-minute hold on one available listing before checkout. Created and approved the focused docs-only reservation contract issue covering `POST /api/listings/{listingId}/reservations`, separate reservation persistence, seller self-reservation prevention, atomic `ACTIVE -> RESERVED` concurrency behavior, idempotent same-buyer retries without expiry extension, automatic expiration back to `ACTIVE`, safe responses, and deferred payment, escrow, sale completion, reveal, manual release, and broader audit events.
- Files changed:
  - `docs/user-stories/US-0006-reserve-available-ticket-listing.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-15
- GitHub Issue: `#45` - https://github.com/VietCT04/TicketPass/issues/45
- Summary: Implemented the backend public `GET /api/events/{eventId}` event-detail endpoint. The endpoint explicitly parses malformed event IDs into controlled `400` responses, treats missing and no-longer-upcoming events as public `404 Event not found`, captures a single request timestamp, returns an upcoming event even when it has zero browse-eligible listings, pages listing summaries with the shared public pagination parser, orders listings by price, creation time, and ID in the database, reuses the public browse-eligible listing predicate, keeps `image_url` as `null`, and exposes only safe listing fields without seller identity, `public_notes`, internal status, timestamps, transferability confirmation, or ticket payload data.
- Files changed:
  - `apps/api/src/main/java/com/ticketpass/api/auth/SecurityConfig.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/EventDetailController.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/EventDetailResponse.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/EventDetailService.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/EventListingSummaryRow.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/EventBrowseService.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/EventRepository.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/ListingRepository.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/PublicListingEligibility.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/PublicPagination.java`
  - `docs/SECURITY.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-15
- GitHub Issue: `#13` - https://github.com/VietCT04/TicketPass/issues/13
- Summary: Protected the existing `/sell` frontend route with a reusable client-side auth guard that verifies sessions through `GET /api/me`, redirects signed-out users to `/login?next=/sell` with `router.replace(...)`, blocks the seller listing form from mounting before authentication succeeds, shows a generic retryable error state for unexpected session-check failures, and updates login/signup to honor only safe `next=/sell` redirects before falling back to `/`.
- Files changed:
  - `apps/web/src/components/RequireAuth.tsx`
  - `apps/web/src/lib/redirects.ts`
  - `apps/web/src/app/sell/page.tsx`
  - `apps/web/src/app/login/page.tsx`
  - `apps/web/src/app/signup/page.tsx`
  - `apps/web/src/components/AuthForm.tsx`
  - `apps/web/src/components/SellerListingForm.tsx`
  - `docs/SECURITY.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-15
- GitHub Issue: `#5` - https://github.com/VietCT04/TicketPass/issues/5
- Summary: Added the initial backend audit foundation for seller listing creation. The new `audit_events` table records immutable `LISTING_CREATED` events with actor user ID, entity type `LISTING`, created listing ID, and a server-generated timestamp. Listing creation now writes the listing and audit event in the same transaction, with no request bodies, seller contact data, public notes, seat details, pricing, ticket payload data, credentials, or session data stored in audit rows.
- Files changed:
  - `apps/api/src/main/java/com/ticketpass/api/audit/AuditAction.java`
  - `apps/api/src/main/java/com/ticketpass/api/audit/AuditEntityType.java`
  - `apps/api/src/main/java/com/ticketpass/api/audit/AuditEventEntity.java`
  - `apps/api/src/main/java/com/ticketpass/api/audit/AuditEventRepository.java`
  - `apps/api/src/main/java/com/ticketpass/api/audit/AuditService.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/ListingService.java`
  - `apps/api/src/main/resources/db/migration/V3__create_audit_events.sql`
  - `docs/DATABASE.md`
  - `docs/SECURITY.md`
  - `docs/flows/SELLER_LISTING_FLOW.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-15
- GitHub Issue: `#44` - https://github.com/VietCT04/TicketPass/issues/44
- Summary: Defined the docs-only public `GET /api/events/{eventId}` contract for opening an event and viewing its currently browse-eligible listings. The contract returns the event summary and paginated listing summaries in one response, reuses the public browse-eligible listing rule, keeps listing ordering deterministic, documents no-longer-upcoming events as `404`, excludes seller/private/ticket payload data, and states that the response is a current marketplace snapshot rather than a reservation guarantee. Backend implementation belongs to `#45`; frontend implementation belongs to `#46`.
- Files changed:
  - `docs/API.md`
  - `docs/SECURITY.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-15
- GitHub Issue: `#27` - https://github.com/VietCT04/TicketPass/issues/27
- Summary: Implemented the frontend public event browse page on `/` using the approved `GET /api/events` contract. The page server-fetches event summaries with `cache: "no-store"`, normalizes invalid page query values to page 1, redirects pages above the final valid page, renders empty/error/success states, adds loading skeletons, shows only safe event fields and server-derived aggregates, keeps event cards non-clickable, keeps the account status section secondary, and removes the placeholder API base URL card.
- Files changed:
  - `apps/web/src/app/page.tsx`
  - `apps/web/src/app/loading.tsx`
  - `apps/web/src/components/EventBrowseCard.tsx`
  - `apps/web/src/components/EventBrowsePagination.tsx`
  - `apps/web/src/components/EventBrowseSkeleton.tsx`
  - `apps/web/src/components/EventDateTime.tsx`
  - `apps/web/src/lib/events.ts`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-14
- GitHub Issue: `#26` - https://github.com/VietCT04/TicketPass/issues/26
- Summary: Implemented the public backend `GET /api/events` browse endpoint. The endpoint defaults to 1-based pagination, rejects invalid pagination with controlled API errors, uses the injected `Clock`, runs a database-side grouped query over active future VND listings, returns safe event summaries with query-time lowest-price and available-listing aggregates, keeps `image_url` as `null`, and explicitly marks the route public in API security configuration.
- Files changed:
  - `apps/api/src/main/java/com/ticketpass/api/auth/SecurityConfig.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/EventBrowseController.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/EventBrowseResponse.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/EventBrowseRow.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/EventBrowseService.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/EventRepository.java`
  - `docs/API.md`
  - `docs/SECURITY.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-14
- GitHub Issue: None - user story and issue creation
- Pull Request: `#43` - https://github.com/VietCT04/TicketPass/pull/43
- Summary: Added `US-0005` for the public buyer flow to open an event and compare its currently available ticket listings. Created focused follow-up issues for the API contract (`#44`), backend implementation (`#45`), and frontend event-detail page (`#46`). The story keeps listing cards read-only, uses the shared browse-eligibility rule, excludes seller identity and `public_notes`, and defers reservation and checkout.
- Files changed:
  - `docs/user-stories/US-0005-view-available-listings-for-event.md`
- GitHub Issue: `#6` - https://github.com/VietCT04/TicketPass/issues/6
- Summary: Implemented the frontend seller listing form on `/sell`. The form reuses the event autocomplete selector, requires a selected `event_id`, collects listing-specific fields only, submits fixed `transfer_method = PLATFORM_TRANSFER`, treats `VND` as fixed MVP currency, warns sellers not to put sensitive ticket payload data in public notes, preserves form state on failures, handles `401` with a login link, shows same-page success with the created listing ID and summary, and adds a create-another-listing reset action.
- Files changed:
  - `apps/web/src/app/sell/page.tsx`
  - `apps/web/src/components/AuthStatus.tsx`
  - `apps/web/src/components/SellerListingForm.tsx`
  - `apps/web/src/lib/listings.ts`
  - `docs/flows/SELLER_LISTING_FLOW.md`
  - `docs/SECURITY.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-14
- GitHub Issue: `#35` - https://github.com/VietCT04/TicketPass/issues/35
- Summary: Implemented the frontend seller event autocomplete selector on `/sell`. The selector calls the authenticated autocomplete endpoint with included credentials, trims queries, waits for three characters, debounces requests, cancels stale searches, supports keyboard and mouse selection, exposes the selected event summary and `event_id`, clears selection when typing changes, and shows guidance, loading, empty, error, unauthenticated, and selected-event states.
- Files changed:
  - `apps/web/src/app/sell/page.tsx`
  - `apps/web/src/components/EventAutocompleteSelector.tsx`
  - `apps/web/src/lib/events.ts`
  - `docs/flows/SELLER_LISTING_FLOW.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-14
- GitHub Issue: `#34` - https://github.com/VietCT04/TicketPass/issues/34
- Summary: Implemented backend event-linked seller listing creation. `POST /api/listings` now accepts `event_id`, rejects legacy seller-provided event identity fields and `currency`, resolves an existing future event, never creates or modifies events during listing creation, stores listing-level `event_platform`, and persists new MVP listings as `VND`. Updated the unapplied `V2` listing migration directly and aligned docs/concerns/continuity. No backend or frontend tests were run per the approved issue decision; local test-source compilation remains blocked by the Java 19 runtime versus the project Java 21 target.
- Files changed:
  - `apps/api/src/main/java/com/ticketpass/api/common/ApiExceptionHandler.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/CreateListingRequest.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/EventEntity.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/ListingController.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/ListingEntity.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/ListingResponse.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/ListingService.java`
  - `apps/api/src/main/resources/db/migration/V2__create_listing_tables.sql`
  - `apps/api/src/test/java/com/ticketpass/api/listing/ListingControllerTest.java`
  - `apps/api/src/test/java/com/ticketpass/api/listing/ListingServiceTest.java`
  - `docs/API.md`
  - `docs/DATABASE.md`
  - `docs/SECURITY.md`
  - `docs/flows/SELLER_LISTING_FLOW.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-14
- GitHub Issue: `#33` - https://github.com/VietCT04/TicketPass/issues/33
- Summary: Implemented authenticated `GET /api/events/autocomplete` for seller event selection. The endpoint trims and validates `q`, requires 3 to 100 characters, returns at most 10 future events, performs database-side case-insensitive substring matching over event name, venue, and city with deterministic ranking, uses the injected `Clock`, and returns safe event summaries without `event_platform`.
- Files changed:
  - `apps/api/src/main/java/com/ticketpass/api/auth/SecurityConfig.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/EventAutocompleteController.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/EventAutocompleteResponse.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/EventAutocompleteService.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/EventRepository.java`
  - `docs/API.md`
  - `docs/SECURITY.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

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

- Current GitHub Issue: `#45` - https://github.com/VietCT04/TicketPass/issues/45
- Current goal: Review and merge the public event-detail backend API pull request.
- Current blocker: None.

## Important User Stories

- `docs/user-stories/US-0001-list-transferable-ticket.md`: Seller can list a transferable ticket safely without exposing sensitive ticket data too early.
- `docs/user-stories/US-0002-authenticate-user.md`: User can sign up, log in, log out, maintain secure sessions, and access protected TicketPass account features.
- `docs/user-stories/US-0003-browse-events.md`: Buyer can browse events that have active publicly visible ticket listings with safe event summaries and basic pagination.
- `docs/user-stories/US-0004-search-select-existing-event.md`: Seller must search and select an existing event through autocomplete, and listing creation must reference that event through `event_id`.
- `docs/user-stories/US-0005-view-available-listings-for-event.md`: Buyer can open an event and compare its currently browse-eligible ticket listings without exposing seller identity or sensitive ticket payload data.
- `docs/user-stories/US-0006-reserve-available-ticket-listing.md`: Authenticated buyer can place a server-controlled 10-minute hold on one available listing before checkout, with atomic duplicate-sale prevention and automatic expiration.

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
- Event image source and moderation rules are not defined; the issue `#27` frontend uses a neutral local placeholder only.
- Sellers cannot list tickets for missing events until a later event request/reporting user story is defined.
- Event autocomplete query performance may require indexes or a dedicated search strategy after production-volume review.
- Event local timezone preservation and display rules are unresolved.
- Listing availability can change between event-detail page load and a future reservation attempt; `GET /api/events/{eventId}` is only a marketplace snapshot.
- Reservation concurrency and expiration cleanup mechanisms remain to be defined and implemented after issue `#53` documents the contract.
- Audit retention, deletion, export, and compliance rules are not defined.

## Next Recommended Steps

1. Merge the issue `#45` public event-detail backend API pull request after review.
2. Implement the docs-only buyer reservation contract in issue `#53`.
3. Build the public event-detail frontend page in issue `#46` after `#45` is available.
