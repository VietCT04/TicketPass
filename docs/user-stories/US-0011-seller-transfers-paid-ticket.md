# US-0011 — Seller Transfers A Paid Ticket

## User Story

As a seller, I want to transfer a paid ticket within the allowed window and confirm that I completed the transfer so that the buyer can verify receipt and the order can progress toward settlement.

## Context

A trusted payment success confirms only that payment was completed. It does not prove that the seller transferred the ticket or that the buyer received it.

After trusted payment completion, TicketPass gives the seller a server-controlled 15-minute window to complete the ticket transfer. The seller must then explicitly confirm that the transfer was performed. Seller confirmation records fulfilment progress only; it does not confirm buyer receipt and does not release held funds.

## Scope

- Start the seller transfer window from trusted `paid_at`.
- Derive `transfer_deadline_at = paid_at + 15 minutes` using server time.
- Keep payment, transfer, and settlement statuses separate.
- Create the approved awaiting-seller-transfer state after trusted payment success.
- Let only the authenticated order seller confirm transfer.
- Record an immutable seller-confirmed timestamp.
- Make repeated valid confirmation idempotent.
- Show safe transfer progress and the authoritative deadline to buyer and seller.
- Keep settlement held after seller confirmation.
- Prevent browser countdowns, redirects, or client state from changing transfer status.
- Exclude identities, provider records, credentials, and sensitive ticket payloads from responses and logs.

## Out of Scope

- Buyer receipt confirmation.
- Settlement release or production payout-provider integration.
- Seller transfer timeout reconciliation.
- Refunds, disputes, chargebacks, or admin tooling.
- Ticket upload, storage, QR/barcode handling, or automated proof verification.
- Buyer-seller messaging or communication tooling.

## Acceptance Criteria

- [ ] A trusted paid order enters the approved awaiting-seller-transfer state.
- [ ] The transfer deadline is exactly 15 minutes after trusted payment completion.
- [ ] The deadline is derived from server time and cannot be extended by the client.
- [ ] Only the authenticated order seller can confirm transfer.
- [ ] Seller confirmation records an immutable timestamp.
- [ ] Repeated valid confirmation is idempotent.
- [ ] Seller confirmation does not imply buyer receipt.
- [ ] Seller confirmation does not release held funds.
- [ ] Browser countdowns and redirects cannot change authoritative transfer state.
- [ ] Safe buyer and seller progress excludes identities, provider records, credentials, and sensitive ticket payloads.
- [ ] Relevant API, database, security, lifecycle, concern, and continuity documentation is updated by the focused issues.

## Focused Issues

- `#92` — Define post-payment ticket transfer lifecycle.
- `#93` — Implement seller ticket transfer confirmation backend.
- `#94` — Build seller ticket transfer confirmation flow.

## Approved Lifecycle Contract

Issue `#92` keeps payment, transfer, and settlement as separate dimensions through a future one-to-one fulfilment record. Trusted payment success creates `AWAITING_SELLER_TRANSFER` and `FUNDS_HELD` with an immutable server-derived deadline exactly 15 minutes after `paid_at`. The seller confirmation endpoint is bodyless, seller-authorized, and idempotent; it records only the seller's claim and cannot confirm buyer receipt or release settlement. Backend persistence and the endpoint remain issue `#93`.

## Delivery Order

1. Define and approve the transfer-status, persistence, deadline, authorization, privacy, and API contract in `#92`.
2. Implement fulfilment persistence, trusted payment initialization, and seller confirmation in `#93`.
3. Build the protected seller transfer-confirmation flow in `#94`.
4. Continue with buyer receipt confirmation through `#95`–`#97`.
5. Add timeout and review handling through `#98`–`#100`.

## Concerns

- Existing paid orders require a deterministic migration and fulfilment backfill rule.
- Migration numbering must be coordinated with other schema work.
- The transfer deadline must never be derived from browser time, provider redirects, or user-submitted timestamps.
- Seller confirmation is only the seller's assertion and is not automated proof that the buyer received the ticket.
- Lock ordering must remain aligned with checkout and payment reconciliation to avoid deadlocks.
- A missed seller deadline requires the separate timeout and review behavior defined by `US-0013`.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, or other verification commands for this documentation story. Verification remains deferred to the final application phase.
