# US-0016: Cancel Own Unsold Listing

## User Story

As a seller, I want to cancel an unsold listing that I no longer want to offer so that buyers can no longer reserve or purchase it.

## Context

TicketPass already models `CANCELLED` as a terminal listing status and allows `ACTIVE -> CANCELLED` in the documented lifecycle. However, no authenticated seller cancellation API or frontend action exists.

Cancellation must be server-authoritative and must never invalidate an active buyer reservation, checkout, payment, or completed sale.

## Scope

- Allow an authenticated seller to cancel only their own `ACTIVE` listing.
- Use an explicit no-body cancellation action rather than deleting the listing record.
- Make cancellation atomic with concurrent buyer reservation creation through the existing listing-first lock order.
- Keep `RESERVED`, `SOLD`, and `EXPIRED` listings ineligible for seller cancellation.
- Treat repeated cancellation of the same already-cancelled owned listing as idempotent.
- Write a safe immutable `LISTING_CANCELLED` audit event only on the first successful transition.
- Preserve historical listing, reservation, order, payment, and audit records.
- Return controlled responses without exposing another seller's listing or buyer activity.
- Add a focused cancellation control to the seller-owned listings experience after the read-only page exists.
- Keep browser state presentation-only and reload authoritative status after cancellation or conflict.

## Out of Scope

- Cancelling a buyer reservation or a listing in `RESERVED` state.
- Payment cancellation, refunds, disputes, chargebacks, ticket transfer, or settlement behavior.
- Admin cancellation or recovery from terminal listing states.
- Editing, deleting, renewing, relisting, or duplicating listings.
- Draft listing creation or management.
- Notifications, analytics, exports, or bulk actions.

## Acceptance Criteria

- [ ] Only the authenticated owning seller may cancel an `ACTIVE` listing.
- [ ] Cancellation transitions the listing to terminal `CANCELLED` without deleting it.
- [ ] Cancellation and reservation creation cannot both succeed for the same `ACTIVE` listing.
- [ ] `RESERVED`, `SOLD`, and `EXPIRED` listings remain unchanged.
- [ ] Repeated cancellation of an already-cancelled owned listing is idempotent.
- [ ] The first successful transition writes one safe cancellation audit event.
- [ ] Reservation, order, payment, and ticket payload records are not modified or exposed.
- [ ] Seller UI requires explicit confirmation and refreshes authoritative state.
- [ ] Relevant API, database, security, lifecycle, concern, and continuity documentation is updated.

## Focused Issues

- GitHub Issue `#113` — Define seller listing cancellation contract.
- GitHub Issue `#114` — Implement seller listing cancellation backend.
- GitHub Issue `#115` — Build seller listing cancellation control.

## Delivery Order

1. Complete `#113` to approve endpoint, authorization, lifecycle, idempotency, audit, and concurrency rules.
2. Complete `#114` to implement the protected atomic backend transition.
3. Complete `#83` and `#84` so sellers can view their own listings.
4. Complete `#115` to add the eligible cancellation action to the seller page.

## Concerns

- A cancellation racing reservation creation must be resolved by the shared pessimistic listing lock, with exactly one transition winning.
- A `RESERVED` listing remains owned by its reservation or checkout flow until existing reconciliation restores it to `ACTIVE`; seller cancellation must not bypass that ownership.
- Conflict responses must not reveal buyer identity, reservation details, or payment state.
- Audit failure must roll back the listing transition so cancellation is never left unaudited.
- Draft and admin cancellation require separate authorization and lifecycle decisions.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, or other verification commands for this story. Complete application implementation first; verification will be handled later as a separate final phase.
