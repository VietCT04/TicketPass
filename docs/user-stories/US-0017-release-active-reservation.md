# US-0017: Release an Active Ticket Reservation

## User Story

As a buyer, I want to release an active ticket reservation before checkout so that the ticket becomes available to other buyers when I decide not to continue.

## Context

TicketPass currently gives an authenticated buyer a server-controlled 10-minute hold and allows that buyer to continue to checkout. The reservation model already includes `CANCELLED`, but no approved buyer action defines how the hold can be released early.

A safe release flow must preserve the existing inventory and payment boundaries. It may operate only before checkout creates an order. Once an order exists, checkout reconciliation owns reservation and listing state because provider activity may already be in progress or uncertain.

## Scope

- Allow an authenticated buyer to cancel only their own active, unexpired reservation.
- Support cancellation only while no order exists for the reservation.
- Atomically transition the reservation from `ACTIVE` to `CANCELLED`.
- Reactivate the matching listing only while it remains `RESERVED` by that reservation path.
- Preserve the listing-first lock order used by reservation and checkout flows.
- Make repeated cancellation of the same already-cancelled owned reservation idempotent.
- Reconcile an already-expired active hold to `EXPIRED`, not `CANCELLED`.
- Record the first successful buyer cancellation as a safe immutable audit event.
- Add a release control to the existing held-ticket card.
- Keep all state server-authoritative and out of browser persistence.

## Out of Scope

- Cancelling an order, checkout, payment session, payment event, paid order, or sold listing.
- Reservation extension, renewal, replacement, or automatic re-reservation.
- Reservation history or an account-wide reservations page.
- Seller or admin cancellation of buyer reservations.
- Refunds, disputes, chargebacks, ticket transfer, or settlement behavior.
- Notifications, analytics, or broad event-detail redesign.

## Acceptance Criteria

- [ ] An authenticated buyer can release their own eligible active reservation before checkout.
- [ ] A reservation with an order cannot be cancelled through this flow.
- [ ] Release and checkout creation cannot both win for the same reservation.
- [ ] Successful release atomically changes the reservation to `CANCELLED` and the matching listing to `ACTIVE`.
- [ ] Expired reservations become `EXPIRED` rather than `CANCELLED`.
- [ ] Repeated cancellation is idempotent and does not duplicate audit records.
- [ ] The frontend waits for authoritative server success before changing availability.
- [ ] Responses, logs, and UI exclude private identities, payment details, credentials, and sensitive ticket payload data.
- [ ] Relevant API, database, security, flow, concern, and continuity documentation is updated.

## Focused Issues

- `#117` — Define buyer reservation release contract.
- `#118` — Implement buyer reservation release backend.
- `#119` — Build buyer reservation release control.

## Delivery Order

1. Complete and approve `#117`.
2. Implement `#118` after the contract is merged.
3. Implement `#119` after the backend is available.

The contract can proceed independently of the post-payment fulfilment, event-search, container-stack, and seller-cancellation workstreams.

## Concerns

- Cancellation and checkout must use the same listing-first lock order to prevent deadlocks and inconsistent inventory.
- The presence of any order is the boundary after which checkout reconciliation owns state, even if payment has not completed.
- The initial reservation lookup used to discover the listing must be fully revalidated after locks are acquired.
- Listing reactivation must never overwrite `SOLD`, `CANCELLED`, `EXPIRED`, or another later state.
- The current frontend only knows reservations created in the active page session; reservation history remains separate work.
- Audit records must contain only actor ID, reservation ID, action, entity type, and timestamp.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, or other verification commands in this story or its focused issues. Complete application implementation first; verification will be handled later as a separate final phase.
