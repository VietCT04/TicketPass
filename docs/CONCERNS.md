# Concerns

## CONCERN-0035: Settlement Provider And Operational Recovery Are Undefined

Date: 2026-07-19
Related User Story: `docs/user-stories/US-0012-buyer-confirms-ticket-receipt.md`
Related GitHub Issues: `#95` - https://github.com/VietCT04/TicketPass/issues/95, `#96` - https://github.com/VietCT04/TicketPass/issues/96, `#98` - https://github.com/VietCT04/TicketPass/issues/98, `#99` - https://github.com/VietCT04/TicketPass/issues/99

### Concern

The approved receipt-confirmation contract creates a durable, idempotent settlement-release boundary but intentionally does not select a production settlement provider, define seller onboarding, define retry ownership or operational alerting, or define refund, dispute, timeout, and review resolution after a release operation becomes uncertain or requires review. Issue `#96` supplies the local-only mock adapter and operation record; production settlement remains unimplemented.

### Risk

An implementation could represent a development adapter as real financial infrastructure, retry unsafely without an owner, or handle a provider timeout, permanent failure, or contradictory outcome as a release or refund without authoritative evidence. This could cause duplicate release, stranded held funds, or an unresolvable buyer and seller outcome.

### Recommendation

Keep issue `#96` limited to the approved stable-key, durable-operation, lease, lookup, and fail-safe local lifecycle. Before production settlement, approve provider-specific credentials, payout onboarding, financial reconciliation, monitoring, alerting, manual review, refund, dispute, and chargeback workflows. Issues `#98` and `#99` must define how timeout and review states inspect existing release operations before any automated disposition.

### Status

Open

## CONCERN-0034: Admin Bootstrap And Exact Event Identity Limits

Date: 2026-07-19
Related User Story: `docs/user-stories/US-0024-admin-reviews-event-requests.md`
Related GitHub Issue: `#145` - https://github.com/VietCT04/TicketPass/issues/145

### Concern

The review contract introduces server-controlled `ADMIN` access and exact normalized event identity, but it does not define an operator bootstrap process, event-local timezone storage, or broader similarity matching. Existing exact duplicate events must be identified before the unique index can be applied.

### Risk

An unsafe role-provisioning process could grant privileged access. Exact matching can leave near-duplicate events unresolved, while a careless migration could merge or break existing events and their listings. Absolute timestamps may still render differently from a venue's local time.

### Recommendation

Define a controlled operational admin-provisioning runbook before enabling review. Report and resolve existing exact duplicates deliberately before adding the unique index. Keep fuzzy matching and event-local timezone design in separate reviewed work.

### Status

Open

## CONCERN-0033: Draft And Administrative Listing Cancellation Are Undefined

Date: 2026-07-19
Related User Story: `docs/user-stories/US-0016-cancel-own-listing.md`
Related GitHub Issues: `#113` - https://github.com/VietCT04/TicketPass/issues/113, `#114` - https://github.com/VietCT04/TicketPass/issues/114, `#115` - https://github.com/VietCT04/TicketPass/issues/115

### Concern

The approved MVP seller-cancellation contract allows only an authenticated owner to transition an `ACTIVE` listing to `CANCELLED`. It intentionally does not define draft cancellation, administrative action, terminal-state recovery, notifications, or a dedicated `cancelled_at` field.

### Risk

Adding a broad cancellation path later could bypass reservation, checkout, paid-sale, audit, or authorization safeguards, or create inconsistent terminal-history semantics.

### Recommendation

Keep issue `#114` limited to the approved `ACTIVE -> CANCELLED` seller transition. Define each additional actor, status transition, timestamp, notification, and recovery behavior in a separately reviewed issue before implementation.

### Status

Open

## CONCERN-0032: Display-Name Moderation And Impersonation Policy Is Deferred

Date: 2026-07-19
Related User Story: `docs/user-stories/US-0023-update-account-profile.md`
Related GitHub Issues: `#141` - https://github.com/VietCT04/TicketPass/issues/141, `#142` - https://github.com/VietCT04/TicketPass/issues/142, `#143` - https://github.com/VietCT04/TicketPass/issues/143

### Concern

