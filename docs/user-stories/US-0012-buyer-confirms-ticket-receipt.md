# US-0012 — Buyer Confirms Ticket Receipt

## User Story

As a buyer, I want to confirm that I received the transferred ticket so that TicketPass can complete fulfilment and release the held settlement to the seller.

## Context

Seller transfer confirmation is only the seller's assertion that the transfer was performed. It does not prove buyer receipt and cannot release held funds by itself.

After the seller confirms transfer, the authenticated buyer must verify that the ticket was received. A valid buyer confirmation completes the happy-path fulfilment flow and authorizes settlement release through an approved provider-neutral boundary.

## Scope

- Keep payment, transfer, and settlement statuses separate.
- Let only the authenticated order buyer confirm receipt.
- Allow confirmation only after trusted payment and seller transfer confirmation.
- Block confirmation during timeout, review, or other ineligible states.
- Record immutable buyer-confirmed and settlement-release timestamps.
- Make repeated successful confirmation idempotent.
- Release held settlement through an approved provider-neutral boundary.
- Define recoverable behavior when an external release attempt does not complete normally.
- Support development mock behavior without representing it as production payout.
- Show safe completed progress to buyer and seller.
- Exclude identities, provider records, credentials, and sensitive ticket payloads from responses and logs.

## Out of Scope

- Automatic buyer confirmation.
- Buyer confirmation before seller transfer confirmation.
- Negative receipt reporting or dispute submission.
- Production payout-provider selection or seller onboarding.
- Timeout resolution, refunds, disputes, chargebacks, or admin tooling.
- Ticket payload inspection, upload, or automated proof verification.

## Acceptance Criteria

- [ ] Only the authenticated order buyer can confirm receipt.
- [ ] Confirmation is unavailable before seller transfer confirmation.
- [ ] Confirmation is blocked during timeout, review, or other ineligible states.
- [ ] Seller confirmation alone cannot release held settlement.
- [ ] Buyer-confirmed and settlement-release timestamps are immutable.
- [ ] Repeated confirmation is idempotent and cannot release settlement twice.
- [ ] External release attempts use a stable idempotency boundary and recover safely from partial failure.
- [ ] A failed external release attempt does not falsely mark settlement as released.
- [ ] Development mock behavior is not presented as production payout.
- [ ] Safe buyer and seller progress excludes identities, provider records, credentials, and sensitive ticket payloads.
- [ ] Relevant API, database, security, lifecycle, concern, and continuity documentation is updated by the focused issues.

## Focused Issues

- `#95` — Define buyer receipt confirmation and fund release contract.
- `#96` — Implement buyer receipt confirmation and settlement release.
- `#97` — Build buyer ticket receipt confirmation flow.

## Delivery Order

1. Complete and approve the seller transfer lifecycle in `#92`.
2. Issue `#95` defines the buyer-confirmation, settlement-status, idempotency, provider-boundary, and privacy contract.
3. Buyer confirmation and provider-neutral settlement release are implemented in `#96` after seller transfer persistence in `#93`; production payout, UI, and recovery operations remain separate work.
4. Build the protected buyer receipt-confirmation action in `#97` after the order-progress page in `#89` exists.
5. Integrate timeout and review restrictions from `#98`–`#100`.

## Concerns

- A database transaction cannot safely assume that an external settlement-provider operation commits atomically with local persistence.
- Provider idempotency keys must remain stable across retries to prevent duplicate release.
- A partial external failure needs an explicit recoverable state rather than being reported as completed.
- Buyer confirmation must clearly explain that it authorizes release and should not be treated as a reversible UI-only action.
- Negative receipt reporting and dispute resolution require separate approved product and operational workflows.
- Production payout behavior must not be implied by development-only mock implementation.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, or other verification commands for this documentation story. Verification remains deferred to the final application phase.
