# Continuity

## Current Project State

TicketPass is an early monorepo scaffold with authenticated seller listings, event browsing/detail, buyer reservations, protected browser checkout recovery, authenticated missing-event requests from the `/sell` fallback, and authenticated seller own-listings API backed by a mock hosted payment provider. Mock payment events are delivered through signed HTTP webhooks and an atomic receipt ledger; verified, timely payment success completes the order and sells the reserved listing. Checkout reconciliation handles trusted failure, cancellation, and expiry without releasing inventory when payment requires manual action, and authenticated buyers can reload their safe server-authoritative order state. The buyer order-progress contract now defines a read-only, server-owned account-history view with separate payment, transfer, and settlement dimensions; its persistence and backend implementation remain future issues. The mock lifecycle is fail-closed at the route boundary, startup-validated, and bounded for webhook input and network delivery. Issue `#84` adds the protected seller own-listings page.

## Latest Completed Work

- Date: 2026-07-19
- GitHub Issue: `#87` - https://github.com/VietCT04/TicketPass/issues/87
- Summary: Defined the authenticated, read-only buyer order-progress contract for `GET /api/me/orders`, including database-side ownership/filtering/pagination, deterministic ordering, separate payment/transfer/settlement progress, bounded server-derived buyer actions, snapshot freshness behavior, safe response fields, and no-store privacy controls. No endpoint, persistence, or frontend implementation was added.
- Files changed:
  - `docs/API.md`
  - `docs/SECURITY.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-19
- GitHub Issue: `#83` - https://github.com/VietCT04/TicketPass/issues/83
- Summary: Implemented authenticated read-only `GET /api/me/listings`. The endpoint derives seller ownership from the session principal, uses database-side owner and optional exact status filtering before pagination, joins only listing event metadata, returns explicit safe DTOs in deterministic newest-first order, sends `Cache-Control: no-store`, and performs no marketplace reconciliation, locking, writes, or audit creation.
- Files changed:
  - `apps/api/src/main/java/com/ticketpass/api/auth/SecurityConfig.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/ListingRepository.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/SellerOwnListingController.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/SellerOwnListingQueryParser.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/SellerOwnListingResponse.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/SellerOwnListingPageResponse.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/SellerOwnListingRow.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/SellerOwnListingService.java`
  - `docs/API.md`
  - `docs/SECURITY.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-19
- GitHub Issue: `#79` - https://github.com/VietCT04/TicketPass/issues/79
- Summary: Added the authenticated `/sell` missing-event fallback. An empty autocomplete result opens an inline, non-nested controlled request panel that validates bounded metadata and an explicit UTC offset, submits `POST /api/event-requests` with credentials, preserves fields on controlled failures, and shows a safe pending confirmation. A request never becomes an event selection or listing, and listing submission remains disabled without a real selected event.
- Files changed:
  - `apps/web/src/components/EventAutocompleteSelector.tsx`
  - `apps/web/src/components/MissingEventRequestPanel.tsx`
  - `apps/web/src/components/SellerListingForm.tsx`
  - `apps/web/src/lib/event-requests.ts`
  - `docs/flows/SELLER_LISTING_FLOW.md`
  - `docs/SECURITY.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-18
- GitHub Issue: `#78` - https://github.com/VietCT04/TicketPass/issues/78
- Summary: Implemented authenticated `POST /api/event-requests` with server-derived requester ownership and time, strict offset-bearing future-time and HTTPS URL validation, Unicode-aware text normalization, requester-scoped database-backed pending duplicate recovery, safe no-store responses, and a strict boundary from the event catalogue and listing creation. Seller UI remains `#79`; catalogue review and event insertion remain future work.
- Files changed:
  - `apps/api/src/main/java/com/ticketpass/api/eventrequest/*`
  - `apps/api/src/main/java/com/ticketpass/api/auth/SecurityConfig.java`
  - `apps/api/src/main/resources/db/migration/V9__create_event_requests.sql`
  - `docs/API.md`
  - `docs/DATABASE.md`
  - `docs/SECURITY.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-18
- GitHub Issue: `#82` - https://github.com/VietCT04/TicketPass/issues/82
- Summary: Defined the authenticated seller own-listings API contract, including ownership, 1-based pagination, exact status filtering, deterministic ordering, safe DTO boundaries, empty-page behavior, logging, and deferred mutation/performance work. Backend implementation remains `#83`; the protected seller page remains `#84`.
- Files changed:
  - `docs/API.md`
  - `docs/SECURITY.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-18
- GitHub Issue: `#77` - https://github.com/VietCT04/TicketPass/issues/77
- Summary: Defined the documentation-only authenticated missing-event request contract for `POST /api/event-requests`. The future endpoint uses server-derived requester ownership, offset-bearing future timestamps, bounded and normalized request metadata, requester-scoped database-backed pending duplicate recovery, safe responses, and a strict boundary preventing pending requests from creating events or bypassing listing creation. Backend implementation remains `#78`; seller UI remains `#79`.
- Files changed:
  - `docs/API.md`
  - `docs/DATABASE.md`
  - `docs/SECURITY.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-18
- GitHub Issue: `#71` - https://github.com/VietCT04/TicketPass/issues/71
- Summary: Added the protected buyer `/checkout/{orderId}` recovery route and strict checkout/order API client. Active reservations now expose an explicit checkout action; valid hosted-payment URLs remain ephemeral and redirect only after controlled responses. The checkout UI reloads only safe server-authoritative order data, treats provider-return values and countdowns as non-authoritative presentation context, bounds visible-page return polling, and clearly keeps ticket delivery/reveal and seller payout out of the paid state.
- Files changed:
  - `apps/web/src/app/checkout/[orderId]/page.tsx`
  - `apps/web/src/components/CheckoutPanel.tsx`
  - `apps/web/src/components/EventListingCard.tsx`
  - `apps/web/src/components/RequireAuth.tsx`
  - `apps/web/src/lib/checkout.ts`
  - `apps/web/src/lib/redirects.ts`
  - `docs/flows/BUYER_CHECKOUT_FLOW.md`, `docs/flows/BUYER_RESERVATION_FLOW.md`, `docs/SECURITY.md`, `docs/CONCERNS.md`, `docs/user-stories/US-0007-complete-checkout-for-reserved-ticket.md`, `docs/CONTINUITY.md`

