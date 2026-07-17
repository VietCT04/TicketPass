# US-0013 — Handle Transfer Timeout And Review

## User Story

As a buyer or seller, I want TicketPass to show a safe server-authoritative outcome when the seller transfer deadline is missed or fulfilment requires review so that ineligible actions are blocked while the order remains unresolved.

## Context

The happy path does not cover every paid order. A seller may miss the 15-minute transfer deadline, or persisted payment, transfer, and settlement progress may become inconsistent and require operational review.

TicketPass needs explicit timeout and review states before production resolution tooling exists. These states must preserve completed progress, block ineligible transfer or receipt-confirmation actions, and avoid promising an automatic resolution.

## Scope

- Reconcile a missed seller transfer deadline using server time.
- Define approved transfer and settlement statuses for timeout and review.
- Define request-time and bounded scheduled reconciliation ownership.
- Make timeout and review transitions idempotent and safe across multiple application instances.
- Preserve completed buyer receipt and released settlement states.
- Block seller transfer confirmation after the deadline when the state is no longer eligible.
- Block buyer receipt confirmation and settlement release while review is required.
- Show safe timeout, deadline, timestamp, and review progress to buyer and seller.
- Keep browser countdowns presentation-only.
- Exclude identities, provider internals, credentials, reason details, and sensitive ticket payloads from public progress responses and logs.

## Out of Scope

- Automatic resolution of reviewed orders.
- Buyer negative-receipt or dispute-submission workflow.
- Production refund execution.
- Chargeback processing, fraud scoring, legal arbitration, or support/admin UI.
- Ticket upload, QR/barcode inspection, evidence processing, or automated fault determination.
- Buyer-seller messaging or communication tooling.

## Acceptance Criteria

- [ ] Missing the seller deadline produces the approved server-authoritative timeout or review state.
- [ ] Deadline evaluation uses captured server time rather than browser countdowns.
- [ ] Scheduled and request-time reconciliation are idempotent.
- [ ] Multi-instance processing cannot apply conflicting transitions.
- [ ] Completed buyer receipt or released settlement progress is never reversed.
- [ ] Seller and buyer actions are blocked when the authoritative state is ineligible.
- [ ] Review progress does not promise automatic release, refund, or fault determination.
- [ ] Buyer-safe and seller-safe progress excludes identities, provider internals, credentials, and sensitive ticket payloads.
- [ ] Relevant API, database, security, lifecycle, concern, and continuity documentation is updated by the focused issues.

## Focused Issues

- `#98` — Define post-payment timeout handling.
- `#99` — Implement post-payment timeout reconciliation.
- `#100` — Show transfer timeout and review progress.

## Delivery Order

1. Approve the seller transfer lifecycle and deadline in `#92`.
2. Define timeout, review-state, transition, privacy, and reconciliation behavior in `#98`.
3. Implement bounded scheduled and request-time reconciliation in `#99` after fulfilment persistence exists in `#93`.
4. Apply approved action restrictions to buyer confirmation implementation in `#96`.
5. Show safe timeout and review progress to buyers and sellers in `#100` after the relevant account flows exist.
6. Create separate future stories for negative receipt reporting, operational review, and final financial resolution.

## Concerns

- Candidate selection and lock ordering must remain aligned with payment and fulfilment flows to avoid deadlocks.
- Scheduled reconciliation must remain safe across multiple application instances.
- Timeout processing must never overwrite completed buyer receipt or released settlement.
- Review-required orders need explicit operational ownership before production use.
- Frontend countdowns can drift and must trigger a server refresh rather than a local lifecycle transition.
- Negative receipt reporting, evidence collection, fault determination, and final financial resolution are intentionally unresolved and require separate product decisions.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, or other verification commands for this documentation story. Verification remains deferred to the final application phase.