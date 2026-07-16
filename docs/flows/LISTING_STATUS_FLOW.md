# Listing Status Flow

## Source

User Story: `docs/user-stories/US-0001-list-transferable-ticket.md`  
GitHub Issue: `#4` - https://github.com/VietCT04/TicketPass/issues/4

Reservation contract: `docs/user-stories/US-0006-reserve-available-ticket-listing.md`
GitHub Issue: `#53` - https://github.com/VietCT04/TicketPass/issues/53

## Goal

Define listing status rules that prevent the same ticket listing from being sold twice.

This document defines the contract for status meanings, allowed transitions, and duplicate-sale prevention invariants. Implementation belongs to later backend/database work.

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
| `RESERVED` | `ACTIVE` | Reservation expires or a future payment attempt fails before completion. | On reservation expiry, mark the reservation `EXPIRED`; listing becomes purchasable again only when all other eligibility rules still hold. |
| `RESERVED` | `SOLD` | Payment/escrow and sale completion rules allow sale finalization. | Terminal. |
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
- A transition from `ACTIVE` to `RESERVED` must be atomic and server-side.
- Concurrent buyers must not be able to reserve the same listing.
- Reservation ownership must be stored in a separate reservation record linked to the listing and authenticated buyer.
- Only one valid `ACTIVE` reservation may own a listing at a time.
- A same-buyer retry while their reservation remains active must return that reservation without creating another record or extending its expiry.
- A reservation that reaches its server-generated expiry must become `EXPIRED` and stop owning the listing.
- A seller must not reserve their own listing.
- A `SOLD` listing must never be sold again.
- Frontend state must never be trusted to decide whether a listing is available.

## Enforcement Expectations

Backend implementation should enforce status transitions in one service boundary instead of scattering status writes across controllers.

Database implementation should support duplicate-sale prevention with transactional checks or constraints. The exact database mechanism belongs to backend implementation work, but it must protect against concurrent purchase attempts.

Recommended implementation direction:

- Read the listing row in a transaction.
- Revalidate that the listing exists, is `ACTIVE`, uses `VND`, belongs to an upcoming event, and is not owned by the authenticated buyer.
- Confirm no competing valid reservation owns the listing.
- Atomically create the reservation and update status to `RESERVED`.
- Treat zero updated rows or stale state as a failed reservation.
- Return the existing active reservation for a same-buyer retry without extending its expiry.
- Reconcile expired reservations so they become `EXPIRED` and release their listings back to `ACTIVE` when otherwise eligible.
- Do not allow client-provided status changes for sensitive transitions.

## Relationship To Other Flows

- Listing creation starts as `ACTIVE` in the MVP contract from `docs/API.md`.
- Issue `#53` defines `ACTIVE -> RESERVED` as an authenticated buyer's 10-minute server-controlled reservation. It does not define checkout or payment.
- Buyer checkout and payment may later drive `RESERVED -> SOLD` or `RESERVED -> ACTIVE` under separate contracts.
- Payment and escrow rules decide when a reserved listing can become `SOLD`.
- Ticket reveal must not occur just because a listing is `RESERVED`.
- Disputes and admin actions may need additional transitions in later issues.

## Out Of Scope

- Implementing database constraints or service logic.
- Buyer checkout.
- Payment and escrow state transitions.
- Ticket reveal.
- Dispute handling.
- Admin recovery from terminal statuses.

## Related Docs

- `docs/API.md`
- `docs/DATABASE.md`
- `docs/SECURITY.md`
- `docs/user-stories/US-0001-list-transferable-ticket.md`