- Date: 2026-07-18
- GitHub Issue: `#70` - https://github.com/VietCT04/TicketPass/issues/70
- Summary: Hardened the mock checkout lifecycle with deny-by-default route authorization, exact webhook browser-security exclusions, immutable validated payment configuration, external webhook-secret enforcement, safe URI construction, no-store checkout responses, hosted-page headers, webhook size/header limits, and claim/call/finalize mock delivery. No schema migration or generic audit event was added.
- Files changed:
  - `apps/api/src/main/java/com/ticketpass/api/{auth,config,payment}/*`
  - `apps/api/src/main/resources/application.yml`
  - `docs/API.md`, `docs/SECURITY.md`, `docs/DATABASE.md`, `docs/CONCERNS.md`, `docs/user-stories/US-0007-complete-checkout-for-reserved-ticket.md`, `docs/CONTINUITY.md`

- Date: 2026-07-17
- GitHub Issue: `#69` - https://github.com/VietCT04/TicketPass/issues/69
- Summary: Implemented bounded checkout reconciliation for deferred failure/cancellation receipts and expired unpaid orders, preserving the listing -> reservation -> order -> payment-session lock order. Generic reservation expiry now excludes reservations with orders. Added protected buyer-only `GET /api/orders/{orderId}` with request-time reconciliation and a shared safe order response used by checkout start and order reads. Unresolved `REQUIRES_ACTION` receipts block automated state changes and inventory release.
- Files changed:
  - `apps/api/src/main/java/com/ticketpass/api/payment/webhook/CheckoutReconciliationService.java`
  - `apps/api/src/main/java/com/ticketpass/api/payment/webhook/CheckoutReconciliationScheduler.java`
  - `apps/api/src/main/java/com/ticketpass/api/payment/webhook/PaymentWebhookReceiptRepository.java`
  - `apps/api/src/main/java/com/ticketpass/api/order/OrderController.java`
  - `apps/api/src/main/java/com/ticketpass/api/order/OrderReadService.java`
  - `apps/api/src/main/java/com/ticketpass/api/order/SafeOrderResponse.java`
  - `apps/api/src/main/java/com/ticketpass/api/order/SafeOrderResponseService.java`
  - `apps/api/src/main/java/com/ticketpass/api/{auth,listing,order,payment}/*`
  - `apps/api/src/main/resources/application.yml`
  - `apps/api/src/main/resources/db/migration/V8__add_checkout_reconciliation_indexes.sql`
  - `docs/API.md`, `docs/DATABASE.md`, `docs/SECURITY.md`, `docs/flows/LISTING_STATUS_FLOW.md`, `docs/CONCERNS.md`, `docs/user-stories/US-0007-complete-checkout-for-reserved-ticket.md`, `docs/CONTINUITY.md`