The approved MVP display-name contract accepts broad Unicode text after trimming. It does not case-fold, Unicode-normalize, enforce uniqueness, or apply reserved-name, impersonation, moderation, or history rules.

### Risk

Users can choose visually similar or misleading names. Unicode-equivalent values may behave differently, and support or trust-and-safety operations lack a controlled way to restrict abusive profile names.

### Recommendation

Before exposing profiles publicly or relying on display names for trust decisions, define a separately reviewed policy for rendering, moderation, reserved names, Unicode normalization, impersonation handling, and any retention needed for enforcement. Continue rendering every display name as untrusted text.

### Status

Open

## CONCERN-0028: Build-Time Web API Origin Limits Image Portability

Date: 2026-07-19
Related User Story: `docs/user-stories/US-0014-reproducible-container-stack.md`
Related GitHub Issue: `#104` - https://github.com/VietCT04/TicketPass/issues/104

### Concern

`NEXT_PUBLIC_API_BASE_URL` is embedded in the browser bundle at web-image build time.

### Risk

The same image cannot move between environments with different public API origins without a rebuild. Supplying the internal Compose address to the browser would also make browser requests fail.

### Recommendation

Build each deployment image with its explicit externally reachable API origin. Evaluate runtime-neutral API discovery only as a separately reviewed change.

### Status

Open

## CONCERN-0029: Container Base-Image Maintenance Needs An Update Process

Date: 2026-07-19
Related User Story: `docs/user-stories/US-0014-reproducible-container-stack.md`
Related GitHub Issues: `#104` - https://github.com/VietCT04/TicketPass/issues/104, `#105` - https://github.com/VietCT04/TicketPass/issues/105, `#106` - https://github.com/VietCT04/TicketPass/issues/106

### Concern

Explicit Java, Maven, and Node image versions improve reproducibility, while permanent digest pins can prevent security updates without an ownership and update process.

### Risk

Container images may retain known vulnerabilities or receive unreviewed base-image changes.

### Recommendation

Before production deployment, establish image ownership, review cadence, vulnerability handling, and a documented policy for version and digest updates.

### Status

Open

## CONCERN-0030: Single-Host Compose Does Not Provide Resilience

Date: 2026-07-19
Related User Story: `docs/user-stories/US-0014-reproducible-container-stack.md`
Related GitHub Issues: `#104` - https://github.com/VietCT04/TicketPass/issues/104, `#107` - https://github.com/VietCT04/TicketPass/issues/107

### Concern

The first container stack is intentionally one host with one API instance, one web instance, and one PostgreSQL instance.

### Risk

Host, database, or application failure causes service interruption, and the baseline does not include backups, monitoring, disaster recovery, or high availability.

### Recommendation

Use the stack only for local integration, demonstrations, and explicitly accepted single-host deployment work. Design resilience, backup/restore, observability, and high availability separately before relying on it for critical production operations.

### Status

Open

## CONCERN-0031: Flyway Replica Coordination Is Undefined

Date: 2026-07-19
Related User Story: `docs/user-stories/US-0014-reproducible-container-stack.md`
Related GitHub Issues: `#104` - https://github.com/VietCT04/TicketPass/issues/104, `#107` - https://github.com/VietCT04/TicketPass/issues/107

### Concern

The approved container baseline runs a single API instance and relies on Flyway during API startup. Multi-replica migration coordination and rollback are not defined.

### Risk

Scaling the API without a migration strategy could cause startup contention, incomplete rollout behavior, or unsafe manual recovery.

### Recommendation

Keep the initial stack single-instance. Define replica-safe migration ownership, rollout sequencing, rollback boundaries, and recovery procedures before introducing multiple API replicas.

### Status

Open

## CONCERN-0027: Paid-Order Fulfilment Backfill And Deadline Reconciliation

Date: 2026-07-19
Related User Story: `docs/user-stories/US-0011-seller-transfers-paid-ticket.md`
Related GitHub Issues: `#92` - https://github.com/VietCT04/TicketPass/issues/92, `#93` - https://github.com/VietCT04/TicketPass/issues/93, `#98` - https://github.com/VietCT04/TicketPass/issues/98, `#99` - https://github.com/VietCT04/TicketPass/issues/99

### Concern

