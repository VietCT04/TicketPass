# US-0013 — Handle Transfer Timeout And Manual Review

## User Story

As a buyer or seller, I want TicketPass to show a safe server-authoritative outcome when ticket delivery is not completed normally so that settlement is not released while fulfilment remains unresolved.

## Context

The happy path does not cover a seller missing the 15-minute deadline or a buyer reporting that the ticket was not received after seller confirmation. These outcomes require explicit timeout and review states before production refund or support tooling exists.

## Scope

- Reconcile a missed seller transfer deadline using server time.
- Block settlement completion for timed-out or unresolved fulfilment.
- Define buyer non-receipt reporting after seller confirmation.
- Keep timeout, manual-review, and refund-required status separate from actual refund execution.
- Show safe progress to buyer and seller.
- Preserve immutable lifecycle timestamps and controlled reason categories.
- Keep ticket payloads, identities, and provider internals out of responses and logs.

## Out of Scope

- Production refund execution.
- Chargeback processing, fraud scoring, legal arbitration, or support/admin UI.
- Automated judgement from ticket payloads or user-entered evidence.
- Ticket upload, QR/barcode inspection, or communication tooling.

## Acceptance Criteria

- [ ] Missing the seller deadline produces an approved server-authoritative outcome.
- [ ] Unresolved fulfilment prevents settlement release.
- [ ] Buyer non-receipt can be represented without automatically deciding fault.
- [ ] Refund-required state is separate from actual refund execution.
- [ ] Buyer and seller receive safe non-misleading progress information.

## Focused Issues

- `#98` — Define post-payment timeout handling.
- `#99` — Implement post-payment timeout reconciliation.
- `#100` — Show transfer timeout and review progress.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, or other verification commands for this story. Verification remains deferred.