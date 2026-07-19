# US-0019 — Edit an Active Listing

## User Story

As a seller, I want to correct the editable details of my available ticket listing so that buyers see accurate ticket information and asking price without requiring me to cancel and recreate the listing.

## Context

TicketPass already supports seller listing creation and is adding a protected view of seller-owned listings. The listing lifecycle also permits buyers to reserve an `ACTIVE` listing through a server-controlled lock.

A seller edit must therefore be limited to an available listing, preserve event and ownership identity, and serialize against reservation creation. It must not rewrite historical order or payment snapshots.

## Scope

- Let an authenticated seller edit their own `ACTIVE` listing while its event remains upcoming.
- Use `PUT /api/listings/{listingId}` as a full replacement of the approved editable fields.
- Editable fields are:
  - `event_platform`
  - `seat_info`
  - `ticket_type`
  - `asking_price_minor`
  - `transfer_method`
  - optional `public_notes`
- Keep event identity, seller ownership, quantity, currency, transferability confirmation, lifecycle status, creation time, and audit history server-controlled.
- Reuse listing-creation validation and normalization rules where applicable.
- Serialize editing against reservation creation and listing cancellation through the existing listing-first pessimistic lock.
- Treat normalized no-op requests as idempotent without changing `updated_at` or creating another audit event.
- Write one immutable `LISTING_UPDATED` audit event for each effective change.
- Add an accessible seller edit flow from the own-listings experience.
- Keep all responses and UI free of buyer, order, payment, provider, credential, and ticket payload data.

## Out of Scope

- Changing the linked event.
- Changing seller ownership, quantity, currency, status, or transferability confirmation.
- Editing `RESERVED`, `SOLD`, `CANCELLED`, `EXPIRED`, or draft listings.
- Cancelling, deleting, renewing, relisting, duplicating, or bulk-editing listings.
- Reservation, checkout, payment, refund, dispute, transfer, or settlement behavior.
- Ticket payload upload, storage, replacement, or reveal.
- Broad seller-dashboard redesign.

## Acceptance Criteria

- [ ] Only the authenticated owning seller can edit an eligible `ACTIVE` listing.
- [ ] Event identity and other server-controlled fields cannot be changed.
- [ ] The complete approved editable field set is validated and normalized server-side.
- [ ] Editing and reservation creation cannot both cross the same `ACTIVE` decision boundary.
- [ ] No-op retries are idempotent and do not change timestamps or audit history.
- [ ] Each effective update writes exactly one safe immutable audit event.
- [ ] Reserved or terminal listings remain unchanged and return controlled responses.
- [ ] Historical reservations, orders, payments, and ticket payload records are not modified.
- [ ] The seller UI reloads authoritative state after success or lifecycle conflict.

## Focused Issues

1. `#125` — Define seller active-listing edit contract.
2. `#126` — Implement seller active-listing edit backend.
3. `#127` — Build seller active-listing edit flow.

## Delivery Order

1. Approve the endpoint, editable fields, validation, locking, response, audit, and error contract in `#125`.
2. Implement the protected backend transition in `#126`.
3. Add the seller-facing edit flow after the own-listings surface and backend are available in `#127`.

## Concerns

- Public buyer pages may temporarily display a stale listing snapshot; later reservation and checkout operations must continue to use authoritative server state.
- A price update racing reservation creation must be resolved by the same listing-first lock so a reservation cannot observe a partially updated listing.
- Historical terminal orders may contain prior price snapshots and must never be rewritten.
- Audit rows must not include old or new free-text values, ticket metadata, buyer details, or payment data.
- Reusing creation-form controls must not accidentally make event selection editable.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, or other verification commands in these issues. Complete application implementation first; verification will be handled later as a separate final phase.