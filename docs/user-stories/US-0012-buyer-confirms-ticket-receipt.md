# US-0012 — Buyer Confirms Ticket Receipt

## User Story

As a buyer, I want to confirm that I received the transferred ticket so that TicketPass can complete fulfilment and release the held settlement to the seller.

## Context

Seller transfer confirmation is only the seller's assertion that the transfer was performed. Funds remain held until the authenticated buyer confirms receipt through the approved server-side flow.

## Scope

- Let only the authenticated order buyer confirm receipt.
- Allow confirmation only after trusted payment and seller transfer confirmation.
- Keep payment, transfer, and settlement statuses separate.
- Record immutable buyer-confirmed and settlement-release timestamps.
- Make repeated successful confirmation idempotent.
- Release settlement through a provider-neutral boundary.
- Support mock development behavior without presenting it as production payout.
- Show safe completed progress to buyer and seller.

## Out of Scope

- Automatic buyer confirmation.
- Buyer confirmation before seller transfer confirmation.
- Production payout-provider selection.
- Negative receipt reporting, refunds, disputes, chargebacks, or timeout resolution.
- Ticket payload inspection or automated proof verification.

## Acceptance Criteria

- [ ] Only the order buyer can confirm receipt.
- [ ] Confirmation is unavailable before seller transfer confirmation.
- [ ] Seller confirmation alone cannot release settlement.
- [ ] Buyer confirmation and release are atomic or recoverably idempotent.
- [ ] Repeated confirmation cannot release settlement twice.
- [ ] Production payout is not falsely represented by mock behavior.

## Focused Issues

- `#95` — Define buyer receipt confirmation and fund release contract.
- `#96` — Implement buyer receipt confirmation and settlement release.
- `#97` — Build buyer ticket receipt confirmation flow.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, or other verification commands for this story. Verification remains deferred.