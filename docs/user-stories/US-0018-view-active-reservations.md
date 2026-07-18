# US-0018 — View And Resume Active Reservations

## User Story

As a buyer, I want to find my current ticket holds after leaving or refreshing an event page so that I can continue checkout or release an unused reservation before it expires.

## Context

The current event-detail flow stores a newly created reservation in page memory. The server hold remains valid after navigation or refresh, but the buyer loses the visible countdown and actions.

TicketPass needs a protected account-level view for active reservations that have not entered checkout. Once an order exists, the checkout and buyer order-progress flows remain authoritative.

## Scope

- Provide an authenticated view of the current buyer's active, unexpired reservations that have no order.
- Keep reservation ownership, expiry, listing state, and order exclusion server-authoritative.
- Show approved event, listing summary, price, and expiry information.
- Order the most urgent expirations first with bounded pagination.
- Let the buyer continue through the existing checkout-start flow.
- Let the buyer release an eligible reservation through the approved reservation-release flow.
- Refresh authoritative state after expiry, release, checkout start, or lifecycle conflict.
- Keep countdowns as presentation only.
- Keep reservation data out of browser persistence.
- Exclude seller identity, provider records, credentials, and sensitive ticket payloads.

## Out of Scope

- Historical expired or cancelled reservation records.
- Reservations that already have an order.
- New checkout, payment, refund, dispute, ticket-transfer, or settlement behavior.
- Reservation extension, renewal, replacement, or automatic recreation.
- Seller or admin reservation views.
- Notifications, analytics, exports, or broad account-dashboard redesign.

## Acceptance Criteria

- [ ] An authenticated buyer can view only their active unexpired pre-checkout reservations.
- [ ] Reservations with orders are excluded and remain owned by checkout/order flows.
- [ ] Pagination and ordering are enforced by the server.
- [ ] Event, listing, price, and expiry fields are safe and sufficient for identification.
- [ ] Buyers can continue checkout through the existing protected action.
- [ ] Buyers can release a reservation only through the approved backend action.
- [ ] Countdown expiry and lifecycle conflicts trigger authoritative refreshes.
- [ ] No private identity, provider, credential, or ticket payload data is returned, rendered, or persisted.

## Focused Issues

- `#121` — Define buyer active-reservations contract.
- `#122` — Implement buyer active-reservations backend.
- `#123` — Build buyer active-reservations page.

## Delivery Order

1. Complete `#121` to approve the API, eligibility, pagination, privacy, and response contract.
2. Complete `#122` to implement the protected database-backed read.
3. Complete `#123` after `#122` and reservation release backend `#118` are available.

## Concerns

- The schema allows multiple simultaneous holds by one buyer, so the view must remain bounded and paginated.
- Expired rows can remain stored as `ACTIVE` briefly until existing reconciliation runs; they must still be excluded using server time.
- A reservation can enter checkout after the page loads, so every mutation must revalidate current state.
- The page must not duplicate order-backed checkout recovery or the broader buyer order-progress work.
- Browser countdowns may differ from server time and must never mutate reservation or listing state.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, or other verification commands while implementing this story. Complete application implementation first; verification will be handled later as a separate final phase.