Issue `#93` creates one fulfilment record for every existing paid order from its trusted `paid_at`, including orders whose derived 15-minute deadline has already elapsed. Those elapsed records remain durably awaiting transfer until separately approved timeout handling changes them.

### Risk

Inventing a missing payment timestamp would create an unauditable deadline. Leaving a backfilled elapsed deadline untreated could expose stale awaiting-transfer progress until timeout reconciliation runs.

### Recommendation

Issue `#93` fails migration if a paid order lacks `paid_at`, backfills only from that trusted timestamp, and preserves the deadline without extension. Issue `#99` should process eligible past-due backfilled records through the separately approved timeout transition before operational rollout.

### Status

Open

## CONCERN-0026: Public Event Search Performance And Time Semantics

Date: 2026-07-19
Related User Story: `docs/user-stories/US-0015-search-filter-events.md`
Related GitHub Issues: `#109` - https://github.com/VietCT04/TicketPass/issues/109, `#110` - https://github.com/VietCT04/TicketPass/issues/110, `#111` - https://github.com/VietCT04/TicketPass/issues/111

### Concern

The approved public search contract uses case-insensitive substring matching across event name, venue, and free-text city data. Event timestamps are stored as instants, while future calendar-day controls use a user-supplied UTC offset rather than an event-local timezone. Results retain deterministic upcoming-event ordering rather than relevance ranking.

### Risk

Substring matching may become slow as the event catalogue grows, city spelling and normalization may fragment exact-city results, and a user-supplied offset can differ from the venue's local time. Buyers may also expect search relevance that the intentionally unranked MVP does not provide.

### Recommendation

Implement issue `#110` with database-side predicates and measure production query behavior before adding indexes or dedicated search infrastructure. Keep offset selection explicit in issue `#111`, do not imply stored event-local timezone data, and treat stronger normalization, locale-aware matching, deduplication, and relevance ranking as separate product work.

### Status

Open

## CONCERN-0025: Buyer Order-Progress Snapshot Freshness

Date: 2026-07-19
Related User Story: `docs/user-stories/US-0010-view-own-orders.md`
Related GitHub Issues: `#87` - https://github.com/VietCT04/TicketPass/issues/87, `#88` - https://github.com/VietCT04/TicketPass/issues/88, `#98` - https://github.com/VietCT04/TicketPass/issues/98

### Concern

The future `GET /api/me/orders` endpoint is intentionally a read-only paginated snapshot. It does not reconcile every returned order against payment, transfer, or settlement deadlines.

### Risk

A persisted status can be briefly stale after a deadline until the scheduled reconciliation path or an authoritative single-order refresh processes it. Per-row reconciliation would instead create unbounded database and provider work for an account-history page and could make pagination inconsistent.

### Recommendation

Keep bulk lifecycle reconciliation scheduled and bounded. Return `status_refresh_required` only when the server can identify a passed persisted deadline, and require the browser to use the protected single-order read for authoritative refresh. Add focused freshness, pagination, and multi-instance reconciliation coverage during the later verification phase.

### Status

Open

## CONCERN-0024: Own-Listings Query Performance And Future Mutations

Date: 2026-07-18
Related User Story: `docs/user-stories/US-0009-view-own-listings.md`
Related GitHub Issues: `#82` - https://github.com/VietCT04/TicketPass/issues/82, `#83` - https://github.com/VietCT04/TicketPass/issues/83, `#84` - https://github.com/VietCT04/TicketPass/issues/84

### Concern

The MVP own-listings query uses the existing separate `seller_id` and `status` indexes. It does not add a composite index for seller, optional status, and newest-first ordering, and it remains read-only.

### Risk

At higher listing volumes, pagination queries may become inefficient. Adding editing, cancellation, relisting, or bulk actions without separate lifecycle, concurrency, and audit rules could conflict with active reservations, checkout, completed sales, or dispute work.

### Recommendation

Measure production query behavior before creating a focused performance issue for a composite index. Define each listing mutation separately with authorization, state-transition, concurrency, and audit requirements before enabling it.

### Status

Open

## CONCERN-0023: Missing-Event Request Review And Catalogue Completion

