# Concerns

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
Related User Story: `docs/user-stories/US-0002-authenticate-user.md`
Related GitHub Issue: `#9` - https://github.com/VietCT04/TicketPass/issues/9

### Concern

The MVP auth contract uses `HttpOnly` cookies with `SameSite=Lax`, but CSRF hardening may need additional controls as deployment and cross-origin behavior become clearer.

### Risk

State-changing authenticated endpoints could be exposed to CSRF risk if cookie behavior, CORS, and frontend deployment domains are not aligned.

### Recommendation

Review CSRF strategy during backend auth implementation and add CSRF tokens or stricter cookie/domain rules if needed.

### Status

Open

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
Related GitHub Issues: `#44` - https://github.com/VietCT04/TicketPass/issues/44, `#45` - https://github.com/VietCT04/TicketPass/issues/45, `#46` - https://github.com/VietCT04/TicketPass/issues/46

### Concern

The public event-detail response shows currently browse-eligible listings, but loading the event detail page does not reserve inventory or guarantee that a listed ticket will remain available.

### Risk

A buyer may view a listing that becomes sold, reserved, cancelled, expired, or otherwise unavailable before a future checkout flow starts. If later checkout logic trusts the public detail response, a ticket could be oversold or an unavailable listing could enter payment.

### Recommendation

Treat `GET /api/events/{eventId}` as a current marketplace snapshot only. Future reservation and checkout logic must independently revalidate listing status, event eligibility, currency, transfer method rules, ownership, and availability server-side immediately before reserving or accepting payment.

### Status

Open

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
