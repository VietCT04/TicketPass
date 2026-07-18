# US-0026: Reconcile Event Cancellations And Reschedules

## User Story

As a marketplace operator, I want TicketPass to record an event cancellation or material reschedule and safely reconcile every affected marketplace record so that buyers and sellers cannot continue using stale event information and financial outcomes remain accurate.

## Context

TicketPass currently treats catalogue events as permanently scheduled. Listings, reservations, checkout, payment, ticket transfer, settlement, and disputes all depend on the event remaining valid, but no lifecycle exists for a cancelled, moved, or rescheduled event.

A material event change can affect:

- public event visibility;
- active and reserved listings;
- pre-checkout reservations;
- unpaid and payment-pending orders;
- paid orders with held settlement;
- ticket transfer and receipt-confirmation actions;
- refunds and already released settlement;
- seller listing validity;
- buyer willingness to attend a revised schedule.

The change may affect many rows, so it must block new purchases immediately and then reconcile existing records through durable bounded work rather than one large request transaction.

## Scope

- Add an authoritative event lifecycle and monotonic event revision.
- Persist immutable cancellation and material-reschedule history.
- Allow only authenticated administrators to preview and start an event change.
- Treat start-time, venue, or city changes as material reschedules.
- Block new listings, reservations, and checkout immediately when a change begins.
- Process affected marketplace records through restartable idempotent batches.
- Permanently cancel unsold listings for cancelled events.
- Suspend unsold listings after reschedule until their sellers reconfirm the latest event revision.
- Cancel active reservations and unpaid orders safely without reactivating invalid listings.
- Prevent late payment completion from silently selling a changed-event listing.
- Route eligible cancelled-event paid orders to a full refund.
- Pause eligible rescheduled paid orders and let buyers accept the new schedule or request a full refund.
- Default unanswered reschedule decisions to the safer full-refund outcome.
- Keep ordinary buyer confirmation and settlement release blocked while an order is affected.
- Surface already released settlement as manual action required rather than claiming an automatic reversal.
- Deliver private in-app notifications for required actions and progress.
- Provide admin, buyer, and seller interfaces using server-authoritative state.
- Preserve safe audit, privacy, logging, caching, and concurrency boundaries.

## Out of Scope

- External event-provider integrations or automatic cancellation detection.
- Email, SMS, push, calendar, or webhook notifications.
- Partial refunds, chargebacks, seller clawbacks, negative balances, insurance, or legal recovery.
- Ticket-file inspection, QR/barcode validation, or automated transfer verification.
- Arbitrary event editing, event deletion, event merging, organizer accounts, or bulk catalogue import.
- Reversing a completed cancellation or reopening a completed event change.
- Chat, attachments, off-platform contact exchange, or appeals.

## Acceptance Criteria

- [ ] An administrator can preview and start a cancellation or material reschedule against the current event revision.
- [ ] Starting a change immediately removes the event from new marketplace activity.
- [ ] Exactly one nonterminal change can exist for an event.
- [ ] Event changes and revisions are immutable and auditable.
- [ ] Reconciliation is bounded, restartable, idempotent, and safe across application instances.
- [ ] Cancelled events leave no active unsold listing, active reservation, or payable unpaid order.
- [ ] Rescheduled listings remain hidden until seller reconfirmation of the latest revision.
- [ ] Eligible paid cancelled-event orders enter one recoverable full-refund path.
- [ ] Eligible paid rescheduled orders require an explicit buyer choice and default safely to refund.
- [ ] Refund and seller settlement release cannot both complete for the same affected order.
- [ ] Released-settlement impacts are clearly marked for manual action.
- [ ] Buyers and sellers receive private in-app notifications and see only their own affected resources.
- [ ] Admin and user interfaces refresh from authoritative server state after every action or conflict.
- [ ] Sensitive ticket, payment, identity, session, and provider data are excluded from notifications, logs, and audit records.

## Focused Issues

- `#162` Define event cancellation and reschedule reconciliation contract.
- `#163` Implement event lifecycle and durable change processing.
- `#164` Reconcile listings, reservations, and unpaid orders for event changes.
- `#165` Reconcile paid orders for event cancellations and reschedules.
- `#166` Implement seller reconfirmation and buyer reschedule choices.
- `#167` Implement in-app event-change notifications.
- `#168` Build the admin event-change operations console.
- `#169` Build the buyer and seller event-change experience.

## Delivery Order

1. Approve the complete lifecycle and reconciliation contract in `#162`.
2. Implement event revisions, lifecycle, change persistence, and durable work ownership in `#163`.
3. Implement inventory and unpaid-order reconciliation in `#164`.
4. Implement paid-order refund, buyer-choice, and manual-action routing in `#165`.
5. Implement seller and buyer decision endpoints in `#166`.
6. Implement private notification persistence and APIs in `#167`.
7. Build the admin operations console in `#168`.
8. Build the buyer and seller experience in `#169`.

Issues `#164` and `#165` may proceed in parallel after `#163` when their shared migration and lock-order decisions are coordinated. Frontend work begins only after its required backend contracts are implemented.

## Concerns

- Payment webhooks, reservation expiry, seller transfer confirmation, buyer receipt confirmation, refunds, and settlement release may race the event change.
- Large events require indexed cursor-based processing and durable progress rather than offset pagination.
- A second reschedule may supersede outstanding seller or buyer decisions.
- Event cancellation after settlement release requires future recovery or clawback capability outside this MVP.
- Organizer-specific ticket validity after reschedule cannot be verified automatically.
- Notification and admin messages remain untrusted text and can become stale after later actions.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, image builds, Compose startup, or other verification commands in this story. Complete application implementation first; verification will be handled later as a separate final phase.