Date: 2026-07-18
Related User Story: `docs/user-stories/US-0008-request-missing-event.md`
Related GitHub Issues: `#77` - https://github.com/VietCT04/TicketPass/issues/77, `#78` - https://github.com/VietCT04/TicketPass/issues/78, `#79` - https://github.com/VietCT04/TicketPass/issues/79

### Concern

Issue `#78` implements only seller-owned `PENDING` request submission. Issue `#145` now defines the future admin roles, review decisions, catalogue insertion/linking, seller tracking, and exact sibling resolution contract; implementation remains split across issues `#146` through `#151`. IANA event-timezone storage remains unresolved.

### Risk

Pending requests remain ineligible until the follow-up implementation is delivered. Exact matching can leave near-duplicate requests or events unresolved, while broader matching could incorrectly merge distinct performances or expose ownership. Storing only an absolute instant may render a future event in a timezone different from its venue-local time.

### Recommendation

Implement the approved role, review, audit, exact-identity, privacy, and seller-continuation slices without broadening them. Define event-local timezone preservation separately and keep user-provided text and URLs untrusted throughout the work.

### Status

Open

## CONCERN-0021: Hosted Payment Deadline And Late Confirmation Operations

Date: 2026-07-16  
Related GitHub Issues: `#65` - https://github.com/VietCT04/TicketPass/issues/65; `#67` - https://github.com/VietCT04/TicketPass/issues/67; `#68` - https://github.com/VietCT04/TicketPass/issues/68; `#69` - https://github.com/VietCT04/TicketPass/issues/69; `#70` - https://github.com/VietCT04/TicketPass/issues/70
Related User Story: `docs/user-stories/US-0007-complete-checkout-for-reserved-ticket.md`

### Concern

Checkout inherits the short reservation deadline. Issue `#67` implements only an in-application mock provider, not a production provider. A future production provider may not natively expire or cancel a hosted payment session at the exact inherited deadline, and a verified payment event may arrive after TicketPass has already expired the order and released the listing.

### Risk

An implementation could accidentally extend a hold, sell a listing after it was safely released, or leave paid funds without an automated disposition. That can oversell inventory or create an unresolved buyer-payment obligation.

### Recommendation

Before a production-provider user story is approved, confirm deadline support or define an approved server-side invalidation path. Issue `#68` durably deduplicates late trusted success events and records them as `REQUIRES_ACTION` without sale completion. Issue `#69` now preserves terminal local state during failure/cancellation/expiry reconciliation and blocks automatic release for unresolved receipts. Define manual review, refund, and customer communication operations before production payment collection.

### Status

Open

## CONCERN-0022: Mock Payment Infrastructure Is Not Production Payment Processing

Date: 2026-07-18
Related GitHub Issue: `#70` - https://github.com/VietCT04/TicketPass/issues/70

### Concern

The only supported provider is an in-application mock used for checkout lifecycle development. It does not provide regulated escrow, production payment collection, seller payout, refunds, chargebacks, disputes, fraud controls, monitoring, or admin operations.

### Risk

Deploying or describing the mock path as a production payment service could expose users to incorrect payment expectations and leave financial or operational failures without handling.

### Recommendation

Before production collection, approve a dedicated provider-specific user story and security review covering credentials, webhook contracts, refunds, payouts, disputes, financial audit, alerting, monitoring, and operational ownership.

### Status

Open

## CONCERN-0020: Browser Reservation Hold Recovery

Date: 2026-07-16
Related User Story: `docs/user-stories/US-0006-reserve-available-ticket-listing.md`
Related GitHub Issues: `#57` - https://github.com/VietCT04/TicketPass/issues/57; `#71` - https://github.com/VietCT04/TicketPass/issues/71

### Concern

The issue `#57` reservation panel keeps its active hold and countdown only in React memory. A hard refresh, a new browser session, or navigation away from the event page can remove the visible hold panel while the server-side reservation remains active.

### Risk

The buyer may not immediately see a hold that still prevents other buyers from reserving the listing. Retrying the same listing can recover the active reservation through the idempotent POST, but there is no dedicated read or account recovery view.

### Recommendation

Issue `#71` now provides server-backed order recovery after an order has been started. A current-reservation read or account recovery experience is still needed for holds that have not yet started checkout. Keep all recovery data server-derived and do not use browser storage as a substitute for authoritative reservation state.

