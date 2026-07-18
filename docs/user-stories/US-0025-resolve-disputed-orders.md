# US-0025 — Resolve Disputed And Review-Held Orders

## User Story

As a buyer or seller involved in a paid order that cannot complete normally, I want a structured and impartial resolution process so that settlement remains protected and the order ends with exactly one authoritative outcome: a full buyer refund or release to the seller.

## Context

TicketPass already defines payment, ticket-transfer, buyer-confirmation, settlement, timeout, and review states as separate lifecycle dimensions. Normal completion releases held settlement only after buyer receipt confirmation, while missed seller deadlines or inconsistent delivery can place an order into review.

The current lifecycle intentionally stops at `REQUIRES_REVIEW` and `REVIEW_REQUIRED`. It does not define buyer-raised disputes, seller responses, admin investigation, refund execution, terminal review decisions, or user-facing resolution progress. Without this capability, review-held funds have no safe operational exit.

This story introduces one private resolution-case model for both buyer disputes and system-created timeout reviews. It keeps the order amount fixed, prevents competing refund and release paths, and establishes recoverable provider operations without handling ticket files or payment credentials.

## Scope

- Introduce one private resolution case per paid order.
- Support case origins for buyer dispute, seller-transfer timeout, and approved system inconsistency review.
- Let an eligible buyer open or recover a case while settlement remains held or under review.
- Collect one bounded reason code and optional bounded buyer statement.
- Create or recover the same case when timeout reconciliation moves an order into review.
- Move eligible orders into review-held state and block ordinary buyer confirmation and settlement release.
- Let buyers and sellers list and inspect only cases connected to their own orders.
- Let the seller submit one bounded structured response before terminal resolution.
- Add an admin-only queue and detail workflow using the admin authorization foundation from US-0024.
- Let an admin record exactly one terminal decision:
  - full refund to the buyer; or
  - release settlement to the seller.
- Keep the order amount and original currency authoritative; no client or admin chooses a different amount.
- Add provider-neutral, durable, idempotent full-refund execution with development mock behavior.
- Separate refund authorization from provider-confirmed refund completion.
- Keep refund and seller-release execution mutually exclusive.
- Preserve immutable case, decision, financial-operation, and audit history.
- Show server-authoritative resolution and financial progress to buyers, sellers, and admins.
- Keep every statement, reason, and summary as untrusted bounded text.
- Keep case data out of public APIs, browser persistence, analytics, and broad logs.
- Reuse established marketplace locking and no-store response rules.

## Out of Scope

- Partial refunds, credits, coupons, split decisions, or negotiated amounts.
- Payment-provider chargebacks, card-network arbitration, legal claims, or external support-case integration.
- File or image attachments, screenshots, ticket PDFs, QR codes, barcodes, private transfer links, or automated evidence inspection.
- Real-time messaging, repeated conversation threads, email, SMS, push notifications, or contact sharing.
- Appeals, reopening terminal cases, internal admin notes, case assignment, or SLA escalation.
- Automatic event-cancellation handling or bulk dispute creation.
- Production payment/refund provider selection or production seller-payout onboarding.
- Changing catalogue events, listings, or ticket payloads through the resolution flow.
- Public dispute history, reputation scoring, analytics, or fraud scoring.

## Acceptance Criteria

- [ ] A paid order can have at most one resolution case.
- [ ] Buyer disputes and timeout review converge on the same case model safely.
- [ ] Only an eligible buyer can open a buyer-origin case.
- [ ] Opening a case prevents ordinary receipt confirmation and settlement release.
- [ ] Buyers and sellers can view only their own party-safe case data.
- [ ] An eligible seller can submit one bounded response.
- [ ] Only an admin can apply a terminal resolution decision.
- [ ] A case ends with exactly one outcome: buyer refund or seller release.
- [ ] Full refund execution uses the paid order amount and original currency.
- [ ] Refund and release operations are idempotent, durable, and recoverable.
- [ ] Provider-pending, completed, retryable-failure, and operator-review states are distinguished.
- [ ] Audit records contain identifiers and actions but no statements, credentials, ticket payloads, or contact data.
- [ ] Buyer, seller, and admin interfaces remain server-authoritative and use no browser persistence.

## Focused Issues

1. `#153` — Define order dispute and resolution contract.
2. `#154` — Implement order resolution case persistence and opening.
3. `#155` — Implement buyer and seller case views.
4. `#156` — Implement admin order-case review and decisions.
5. `#157` — Implement recoverable refund execution.
6. `#158` — Build buyer and seller dispute experience.
7. `#159` — Build admin order-resolution console.

## Delivery Order

1. Approve the complete case, lifecycle, API, financial, privacy, and audit contract in `#153`.
2. Implement durable case storage plus buyer and timeout-origin case creation in `#154`.
3. Implement party-owned reads and the seller response in `#155`.
4. Implement admin queue, inspection, and mutually exclusive decisions in `#156`.
5. Implement recoverable full-refund execution and settlement finalization in `#157`.
6. Build buyer and seller case workflows in `#158`.
7. Build the admin resolution console in `#159`.

## Concerns

- Buyer case creation and timeout reconciliation can race; database uniqueness and common locking must converge on one case.
- An external refund timeout does not prove failure; durable provider idempotency and reconciliation are mandatory.
- A seller release operation may already be in flight when a case is opened, so every transition must inspect durable operation state.
- Existing review-held orders require deterministic backfill or lazy case creation without inventing user statements or admin decisions.
- Statements may contain sensitive data despite warnings and therefore need strict bounds, escaping, privacy guidance, and logging exclusions.
- Mock refund behavior can validate application control flow but must not be represented as production payment readiness.
- Terminal financial decisions are irreversible in this story; the UI and APIs must communicate that clearly.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, or other verification commands in these issues. Complete application implementation first; verification will be handled later as a separate final phase.