- Date: 2026-07-17
- GitHub Issue: `#68` - https://github.com/VietCT04/TicketPass/issues/68
- Summary: Implemented signed mock-provider HTTP delivery, bounded outbox retry/dead-letter handling, public raw-body webhook verification, atomic receipt deduplication, and server-validated successful payment completion from payment session through `PAID` order and `SOLD` listing. Failure/cancellation receipts are deferred, and late or inconsistent success is retained for action without changing inventory.
- Files changed:
  - `apps/api/src/main/java/com/ticketpass/api/payment/webhook/*`
  - `apps/api/src/main/java/com/ticketpass/api/payment/mock/*`
  - `apps/api/src/main/java/com/ticketpass/api/payment/PaymentSessionRepository.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/*Repository.java`
  - `apps/api/src/main/java/com/ticketpass/api/order/OrderRepository.java`
  - `apps/api/src/main/java/com/ticketpass/api/auth/SecurityConfig.java`
  - `apps/api/src/main/resources/db/migration/V7__add_mock_webhook_delivery_and_receipts.sql`
  - `apps/api/src/main/resources/application.yml`
  - `docs/API.md`, `docs/DATABASE.md`, `docs/SECURITY.md`, `docs/flows/LISTING_STATUS_FLOW.md`, `docs/CONCERNS.md`, `docs/user-stories/US-0007-complete-checkout-for-reserved-ticket.md`, `docs/CONTINUITY.md`