### Status

Open

## CONCERN-0004: Password Policy Needs Review

Date: 2026-07-10
Related User Story: `docs/user-stories/US-0002-authenticate-user.md`
Related GitHub Issue: `#9` - https://github.com/VietCT04/TicketPass/issues/9

### Concern

The MVP password policy is 12 to 128 characters, but it has not had final product/security review.

### Risk

A weak password policy may allow easily compromised accounts, while an overly strict policy may create poor usability and support issues.

### Recommendation

Review the MVP password policy before public launch and adjust if product or security requirements change.

### Status

Open

## CONCERN-0005: Session Cookie CSRF Hardening

Date: 2026-07-10
Related User Stories: `docs/user-stories/US-0002-authenticate-user.md`, `docs/user-stories/US-0006-reserve-available-ticket-listing.md`
Related GitHub Issues: `#9` - https://github.com/VietCT04/TicketPass/issues/9, `#53` - https://github.com/VietCT04/TicketPass/issues/53, `#56` - https://github.com/VietCT04/TicketPass/issues/56

### Concern

Issue `#56` adds exact trusted-origin validation and credentialed CORS for unsafe cookie-authenticated API requests. The MVP remains limited to same-site frontend and API deployments.

### Risk

A future cross-site deployment requiring `SameSite=None`, wildcard origins, or a different browser-client model could invalidate this narrow origin-validation design.

### Recommendation

Before any cross-site deployment, perform a new cookie, CORS, and CSRF design review rather than weakening the trusted-origin allowlist.

### Status

Resolved for the same-site MVP; reopen for a cross-site deployment.

## CONCERN-0006: Deferred Account Recovery And Verification

Date: 2026-07-10
Related User Story: `docs/user-stories/US-0002-authenticate-user.md`
Related GitHub Issue: `#9` - https://github.com/VietCT04/TicketPass/issues/9

### Concern

Email verification, password reset, MFA, OAuth/social login, admin roles, and account deletion are out of scope for the initial authentication contract.

### Risk

Users may be unable to recover accounts, verify identity, or use stronger login protections until follow-up work is planned.

### Recommendation

Create follow-up user stories or issues for account recovery, verification, MFA, and account lifecycle features before public launch.

### Status

Open

## CONCERN-0007: Local Java Runtime Cannot Verify Backend Tests

Date: 2026-07-10
Related User Story: `docs/user-stories/US-0002-authenticate-user.md`
Related GitHub Issues: `#10` - https://github.com/VietCT04/TicketPass/issues/10, `#3` - https://github.com/VietCT04/TicketPass/issues/3, `#5` - https://github.com/VietCT04/TicketPass/issues/5

### Concern

The backend project targets Java 21, but the current local Java runtime uses Java 19.

### Risk

Backend compile/test commands cannot be run locally with the current Java runtime, so backend changes for issues `#10`, `#3`, and `#5` could not be fully verified in this environment.

### Recommendation

Run `mvn test` with Java 21 before merging or continuing backend implementation work.

### Status

Open

## CONCERN-0008: Public Notes Sensitive Content Is Not Classified

Date: 2026-07-11
Related User Story: `docs/user-stories/US-0001-list-transferable-ticket.md`
Related GitHub Issues: `#3` - https://github.com/VietCT04/TicketPass/issues/3, `#5` - https://github.com/VietCT04/TicketPass/issues/5

### Concern

MVP seller listing creation does not classify or scan free-text `public_notes` for QR codes, barcodes, private transfer links, platform credentials, or other sensitive ticket payload data.

### Risk

A seller may accidentally or intentionally put sensitive ticket data into public notes, which could expose usable ticket information before escrow, payment, audit, and reveal controls apply.

### Recommendation

Keep dedicated sensitive ticket payload fields out of public listing metadata now. Add content classification, stricter public note rules, moderation, or ticket-upload separation in a later issue before public launch or broader seller access.

### Status

Open

## CONCERN-0018: Audit Retention Policy Is Undefined

Date: 2026-07-15
Related User Story: `docs/user-stories/US-0001-list-transferable-ticket.md`
Related GitHub Issue: `#5` - https://github.com/VietCT04/TicketPass/issues/5

