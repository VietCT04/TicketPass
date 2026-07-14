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
Related GitHub Issues: `#10` - https://github.com/VietCT04/TicketPass/issues/10, `#3` - https://github.com/VietCT04/TicketPass/issues/3

### Concern

The backend project targets Java 21, but the current local Maven runtime uses Java 19.

### Risk

`mvn test` cannot compile the project locally, so backend tests added for issues `#10` and `#3` could not be fully verified in this environment.

### Recommendation

Run `mvn test` with Java 21 before merging or continuing backend implementation work.

### Status

Open

## CONCERN-0008: Public Notes Sensitive Content Is Not Classified

Date: 2026-07-11
Related User Story: `docs/user-stories/US-0001-list-transferable-ticket.md`
Related GitHub Issue: `#3` - https://github.com/VietCT04/TicketPass/issues/3

### Concern

MVP seller listing creation does not classify or scan free-text `public_notes` for QR codes, barcodes, private transfer links, platform credentials, or other sensitive ticket payload data.

### Risk

A seller may accidentally or intentionally put sensitive ticket data into public notes, which could expose usable ticket information before escrow, payment, audit, and reveal controls apply.

### Recommendation

Keep dedicated sensitive ticket payload fields out of public listing metadata now. Add content classification, stricter public note rules, moderation, or ticket-upload separation in a later issue before public launch or broader seller access.

### Status

Open

## CONCERN-0009: Event Cancellation And Rescheduling Rules

Date: 2026-07-12
Related User Story: `docs/user-stories/US-0003-browse-events.md`
Related GitHub Issue: `#25` - https://github.com/VietCT04/TicketPass/issues/25

### Concern

The event-first browse story excludes expired, cancelled, hidden, and non-public events, but the current MVP event model does not define cancellation or rescheduling status rules.

### Risk

Buyers may see events that are cancelled, moved, or stale if event lifecycle rules are not defined before public browsing relies on them.

### Recommendation

For issue `#25`, document the MVP browse rule that uses future `events.starts_at` and active listings only. Plan follow-up schema and product work before relying on event-level cancellation, rescheduling, hidden, or public/private visibility states.

### Status

Open

## CONCERN-0010: Browse Event Aggregate Freshness

Date: 2026-07-12
Related User Story: `docs/user-stories/US-0003-browse-events.md`
Related GitHub Issue: `#25` - https://github.com/VietCT04/TicketPass/issues/25

### Concern

Event browse summaries may show aggregate listing information such as lowest available price and available listing count.

### Risk

If aggregate values are cached or denormalized without clear update rules, buyers may see stale prices or availability counts.

### Recommendation

For MVP, use query-time server-derived aggregate values from the shared browse-eligible listing rule. If caching is introduced later, document invalidation rules for listing status, price, currency, visibility, and event start-time changes.

### Status

Open

## CONCERN-0011: Event Image Source And Moderation

Date: 2026-07-12
Related User Story: `docs/user-stories/US-0003-browse-events.md`
Related GitHub Issue: `#25` - https://github.com/VietCT04/TicketPass/issues/25

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
Related GitHub Issue: `#25` - https://github.com/VietCT04/TicketPass/issues/25

### Concern

The browse events contract supports only `VND` for MVP, but the existing seller listing creation contract accepts a generic ISO-4217 currency.

### Risk

Sellers may create active non-VND listings that do not appear in public event browse results, which could confuse sellers and create inconsistent marketplace behavior.

### Recommendation

Decide whether the whole marketplace should be VND-only for MVP. If so, create a follow-up API and validation issue to restrict listing creation to `VND`. Until then, document that only active future VND listings are browse-eligible.

### Status

Open

## CONCERN-0013: Event Autocomplete Matching And Missing-Event Flow

Date: 2026-07-14
Related User Story: `docs/user-stories/US-0004-search-select-existing-event.md`
Related GitHub Issues: `#31` - https://github.com/VietCT04/TicketPass/issues/31, `#33` - https://github.com/VietCT04/TicketPass/issues/33, `#35` - https://github.com/VietCT04/TicketPass/issues/35

### Concern

Issue `#31` defines deterministic MVP autocomplete rules for searching `events.name`, `events.venue`, and `events.city`, but ambiguous results, duplicate event records, accent-insensitive matching support, and missing-event handling remain unresolved. Sellers also cannot create a listing when the required event is missing from TicketPass.

### Risk

Ambiguous or duplicate-looking results may cause a seller to attach a ticket to the wrong event. If accent-insensitive matching is unsupported or inconsistent, sellers may fail to find valid events. Legitimate sellers may also be blocked when no matching event exists.

### Recommendation

Enforce the approved `#31` contract in `#33` and `#35`, including sufficient distinguishing event fields and stable ordering. Keep duplicate-event cleanup, stronger matching, and missing-event reporting as separate product work before broader seller access.

### Status

Open

## CONCERN-0014: Event Autocomplete Query Performance

Date: 2026-07-14
Related User Story: `docs/user-stories/US-0004-search-select-existing-event.md`
Related GitHub Issues: `#31` - https://github.com/VietCT04/TicketPass/issues/31, `#33` - https://github.com/VietCT04/TicketPass/issues/33

### Concern

The approved autocomplete contract searches `events.name`, `events.venue`, and `events.city` with case-insensitive matching, a minimum query length, and a maximum of 10 results, but the current issue does not add database indexes or implementation-specific performance controls.

### Risk

As event volume grows, substring matching across multiple text fields may become slow or create unnecessary backend load even with frontend debouncing and strict result limits.

### Recommendation

Implement the smallest query that satisfies issue `#33`, measure or review performance before launch, and add targeted indexes or a dedicated search strategy in a follow-up migration if needed.

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
Related User Stories: `docs/user-stories/US-0001-list-transferable-ticket.md`, `docs/user-stories/US-0004-search-select-existing-event.md`
Related GitHub Issues: `#2` - https://github.com/VietCT04/TicketPass/issues/2, `#31` - https://github.com/VietCT04/TicketPass/issues/31, `#33` - https://github.com/VietCT04/TicketPass/issues/33

### Concern

Events are normalized into an `events` table, but the MVP contract does not define exact matching or deduplication rules for reusing existing events.

### Risk

The database may contain duplicate event rows for the same real-world event, and those duplicates may appear as separate autocomplete results.

### Recommendation

Require sellers to select an existing event rather than create event identity through the listing form. Show enough event context to distinguish results, while tracking deduplication and admin curation as separate follow-up work.

### Status

Open
