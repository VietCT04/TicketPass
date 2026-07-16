# Listing Status Flow

## Source

User Story: `docs/user-stories/US-0001-list-transferable-ticket.md`  
GitHub Issue: `#4` - https://github.com/VietCT04/TicketPass/issues/4

Reservation contract: `docs/user-stories/US-0006-reserve-available-ticket-listing.md`

Checkout contract: `docs/user-stories/US-0007-complete-checkout-for-reserved-ticket.md`
GitHub Issue: `#53` - https://github.com/VietCT04/TicketPass/issues/53

## Goal

Define listing status rules that prevent the same ticket listing from being sold twice.

This document defines the contract for status meanings, allowed transitions, and duplicate-sale prevention invariants. Issue `#54` implements atomic reservation creation, and issue `#55` implements reservation expiry reconciliation and guarded listing reactivation.

## Statuses

| Status | Meaning | Publicly Purchasable |
|---|---|---:|
| `DRAFT` | Listing exists but is not visible or purchasable. | No |
| `ACTIVE` | Listing is visible and available for a buyer to start purchase. | Yes |
| `RESERVED` | Listing is temporarily held by one server-controlled reservation before a future purchase attempt. | No |
| `SOLD` | Listing has completed sale flow and must never be sold again. | No |
| `CANCELLED` | Seller or admin cancelled the listing before sale completion. | No |
| `EXPIRED` | Listing is unavailable because the event or listing window expired. | No |

Only `ACTIVE` listings can start a buyer purchase or reservation attempt.

## Allowed Transitions

| From | To | Trigger | Notes |
|---|---|---|---|
| none | `DRAFT` | Seller saves incomplete listing. | Optional future behavior. |
| none | `ACTIVE` | Seller creates a complete, valid listing. | MVP default for listing creation. |
| `DRAFT` | `ACTIVE` | Seller publishes a complete, valid listing. | Requires all listing validation rules. |
| `DRAFT` | `CANCELLED` | Seller discards draft. | Terminal. |
| `ACTIVE` | `RESERVED` | Authenticated buyer creates a reservation. | Creates a separate `ACTIVE` reservation with a server-controlled 10-minute expiry; must be atomic server-side. |
| `ACTIVE` | `CANCELLED` | Seller cancels before reservation or sale. | Terminal. |
| `ACTIVE` | `EXPIRED` | Event or listing window expires. | Terminal. |
| `RESERVED` | `ACTIVE` | Reservation expiry or a trusted provider terminal failure/cancellation releases the still-reserved checkout path. | Release only when the listing is still `RESERVED` by that path; never overwrite `SOLD`, `CANCELLED`, `EXPIRED`, or another later state. Browser cancellation redirects are non-authoritative. |
| `RESERVED` | `SOLD` | Verified provider webhook or equivalent trusted server-to-server confirmation completes payment. | Atomically paired with `PAYMENT_PENDING -> PAID` after server-side revalidation. Terminal. |
| `RESERVED` | `CANCELLED` | Admin cancels reserved listing due to risk or support action. | Terminal. |

## Terminal Statuses

The following statuses are terminal for normal marketplace flow:

- `SOLD`
- `CANCELLED`
- `EXPIRED`

Terminal listings must not return to `ACTIVE` without an explicit admin-only recovery process documented in a separate issue.

## Duplicate-Sale Prevention Invariants

These rules must always hold:

- A listing represents exactly one ticket for MVP.
- `quantity` is always `1`.
- Only `ACTIVE` listings can be reserved or purchased.
- `RESERVED`, `SOLD`, `CANCELLED`, and `EXPIRED` listings must not be purchasable.
- A transition from `ACTIVE` to `RESERVED` is atomic and server-side under a pessimistic listing lock, with a partial unique active-reservation index as an integrity safeguard.
- Concurrent buyers must not be able to reserve the same listing.
- Reservation ownership must be stored in a separate reservation record linked to the listing and authenticated buyer.
- Only one valid `ACTIVE` reservation may own a listing at a time.
- A same-buyer retry while their reservation remains active must return that reservation without creating another record or extending its expiry.
- A reservation at `expires_at <= now` must become `EXPIRED` and stop owning the listing. Scheduled and request-time reconciliation use server time and the same pessimistic listing lock.
- Expiration restores `RESERVED -> ACTIVE` only when the listing is still `RESERVED`; it must not overwrite `SOLD`, `CANCELLED`, `EXPIRED`, or another later status.
- Checkout creates no replacement inventory deadline: `order.expires_at` must exactly equal the reservation expiry.
- One reservation may have exactly one order; concurrent or repeated checkout starts must resolve to that same order.
- Only trusted server-to-server payment confirmation may atomically move `RESERVED -> SOLD` with `PAYMENT_PENDING -> PAID`. Browser redirects, browser state, and hosted-session creation are not payment authority.
- A verified successful payment received after order or reservation expiry must not sell the listing, overwrite a terminal order, or reactivate any inventory. It must be retained for operational handling.
- A seller must not reserve their own listing.
- A `SOLD` listing must never be sold again.
- Frontend state must never be trusted to decide whether a listing is available.

## Enforcement Expectations

Backend implementation should enforce status transitions in one service boundary instead of scattering status writes across controllers.

Database implementation should support duplicate-sale prevention with transactional checks or constraints. The exact database mechanism belongs to backend implementation work, but it must protect against concurrent purchase attempts.

Recommended implementation direction:

- Read and pessimistically lock the listing row in a transaction.
- Revalidate that the listing exists, is `ACTIVE`, uses `VND`, belongs to an upcoming event, and is not owned by the authenticated buyer.
- Confirm no competing valid reservation owns the listing.
- Atomically create the reservation and update status to `RESERVED`; the database permits at most one `ACTIVE` reservation record per listing.
- Treat zero updated rows or stale state as a failed reservation.
- Return the existing active reservation for a same-buyer retry without extending its expiry.
- Issue `#55` reconciles expired active reservations during both scheduled cleanup and a new reservation attempt. It flushes the expired row before inserting a replacement active row, so the partial active-reservation uniqueness rule remains valid.
- Do not allow client-provided status changes for sensitive transitions.

## Relationship To Other Flows

- Listing creation starts as `ACTIVE` in the MVP contract from `docs/API.md`.
- Issue `#53` defines `ACTIVE -> RESERVED` as an authenticated buyer's 10-minute server-controlled reservation. Issues `#54` and `#55` implement creation, expiry, and guarded reactivation, but do not define checkout or payment.
- Issue `#65` defines provider-neutral checkout and payment authority. Issues `#66` through `#70` own persistence, provider sessions, verified completion, expiry/failure coordination, and payment operations.
- Checkout may drive `RESERVED -> SOLD` only through verified provider confirmation, or `RESERVED -> ACTIVE` only through an approved trusted expiry/failure release flow.
- Ticket reveal must not occur just because a listing is `RESERVED`.
- Disputes and admin actions may need additional transitions in later issues.

## Out Of Scope

- Implementing database constraints or service logic.
- Checkout implementation, provider selection, and payment-session creation.
- Payment webhook, failure, expiry, and reconciliation implementation.
- Ticket reveal.
- Dispute handling.
- Admin recovery from terminal statuses.

## Related Docs

- `docs/API.md`
- `docs/DATABASE.md`
- `docs/SECURITY.md`
- `docs/user-stories/US-0001-list-transferable-ticket.md`
