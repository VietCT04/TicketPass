# US-0006: Reserve an Available Ticket Listing

## User Story

As an authenticated buyer, I want to temporarily reserve an available ticket listing so that another buyer cannot take it while I proceed toward checkout.

## Context

TicketPass currently lets buyers browse events and compare available ticket listings, but those public responses are marketplace snapshots rather than inventory guarantees. A buyer needs a short server-controlled hold before any future checkout or payment flow can safely begin.

This story introduces reservation only. It does not define payment, escrow, sale completion, ticket reveal, seller contact exchange, refunds, or disputes.

## Acceptance Criteria

- [ ] Only an authenticated buyer can reserve a listing.
- [ ] A seller cannot reserve their own listing.
- [ ] Only a listing that is currently `ACTIVE`, uses `VND`, and belongs to an upcoming event can be reserved.
- [ ] A successful reservation atomically changes the listing from `ACTIVE` to `RESERVED`.
- [ ] Concurrent buyers cannot both reserve the same listing; exactly one reservation attempt may succeed.
- [ ] The reservation hold lasts 10 minutes using server-generated time.
- [ ] The client cannot choose or extend the reservation duration.
- [ ] Reservation ownership is stored separately from the listing through a reservation record linked to the listing and authenticated buyer.
- [ ] Repeating the request as the same buyer while the reservation is still active returns the existing reservation without creating a duplicate or extending its expiry.
- [ ] A different buyer attempting to reserve a held or otherwise unavailable listing receives a general conflict response that does not reveal reservation ownership.
- [ ] An expired reservation stops owning the listing and the listing becomes `ACTIVE` and reservable again.
- [ ] The reservation response excludes seller identity, buyer email, ticket payload data, private transfer links, credentials, session data, and public notes.
- [ ] Availability and status transitions are enforced server-side and never trusted from frontend state.
- [ ] Relevant API, database, security, status-flow, concern, and continuity documentation is updated.

## Out of Scope

- Checkout UI or checkout orchestration.
- Payment capture or escrow funding.
- Transitioning a listing from `RESERVED` to `SOLD`.
- Ticket upload, storage, transfer, or reveal.
- Seller contact exchange.
- Buyer-initiated manual reservation release.
- Reservation extension or renewal.
- Refunds, disputes, or admin reservation management.
- Expanding audit events beyond the existing `LISTING_CREATED` event.

## Risks

- Reservation creation and listing status transition must remain atomic under concurrent requests.
- Expired reservations require a reliable server-side release mechanism so listings do not remain unavailable indefinitely.
- A public event-detail response may become stale before the reservation request and must never be treated as proof of availability.
- State-changing cookie-authenticated reservation requests require the existing CSRF strategy to be reviewed before public launch.
- Automatic expiry without manual release may temporarily hold abandoned inventory for the full 10-minute window.

## Follow-up Issues

- GitHub Issue `#53`: Define buyer listing reservation API and data contract - https://github.com/VietCT04/TicketPass/issues/53
- Implement atomic buyer listing reservation and expiration.
- Add buyer reservation action after the public event-detail page is available.
- Define checkout, payment, escrow, sale completion, and ticket reveal in separate user stories.
