# US-0011 — Seller Transfers A Paid Ticket

## User Story

As a seller, I want to transfer a paid ticket within the allowed window and confirm that I completed the transfer so that the buyer can verify receipt and the order can progress toward settlement.

## Context

A trusted payment success only confirms payment. It does not prove that the seller transferred the ticket. After `paid_at`, TicketPass gives the seller 15 minutes to complete the transfer.

## Scope

- Start the seller transfer window from trusted `paid_at`.
- Derive `transfer_deadline_at = paid_at + 15 minutes` using server time.
- Keep payment and transfer statuses separate.
- Let only the authenticated order seller confirm transfer.
- Record an immutable seller-confirmed timestamp.
- Make repeated valid confirmation idempotent.
- Show safe transfer progress to buyer and seller.
- Keep settlement held after seller confirmation.

## Out of Scope

- Buyer receipt confirmation.
- Fund release or production payout integration.
- Refunds, timeout resolution, disputes, or admin tooling.
- Ticket upload, QR/barcode storage, or automated proof verification.

## Acceptance Criteria

- [ ] A paid order enters an awaiting-seller-transfer state.
- [ ] The transfer deadline is exactly 15 minutes after trusted payment completion.
- [ ] Only the order seller can confirm transfer.
- [ ] Seller confirmation does not release funds.
- [ ] Browser countdowns cannot change server state.
- [ ] Sensitive buyer and ticket payload data is not exposed.

## Focused Issues

- `#92` — Define post-payment ticket transfer lifecycle.
- `#93` — Implement seller ticket transfer confirmation backend.
- `#94` — Build seller ticket transfer confirmation flow.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, or other verification commands for this story. Verification remains deferred.