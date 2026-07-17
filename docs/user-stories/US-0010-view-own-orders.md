# US-0010 — View Own Orders

## User Story

As a buyer, I want to view the orders created from my reservations so that I can understand their current payment status and safely return to an eligible pending checkout.

## Context

TicketPass already stores buyer-owned orders and exposes a protected single-order read for checkout recovery. Buyers currently have no account-level view of those orders after leaving the checkout route.

This story adds a read-only, server-authoritative order history. It does not introduce payment mutation, ticket delivery, seller payout, refunds, or dispute behavior.

## Scope

- Provide an authenticated paginated view of orders owned by the current buyer.
- Show approved event, ticket, amount, status, and timestamp metadata.
- Support exact filtering by the existing bounded order statuses.
- Keep ownership, filtering, ordering, and pagination server-side.
- Let eligible pending orders link to the protected checkout route.
- Keep hosted payment URLs and provider records out of the history response and browser storage.
- Clearly distinguish payment status from ticket delivery, payout, escrow, refund, and dispute outcomes.

## Out of Scope

- Starting or retrying hosted payment directly from the history endpoint or page.
- Embedded payment forms or raw payment credentials.
- Refunds, chargebacks, disputes, invoices, receipts, seller payout, or escrow.
- Ticket upload, storage, transfer, delivery, or reveal.
- Order cancellation, deletion, mutation, export, analytics, or admin tooling.
- A broad account-dashboard redesign.

## Acceptance Criteria

- [ ] An authenticated buyer can retrieve only their own orders.
- [ ] Orders are paginated and deterministically ordered by the server.
- [ ] Buyers can filter by one approved order status.
- [ ] Empty results and pages beyond the final page are handled safely.
- [ ] The response and UI exclude seller identity, provider records, payment URLs, credentials, and ticket payload data.
- [ ] Eligible pending orders can navigate to the protected checkout route.
- [ ] `PAID` is not presented as ticket delivery, seller payout, escrow release, refund eligibility, or dispute completion.
- [ ] Order data is not stored in `localStorage` or `sessionStorage`.

## Focused Issues

- `#87` — Define buyer own-orders contract.
- `#88` — Implement buyer own-orders backend.
- `#89` — Build buyer own-orders page.

## Delivery Order

1. Complete `#87`.
2. Implement `#88` after the contract is approved.
3. Implement `#89` after `#88` and the protected checkout route in `#71` are available.

Contract work for this story can proceed independently of checkout UI implementation, missing-event requests, and seller own-listings work.

## Concerns

- List reads must avoid N+1 loading and unbounded per-order reconciliation.
- Pending status can become stale near expiry, so the contract must define a bounded read policy without turning the list endpoint into a bulk payment processor.
- Existing buyer indexes may be sufficient for MVP; composite indexing should follow an approved query need rather than be added speculatively.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, or other verification commands for this documentation story. Verification remains deferred to the final application phase.