# US-0020 — Save Events to a Watchlist

## User Story

As a buyer, I want to save upcoming events so that I can return later and check whether tickets are available without searching again.

## Context

TicketPass already provides public event browsing and event detail pages. Those surfaces are availability-driven, so an event may disappear from normal browse results when its active listings sell out.

A private watchlist should preserve the buyer's saved event even when current listing availability becomes zero. It must remain separate from reservations, checkout, notifications, recommendations, and public popularity metrics.

## Scope

- Let an authenticated buyer save a future event that is publicly discoverable at save time.
- Let the same buyer remove their saved event.
- Provide a protected paginated list of the buyer's upcoming saved events.
- Store one private watchlist entry per user/event pair.
- Keep an event saved when its active listing count later falls to zero.
- Return current active listing count and lowest current VND asking price as server-derived snapshots.
- Use idempotent save and removal behavior.
- Order saved events by newest saved first with a deterministic event-ID tie-breaker.
- Add save/remove controls to existing event surfaces and a protected `/saved-events` page.
- Keep watchlist state out of browser persistence.
- Exclude buyer identity, seller identity, private listing data, payment data, and ticket payload data from responses and UI.

## Out of Scope

- Email, push, SMS, or in-app availability notifications.
- Price alerts, recommendations, personalization, analytics, or public popularity counts.
- Saving individual listings.
- Public or shared watchlists.
- Reservation, checkout, payment, refund, dispute, transfer, or settlement behavior.
- Historical attended-event collections.

## Acceptance Criteria

- [ ] An authenticated buyer can save an eligible upcoming event.
- [ ] Duplicate saves do not create another row or change the original saved timestamp.
- [ ] The buyer can remove a saved event idempotently.
- [ ] The protected watchlist returns only the authenticated buyer's upcoming saved events.
- [ ] A saved event remains visible when it has no active listings.
- [ ] Listing count and lowest price are derived server-side from current eligible listings.
- [ ] Pagination and ordering are deterministic and bounded.
- [ ] Watchlist responses and UI exclude private identities and sensitive ticket or payment data.
- [ ] No watchlist data is stored in browser persistence.

## Focused Issues

1. `#129` — Define buyer event watchlist contract.
2. `#130` — Implement buyer event watchlist backend.
3. `#131` — Build buyer event watchlist experience.

## Delivery Order

1. Approve the API, persistence, eligibility, idempotency, availability, pagination, privacy, and response contract in `#129`.
2. Implement the persistence and protected endpoints in `#130`.
3. Add save controls and `/saved-events` after the backend is available in `#131`.

## Concerns

- Save eligibility must reuse public marketplace visibility rules rather than trust browser state.
- Availability and lowest price may change immediately after a response and must not imply a reservation or guarantee.
- Past events should be excluded from the active watchlist without destructive cleanup during read requests.
- Sold-out saved events must remain clear and useful without implying future ticket supply.
- Watchlist ownership and behavior must remain private and must not become a public demand metric.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, or other verification commands in these issues. Complete application implementation first; verification will be handled later as a separate final phase.