- Date: 2026-07-17
- GitHub Issue: `#67` - https://github.com/VietCT04/TicketPass/issues/67
- Summary: Implemented authenticated create-or-return checkout with provider-neutral payment interfaces and the approved in-application mock hosted provider. Added V6 payment-session, mock-provider-session, and durable mock-event persistence; locked checkout preparation; provider calls outside database locks; safe checkout responses; and public mock provider actions that create provider events without changing TicketPass orders or listings. Production-provider selection, webhook delivery, sale completion, and checkout UI remain deferred.
- Files changed:
  - `apps/api/src/main/java/com/ticketpass/api/payment/*`
  - `apps/api/src/main/java/com/ticketpass/api/payment/mock/*`
  - `apps/api/src/main/java/com/ticketpass/api/auth/SecurityConfig.java`
  - `apps/api/src/main/java/com/ticketpass/api/common/ApiExceptionHandler.java`
  - `apps/api/src/main/java/com/ticketpass/api/order/OrderRepository.java`
  - `apps/api/src/main/resources/application.yml`
  - `apps/api/src/main/resources/db/migration/V6__create_payment_sessions.sql`
  - `docs/API.md`
  - `docs/DATABASE.md`
  - `docs/SECURITY.md`
  - `docs/CONCERNS.md`
  - `docs/user-stories/US-0007-complete-checkout-for-reserved-ticket.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-17
- GitHub Issue: `#66` - https://github.com/VietCT04/TicketPass/issues/66
- Summary: Implemented provider-neutral checkout order persistence. Flyway migration `V5` creates the bounded `orders` table with non-cascading foreign keys, one-order-per-reservation uniqueness, VND and payment-state constraints, expiry and paid-timestamp checks, and only buyer and pending-expiry indexes. Added the dedicated `order` entity, status enum, and repository. No checkout endpoints, provider fields, hosted sessions, webhooks, lifecycle transitions, frontend behavior, payment audit events, or tests were added.
- Files changed:
  - `apps/api/src/main/resources/db/migration/V5__create_orders.sql`
  - `apps/api/src/main/java/com/ticketpass/api/order/OrderEntity.java`
  - `apps/api/src/main/java/com/ticketpass/api/order/OrderStatus.java`
  - `apps/api/src/main/java/com/ticketpass/api/order/OrderRepository.java`
  - `docs/DATABASE.md`
  - `docs/SECURITY.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-16
- GitHub Issue: `#65` - https://github.com/VietCT04/TicketPass/issues/65
- Summary: Defined the provider-neutral checkout and order contract for `US-0007`. The documentation specifies authenticated checkout start and protected order reads, one order per reservation, safe buyer-only responses, inherited reservation expiry, bounded order states, controlled privacy errors, trusted server-to-server payment authority, atomic sale completion, guarded inventory release, late-payment handling, and deferred provider and payment-operation responsibilities. No application, database migration, payment provider, webhook, or frontend checkout implementation was added.
- Files changed:
  - `docs/API.md`
  - `docs/DATABASE.md`
  - `docs/SECURITY.md`
  - `docs/flows/LISTING_STATUS_FLOW.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-16
- GitHub Issue: `#57` - https://github.com/VietCT04/TicketPass/issues/57
- Summary: Implemented the browser reservation action on each public event-detail listing card. The client submits the existing cookie-authenticated reservation request directly, handles `201` and idempotent `200` holds only after validating safe response fields, shows an inline server-expiry countdown, refreshes stale inventory after conflicts or expiry, and preserves a strictly validated event-detail return route for login/signup after `401`. Reservation state intentionally remains in memory; no checkout, payment, seller contact, ticket reveal, browser storage, or new backend endpoint was added.
- Files changed:
  - `apps/web/src/app/events/[eventId]/page.tsx`
  - `apps/web/src/components/EventListingCard.tsx`
  - `apps/web/src/lib/redirects.ts`
  - `apps/web/src/lib/reservations.ts`
  - `docs/flows/BUYER_RESERVATION_FLOW.md`
  - `docs/SECURITY.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-16
- GitHub Issue: `#56` - https://github.com/VietCT04/TicketPass/issues/56
- Summary: Implemented same-site MVP CSRF hardening for unsafe cookie-authenticated API requests. A validated configurable trusted-origin policy now backs both credentialed CORS and a focused `OncePerRequestFilter`. It requires exact normalized `Origin`, or `Referer` only when `Origin` is absent, and returns controlled `403` JSON errors without exposing the configured allowlist. The opaque `HttpOnly`, `SameSite=Lax` session model and frontend `credentials: "include"` requests remain unchanged.
- Files changed:
  - `apps/api/src/main/java/com/ticketpass/api/auth/CsrfOriginValidationFilter.java`
  - `apps/api/src/main/java/com/ticketpass/api/auth/SecurityConfig.java`
  - `apps/api/src/main/java/com/ticketpass/api/config/CorsConfig.java`
  - `apps/api/src/main/java/com/ticketpass/api/config/TrustedOriginPolicy.java`
  - `apps/api/src/main/resources/application.yml`
  - `docs/API.md`
  - `docs/SECURITY.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-16
- GitHub Issue: `#55` - https://github.com/VietCT04/TicketPass/issues/55
- Summary: Implemented database-backed reservation expiration. A configurable 60-second fixed-delay scheduler selects up to 100 expired active reservations deterministically and processes each in an isolated transaction. Both scheduled and request-time paths lock the listing first, recheck active reservation state using the injected clock, expire the hold, and reactivate only a still-`RESERVED` listing. Request-time expiration flushes the old row before creating a replacement active reservation, preserving the partial unique index.
- Files changed:
  - `apps/api/src/main/java/com/ticketpass/api/TicketPassApiApplication.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/ListingReservationExpirationCandidate.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/ListingReservationExpirationScheduler.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/ListingReservationExpirationService.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/ListingReservationRepository.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/ListingReservationService.java`
  - `apps/api/src/main/resources/application.yml`
  - `docs/API.md`
  - `docs/DATABASE.md`
  - `docs/SECURITY.md`
  - `docs/flows/LISTING_STATUS_FLOW.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-16
- GitHub Issue: `#54` - https://github.com/VietCT04/TicketPass/issues/54
- Summary: Implemented the authenticated backend reservation creation endpoint. The service captures one server timestamp, pessimistically locks the listing, safely handles same-buyer retries, validates availability and ownership server-side, creates a separate `ACTIVE` reservation with a 10-minute expiry, and transitions the listing to `RESERVED` in the same transaction. The `V4` migration restricts reservation statuses and ensures one active hold per listing. Expiration cleanup, listing reactivation, CSRF hardening, frontend controls, checkout, and audit expansion remain out of scope.
- Files changed:
  - `apps/api/src/main/java/com/ticketpass/api/auth/SecurityConfig.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/ListingController.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/ListingRepository.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/ListingReservationEntity.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/ListingReservationRepository.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/ListingReservationResponse.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/ListingReservationResult.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/ListingReservationService.java`
  - `apps/api/src/main/java/com/ticketpass/api/listing/ListingReservationStatus.java`
  - `apps/api/src/main/resources/db/migration/V4__create_listing_reservations.sql`
  - `docs/API.md`
  - `docs/DATABASE.md`
  - `docs/SECURITY.md`
  - `docs/flows/LISTING_STATUS_FLOW.md`
  - `docs/CONCERNS.md`
  - `docs/CONTINUITY.md`

