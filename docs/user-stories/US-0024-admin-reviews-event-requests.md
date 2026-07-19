# US-0024 — Admin Reviews Missing Event Requests and Publishes Catalogue Events

## User Story

As a TicketPass catalogue administrator, I want to review seller-submitted missing-event requests, safely create or select the authoritative catalogue event, and communicate the result so that approved sellers can continue listing tickets without bypassing event quality controls.

## Context

TicketPass already lets authenticated sellers submit structured missing-event requests when autocomplete cannot find an event. Requests are persisted as `PENDING`, but the current product deliberately stops before moderation, event creation, seller tracking, or approval-based continuation.

A complete catalogue workflow requires more than an admin page. TicketPass needs a persisted admin authorization boundary, a bounded review queue, exact event deduplication, concurrency-safe terminal decisions, audit records, seller-owned history, and a safe return to the existing listing form.

## Scope

- Add a persisted `USER` and `ADMIN` account-role model.
- Protect all `/api/admin/**` routes through server-authoritative role checks.
- Provide a documented operator-only process for granting or removing admin access; no HTTP role-assignment endpoint is added.
- Let admins list and inspect event requests through bounded queue and detail APIs.
- Show submitted event name, time, venue, city, and optional official URL as untrusted review metadata.
- Show exact-normalized sibling-request counts without exposing requester identities.
- Let an admin approve a request by:
  - creating a corrected future catalogue event; or
  - linking an existing future catalogue event.
- Let an admin reject a request using a bounded seller-facing reason and optional message.
- Extend request status from `PENDING` to terminal `APPROVED` or `REJECTED`.
- Store approval resolution type, resolved event, reviewer, reviewed timestamp, and rejection information.
- Add exact normalized identity fields and database uniqueness for catalogue events so concurrent approvals cannot create duplicate exact events.
- Resolve currently pending exact sibling requests to the same event in the approval transaction.
- Make identical retries idempotent and reject conflicting terminal decisions.
- Write immutable audit events for effective admin decisions and new catalogue event creation without storing submitted or reviewer text.
- Let sellers retrieve only their own request history and detail.
- For approved requests, return the resolved event and provide a server-authoritative continuation path to `/sell`.
- Preselect the resolved event only after loading the seller-owned approved request; never trust arbitrary event metadata from a URL.
- Keep normal listing validation authoritative after approval.
- Add a focused admin review console and seller request-tracking experience.
- Keep all admin and seller responses free of ticket payload, payment, session, and unrelated account data.

## Out of Scope

- Email, push, SMS, webhook, or in-app unread notifications.
- External event-provider imports, URL fetching, scraping, or automatic verification.
- Fuzzy matching, machine-learning deduplication, or automatic approval.
- Admin user-management screens or public role-assignment APIs.
- Editing or deleting existing catalogue events.
- Reopening or changing terminal request decisions.
- Requester-to-reviewer messaging or internal review chat.
- Automatic listing creation after approval.
- Ticket, listing-lifecycle, reservation, checkout, payment, fulfilment, refund, or settlement changes.
- Multi-tenant administration or fine-grained permission groups.

## Acceptance Criteria

- [ ] Existing and new ordinary users remain `USER`; only persisted admins can access admin APIs and pages.
- [ ] Admins can page, filter, search, and inspect the review queue safely.
- [ ] Admins can approve by creating an exact-deduplicated catalogue event or linking an existing one.
- [ ] Admins can reject with a bounded seller-facing reason.
- [ ] Each request receives at most one terminal decision.
- [ ] Concurrent exact event creation cannot create duplicate catalogue events.
- [ ] Pending exact sibling requests can resolve to the same event without exposing other requesters.
- [ ] Every effective decision is auditable without copying untrusted text into audit storage.
- [ ] Sellers can retrieve only their own pending, approved, and rejected requests.
- [ ] Approved sellers can continue to the existing listing form with the resolved event selected safely.
- [ ] Listing creation still revalidates event eligibility and all normal listing rules.
- [ ] Notifications, external verification, fuzzy matching, and event editing remain excluded.

## Focused Issues

1. `#145` — Define admin event-request review contract.
2. `#146` — Implement admin role and authorization foundation.
3. `#147` — Implement admin event-request queue and detail APIs.
4. `#148` — Implement event-request resolution and catalogue publication.
5. `#149` — Build admin event-request review console.
6. `#150` — Implement seller event-request history APIs.
7. `#151` — Build seller event-request tracking and listing continuation.

## Delivery Order

1. Approve the complete authorization, lifecycle, persistence, API, audit, and seller-continuation contract in `#145`.
2. Implement the shared admin role and route boundary in `#146`.
3. Implement the read-only admin queue and detail APIs in `#147`.
4. Implement transactional approval, linking, rejection, exact catalogue deduplication, sibling resolution, and audit behavior in `#148`.
5. Build the admin review console in `#149` after the backend is available.
6. Implement seller-owned request history in `#150`.
7. Build seller tracking and approved-event continuation in `#151` after `#79`, `#148`, and `#150` are available.

## Concerns

- Exact normalization prevents obvious duplicates but cannot determine whether similar performances are the same event.
- Backfilling normalized fields may reveal existing exact duplicate catalogue events and requires an explicit migration strategy.
- Admin role bootstrap must be tightly controlled and must never depend on frontend visibility or an email allowlist alone.
- Submitted text and URLs remain untrusted even when reviewed by an administrator.
- Queue and seller responses are snapshots; resolution and listing creation must revalidate current server state.
- Newly approved events remain empty until a seller completes normal listing creation.
- Seller-facing rejection messages must not expose internal checks, reviewer identity, or other requesters.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, or other verification commands in these issues. Complete application implementation first; verification will be handled later as a separate final phase.