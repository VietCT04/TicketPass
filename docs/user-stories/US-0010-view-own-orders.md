# US-0010 — View Order Progress

## User Story

As a buyer, I want to view the orders created from my reservations so that I can understand the current payment, ticket-transfer, and settlement progress and safely complete any required action.

## Context

TicketPass already stores buyer-owned checkout orders and exposes a protected single-order read. The marketplace flow continues after payment: the seller receives a server-controlled transfer window, confirms transfer, the buyer confirms receipt, and only then may held funds be released through the approved settlement boundary.

This story provides a read-only account overview of that complete progress. The mutations are owned by separate stories:

- `US-0011` — seller transfers and confirms the paid ticket;
- `US-0012` — buyer confirms receipt and authorizes settlement release;
- `US-0013` — timeout and unresolved-delivery handling.

## Progress Dimensions

Order progress must keep three dimensions separate:

```text
payment status
transfer status
settlement status
```

A paid order is not necessarily transferred or completed. Seller transfer confirmation is not buyer receipt confirmation, and it must not release funds by itself.

## Scope

- Provide an authenticated paginated view of orders owned by the current buyer.
- Show approved event, ticket, amount, timestamp, payment, transfer, and settlement metadata.
- Show the server-derived seller transfer deadline when applicable.
- Support exact filtering by approved lifecycle statuses without loading and filtering in browser memory.
- Keep ownership, filtering, ordering, and pagination server-side.
- Let eligible pending-payment orders link to the protected checkout route.
- Let later fulfilment stories surface their approved buyer actions from the order view.
- Keep hosted payment URLs, provider records, identities, and sensitive ticket payloads out of the history response and browser storage.
- Use non-misleading labels such as awaiting payment, awaiting seller transfer, confirm receipt, completed, timed out, or review required.

## Out of Scope

- Starting payment directly from the history endpoint.
- Seller transfer confirmation implementation.
- Buyer receipt confirmation or settlement release implementation.
- Timeout resolution, refunds, chargebacks, disputes, or admin tooling.
- Ticket upload, storage, QR/barcode processing, or automated delivery verification.
- A broad account-dashboard redesign.

## Acceptance Criteria

- [ ] An authenticated buyer can retrieve only their own orders.
- [ ] Orders are paginated and deterministically ordered by the server.
- [ ] Payment, transfer, and settlement progress are shown as separate dimensions.
- [ ] The seller transfer deadline is server-derived and presentation-only in the browser.
- [ ] Empty results and pages beyond the final page are handled safely.
- [ ] The response and UI exclude seller identity, provider records, payment URLs, credentials, and ticket payload data.
- [ ] Eligible pending-payment orders can navigate to the protected checkout route.
- [ ] `PAID` is not presented as ticket delivery, seller payout, settlement release, refund eligibility, or dispute completion.
- [ ] Seller confirmation is not presented as buyer receipt or settlement release.
- [ ] Order progress is not stored in `localStorage` or `sessionStorage`.

## Focused Issues

- `#87` — Define buyer order-progress contract.
- `#88` — Implement buyer order-progress backend.
- `#89` — Build buyer order-progress page.

Related lifecycle stories:

- `#92`–`#94` — seller ticket transfer.
- `#95`–`#97` — buyer receipt confirmation and settlement release.
- `#98`–`#100` — transfer timeout and manual-review progress.

## Delivery Order

1. Approve lifecycle contracts `#92`, `#95`, and `#98`.
2. Complete the order-progress read contract in `#87`.
3. Implement lifecycle persistence through `#93`, `#96`, and `#99`.
4. Implement the order-progress backend in `#88`.
5. Build the read-only buyer page in `#89`.
6. Add the focused seller, buyer-confirmation, and timeout UI flows through `#94`, `#97`, and `#100`.

## Concerns

- List reads must avoid N+1 loading and unbounded per-order reconciliation.
- Browser countdowns and cached list state are never authoritative.
- Actual production fund release requires an approved settlement-provider boundary and must not be implied by mock development behavior.
- Timeout, non-receipt, and review outcomes must block settlement completion until their approved resolution.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, or other verification commands for this documentation story. Verification remains deferred to the final application phase.