- Date: 2026-07-16
- GitHub Issue: `#46` - https://github.com/VietCT04/TicketPass/issues/46
- Summary: Implemented the public, server-rendered `/events/{eventId}` page using the existing public event-detail API. Homepage event cards now provide an explicit `View tickets` link. The detail page uses no-store fetching, normalizes invalid pagination to page 1, redirects pages beyond the final valid page, renders safe event and read-only listing data only, supports all returned transfer methods, formats VND prices, and provides loading, empty, unavailable-event, and unexpected-error states. It does not introduce reservation, checkout, seller identity, `public_notes`, or sensitive ticket payload rendering.
- Files changed:
  - `apps/web/src/app/events/[eventId]/page.tsx`
  - `apps/web/src/app/events/[eventId]/loading.tsx`
  - `apps/web/src/components/EventBrowseCard.tsx`
  - `apps/web/src/components/EventDetailSkeleton.tsx`
  - `apps/web/src/components/EventListingCard.tsx`
  - `apps/web/src/components/EventListingPagination.tsx`
  - `apps/web/src/lib/events.ts`
  - `docs/CONTINUITY.md`

- Date: 2026-07-16
- GitHub Issue: `#53` - https://github.com/VietCT04/TicketPass/issues/53
- Summary: Documented the approved buyer reservation API and data contract for `POST /api/listings/{listingId}/reservations`. The contract requires authentication, a separate reservation record, a server-controlled 10-minute hold, atomic `ACTIVE -> RESERVED` behavior, seller self-reservation prevention, same-buyer idempotency without expiry extension, automatic expiry back to `ACTIVE`, safe responses, a general availability conflict response, and no payment, escrow, sale completion, reveal, manual release, or audit expansion.
- Files changed:
  - `docs/API.md`
  - `docs/DATABASE.md`
  - `docs/SECURITY.md`
  - `docs/flows/LISTING_STATUS_FLOW.md`
  - `docs/CONCERNS.md`
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

- Current GitHub Issue: `#87` - https://github.com/VietCT04/TicketPass/issues/87
- Current goal: Review and merge the buyer order-progress contract documentation.
- Current blocker: None.