### Concern

Issue `#5` creates append-only audit records for seller listing creation, but TicketPass does not yet define audit retention, deletion, export, or compliance review rules.

### Risk

Keeping audit records indefinitely may create privacy or compliance risk, while deleting them too aggressively may weaken dispute handling, fraud investigation, and marketplace trust.

### Recommendation

Define audit retention and access policy before adding broad audit event coverage, audit viewers, admin search, or compliance workflows.

### Status

Open

## CONCERN-0009: Event Cancellation And Rescheduling Rules

Date: 2026-07-12
Related User Story: `docs/user-stories/US-0003-browse-events.md`
Related GitHub Issues: `#25` - https://github.com/VietCT04/TicketPass/issues/25, `#26` - https://github.com/VietCT04/TicketPass/issues/26, `#44` - https://github.com/VietCT04/TicketPass/issues/44

### Concern

The event-first browse story excludes expired, cancelled, hidden, and non-public events, but the current MVP event model does not define cancellation or rescheduling status rules.

### Risk

Buyers may see events that are cancelled, moved, or stale if event lifecycle rules are not defined before public browsing relies on them.

### Recommendation

Issues `#25`, `#26`, and `#44` use the MVP browse rule based on future `events.starts_at`, active listing status, and VND currency. Plan follow-up schema and product work before relying on event-level cancellation, rescheduling, hidden, or public/private visibility states.

### Status

Open

## CONCERN-0010: Browse Event Aggregate Freshness

Date: 2026-07-12
Related User Story: `docs/user-stories/US-0003-browse-events.md`
Related GitHub Issues: `#25` - https://github.com/VietCT04/TicketPass/issues/25, `#26` - https://github.com/VietCT04/TicketPass/issues/26

### Concern

Event browse summaries may show aggregate listing information such as lowest available price and available listing count.

### Risk

Issue `#26` calculates browse aggregate values at query time. If aggregate values are cached or denormalized later without clear update rules, buyers may see stale prices or availability counts.

### Recommendation

Keep MVP aggregate values query-time and server-derived from the shared browse-eligible listing rule. If caching is introduced later, document invalidation rules for listing status, price, currency, visibility, and event start-time changes.

### Status

Resolved

## CONCERN-0011: Event Image Source And Moderation

Date: 2026-07-12
Related User Story: `docs/user-stories/US-0003-browse-events.md`
Related GitHub Issues: `#25` - https://github.com/VietCT04/TicketPass/issues/25, `#27` - https://github.com/VietCT04/TicketPass/issues/27, `#44` - https://github.com/VietCT04/TicketPass/issues/44

### Concern

The browse events story allows safe event images or placeholders, but the source and moderation rules for event images are not defined.

### Risk

Unsafe, copyrighted, misleading, or user-uploaded images could appear in public marketplace browsing if image sourcing is not constrained.

### Recommendation

Use placeholders for MVP unless a trusted image source is defined. Before allowing user-uploaded or externally sourced event images, define validation, moderation, attribution, and fallback behavior.

### Status

Open

## CONCERN-0012: Browse Currency Scope Differs From Listing Creation

Date: 2026-07-13
Related User Story: `docs/user-stories/US-0003-browse-events.md`
Related GitHub Issues: `#25` - https://github.com/VietCT04/TicketPass/issues/25, `#32` - https://github.com/VietCT04/TicketPass/issues/32, `#34` - https://github.com/VietCT04/TicketPass/issues/34

### Concern

The browse events contract supports only `VND` for MVP. Issue `#34` aligns backend listing creation so new listings reject client-provided `currency` and are stored as `VND`.

### Risk

Resolved for new listing creation. If legacy or manually seeded non-VND listing rows are introduced later, they remain outside the MVP browse-eligible listing rule and will not affect browse event visibility or aggregates.

### Recommendation

Keep the MVP browse rule limited to active future `VND` listings. Revisit this concern only if multi-currency support is planned.

### Status

Resolved

## CONCERN-0015: Event Platform Schema Placement

Date: 2026-07-14
Related User Stories: `docs/user-stories/US-0001-list-transferable-ticket.md`, `docs/user-stories/US-0004-search-select-existing-event.md`
Related GitHub Issues: `#32` - https://github.com/VietCT04/TicketPass/issues/32, `#34` - https://github.com/VietCT04/TicketPass/issues/34

### Concern

Issue `#32` defines `event_platform` as listing/ticket-specific rather than shared event identity. Issue `#34` moves `event_platform` from `events` to `listings` in the unapplied `V2` migration and backend entities.

### Risk

Resolved for the current MVP schema. If `V2` is ever applied in a persistent environment before this change, a real migration and backfill plan would be required instead of editing `V2` directly.

### Recommendation

Keep `event_platform` listing-scoped. Before any persistent deployment, confirm Flyway history does not already contain the older `V2` shape.

### Status

Resolved

## CONCERN-0013: Event Autocomplete Matching And Missing-Event Flow

Date: 2026-07-14
Related User Story: `docs/user-stories/US-0004-search-select-existing-event.md`
Related GitHub Issues: `#31` - https://github.com/VietCT04/TicketPass/issues/31, `#33` - https://github.com/VietCT04/TicketPass/issues/33, `#35` - https://github.com/VietCT04/TicketPass/issues/35

### Concern

Issue `#33` implements deterministic MVP autocomplete rules for searching `events.name`, `events.venue`, and `events.city`, but ambiguous results, duplicate event records, accent-insensitive matching support, and missing-event handling remain unresolved. Sellers also cannot create a listing when the required event is missing from TicketPass.

### Risk

Ambiguous or duplicate-looking results may cause a seller to attach a ticket to the wrong event. If accent-insensitive matching is unsupported or inconsistent, sellers may fail to find valid events. Legitimate sellers may also be blocked when no matching event exists.

### Recommendation

Use the approved autocomplete contract in `#35`, including sufficient distinguishing event fields and stable ordering. Keep duplicate-event cleanup, stronger matching, and missing-event reporting as separate product work before broader seller access.

### Status

Open

## CONCERN-0014: Event Autocomplete Query Performance

Date: 2026-07-14
Related User Story: `docs/user-stories/US-0004-search-select-existing-event.md`
Related GitHub Issues: `#31` - https://github.com/VietCT04/TicketPass/issues/31, `#33` - https://github.com/VietCT04/TicketPass/issues/33

### Concern

The implemented autocomplete endpoint searches `events.name`, `events.venue`, and `events.city` with case-insensitive matching, a minimum query length, and a maximum of 10 results, but issue `#33` does not add database indexes or implementation-specific performance controls.

### Risk

As event volume grows, substring matching across multiple text fields may become slow or create unnecessary backend load even with frontend debouncing and strict result limits.

### Recommendation

Measure or review performance before launch, and add targeted indexes or a dedicated search strategy in a follow-up migration if needed.

### Status

Open

## CONCERN-0016: Event Local Timezone Display

Date: 2026-07-14
Related User Stories: `docs/user-stories/US-0004-search-select-existing-event.md`, `docs/user-stories/US-0005-view-available-listings-for-event.md`
Related GitHub Issues: `#35` - https://github.com/VietCT04/TicketPass/issues/35, `#27` - https://github.com/VietCT04/TicketPass/issues/27, `#44` - https://github.com/VietCT04/TicketPass/issues/44

### Concern

The frontend event selector and public browse page format `starts_at` with the browser locale and available timezone abbreviation or offset. The event-detail contract also exposes the same absolute timestamp, but the event model stores no separate event-local timezone identifier.

### Risk

Users in a different timezone from the event may see a browser-local time that differs from the venue-local time they expect when selecting an event.

### Recommendation

Add event-local timezone preservation and display rules before broader seller access, event browsing, or event-detail pages depend on venue-local date and time presentation.

### Status

Open

## CONCERN-0017: Event Detail Snapshot Is Not A Reservation

Date: 2026-07-15
Related User Story: `docs/user-stories/US-0005-view-available-listings-for-event.md`
Related GitHub Issues: `#44` - https://github.com/VietCT04/TicketPass/issues/44, `#45` - https://github.com/VietCT04/TicketPass/issues/45, `#46` - https://github.com/VietCT04/TicketPass/issues/46, `#53` - https://github.com/VietCT04/TicketPass/issues/53