## Important User Stories

- `docs/user-stories/US-0001-list-transferable-ticket.md`: Seller can list a transferable ticket safely without exposing sensitive ticket data too early.
- `docs/user-stories/US-0002-authenticate-user.md`: User can sign up, log in, log out, maintain secure sessions, and access protected TicketPass account features.
- `docs/user-stories/US-0003-browse-events.md`: Buyer can browse events that have active publicly visible ticket listings with safe event summaries and basic pagination.
- `docs/user-stories/US-0004-search-select-existing-event.md`: Seller must search and select an existing event through autocomplete, and listing creation must reference that event through `event_id`.
- `docs/user-stories/US-0005-view-available-listings-for-event.md`: Buyer can open an event and compare its currently browse-eligible ticket listings without exposing seller identity or sensitive ticket payload data.
- `docs/user-stories/US-0006-reserve-available-ticket-listing.md`: Authenticated buyer can place a server-controlled 10-minute hold on one available listing before checkout, with atomic duplicate-sale prevention and automatic expiration.
- `docs/user-stories/US-0007-complete-checkout-for-reserved-ticket.md`: Authenticated buyer can complete payment for an active reservation through provider-hosted checkout, while only trusted server-to-server confirmation may complete the sale.
- `docs/user-stories/US-0008-request-missing-event.md`: Authenticated sellers can request a missing future event for future catalogue review without creating or modifying an event or bypassing listing rules.
- `docs/user-stories/US-0009-view-own-listings.md`: Authenticated sellers can view only their own listings and stored marketplace statuses through a read-only protected API and page.
- `docs/user-stories/US-0010-view-own-orders.md`: Authenticated buyers can view their own order progress with payment, ticket-transfer, and settlement state kept separate.

## Known Concerns

- See `docs/CONCERNS.md`.
- Password policy is defined for MVP but still needs review before public launch.
- Cross-site frontend/API deployment requires a new CSRF and cookie review; same-site origin protection is implemented in issue `#56`.
- Account recovery and verification features are deferred.
- Local verification requires Java 21; current Maven runtime uses Java 19 and cannot compile the project.
- MVP does not classify seller listing `public_notes` for sensitive ticket payload content.
- Platform-specific transferability rules are unresolved.
- Seller transferability confirmation is not proof.
- Existing duplicate events may appear as separate autocomplete results until deduplication rules are implemented.
- Event cancellation and rescheduling rules are not defined for browse or seller selection.
- Event image source and moderation rules are not defined; the issue `#27` frontend uses a neutral local placeholder only.
- Missing-event requests remain `PENDING` until a future authorized catalogue-review workflow defines review, event insertion, notification, cross-user deduplication, and event-local timezone preservation; see `CONCERN-0023`.
- Own-listings pagination may need a focused composite-index review at production volume, and all listing mutations remain deferred; see `CONCERN-0024`.
- Event autocomplete query performance may require indexes or a dedicated search strategy after production-volume review.
- Event local timezone preservation and display rules are unresolved.
- Listing availability can change between event-detail page load and a future reservation attempt; `GET /api/events/{eventId}` is only a marketplace snapshot.
- Reservation creation, expiry cleanup, guarded listing reactivation, CSRF origin protection, and browser reservation actions are implemented in issues `#54` through `#57`.
- Browser reservation state is intentionally in-memory only; see `CONCERN-0020`.
- Hosted payment deadline support and late successful payment handling remain unresolved; see `CONCERN-0021`.
- Audit retention, deletion, export, and compliance rules are not defined.
- Buyer order-progress lists are intentionally read-only snapshots; deadline freshness must remain owned by bounded reconciliation and the authoritative single-order read; see `CONCERN-0025`.

## Next Recommended Steps

1. Review and merge the issue `#87` buyer order-progress contract pull request.
2. Complete the dependent post-payment lifecycle contracts and persistence before implementing issue `#88`.
3. Add the protected seller own-listings page in issue `#84`.