### Concern

The public event-detail response shows currently browse-eligible listings, but loading the event detail page does not reserve inventory or guarantee that a listed ticket will remain available.

### Risk

A buyer may view a listing that becomes sold, reserved, cancelled, expired, or otherwise unavailable before a future checkout flow starts. If later checkout logic trusts the public detail response, a ticket could be oversold or an unavailable listing could enter payment.

### Recommendation

Treat `GET /api/events/{eventId}` as a current marketplace snapshot only. Future reservation and checkout logic must independently revalidate listing status, event eligibility, currency, transfer method rules, ownership, and availability server-side immediately before reserving or accepting payment.

### Status

Open

## CONCERN-0019: Reservation Atomicity And Expiration Recovery

Date: 2026-07-16
Related User Story: `docs/user-stories/US-0006-reserve-available-ticket-listing.md`
Related GitHub Issues: `#53` - https://github.com/VietCT04/TicketPass/issues/53, `#54` - https://github.com/VietCT04/TicketPass/issues/54, `#55` - https://github.com/VietCT04/TicketPass/issues/55

### Concern

Issues `#54` and `#55` use a transaction-scoped pessimistic listing lock and a partial unique index to ensure only one concurrent buyer acquires an `ACTIVE` listing. Issue `#55` reconciles holds at `expires_at <= now` through both scheduled and request-time paths, and reactivates a listing only while it remains `RESERVED`.

### Risk

The functional expiration and reactivation risk is addressed. Verification coverage for expiration timing, multi-instance races, and partial-index replacement ordering is still deferred to the dedicated verification phase.

### Recommendation

Keep the issue `#54` lock and unique-index safeguards intact. Add focused concurrency and expiration coverage during the later verification phase.

### Status

Resolved

## CONCERN-0001: Platform-Specific Transferability Rules

Date: 2026-07-09
Related User Story: `docs/user-stories/US-0001-list-transferable-ticket.md`
Related GitHub Issue: `#2` - https://github.com/VietCT04/TicketPass/issues/2

### Concern

Ticket transferability varies by event platform, venue, event organizer, and ticket type.

### Risk

A seller may create a listing for a ticket that cannot actually be transferred, which can cause buyer harm, disputes, refunds, or manual support work.

### Recommendation

Track `event_platform` from the start and later define platform-specific validation, seller warnings, or provider integrations before enabling higher-risk transfer methods at scale.

### Status

Open

## CONCERN-0002: Seller Transferability Confirmation Is Not Proof

Date: 2026-07-09
Related User Story: `docs/user-stories/US-0001-list-transferable-ticket.md`
Related GitHub Issue: `#2` - https://github.com/VietCT04/TicketPass/issues/2

### Concern

The listing contract requires `is_transferable_confirmed`, but this is only a seller assertion.

### Risk

Sellers may misunderstand the source platform rules or falsely claim the ticket is transferable.

### Recommendation

Use this confirmation as an MVP safeguard only. Add stronger verification, provider-specific guidance, and dispute evidence requirements in later user stories or implementation issues.

### Status

Open

## CONCERN-0003: Event Reuse And Deduplication

Date: 2026-07-09
Related User Stories: `docs/user-stories/US-0001-list-transferable-ticket.md`, `docs/user-stories/US-0004-search-select-existing-event.md`, `docs/user-stories/US-0005-view-available-listings-for-event.md`
Related GitHub Issues: `#2` - https://github.com/VietCT04/TicketPass/issues/2, `#31` - https://github.com/VietCT04/TicketPass/issues/31, `#33` - https://github.com/VietCT04/TicketPass/issues/33, `#44` - https://github.com/VietCT04/TicketPass/issues/44

### Concern

Events are normalized into an `events` table, but the MVP contract does not define exact matching or deduplication rules for reusing existing events.

### Risk

The database may contain duplicate event rows for the same real-world event. Those duplicates may appear as separate autocomplete results and may split equivalent ticket inventory across separate event-detail pages.

### Recommendation

Require sellers to select an existing event rather than create event identity through the listing form. Show enough event context to distinguish results, while tracking deduplication and admin curation as separate follow-up work.

### Status

Open
