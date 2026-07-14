# US-0005: View Available Listings for an Event

## User Story

As a buyer, I want to open an event and view its currently available ticket listings so that I can compare ticket options before choosing one to purchase.

## Context

TicketPass uses an event-first marketplace flow. The public browse experience lets buyers discover events with available inventory, but buyers still need a focused event page where they can review the event and compare its currently available ticket offers.

This story defines the public read-only step between browsing events and a future reservation, checkout, payment, escrow, and ticket-reveal flow.

## Acceptance Criteria

- [ ] Buyer can open an event through the public route `/events/{eventId}`.
- [ ] The event page shows only approved public event fields, including name, date and time, venue, city, and a safe image or placeholder where available.
- [ ] The event page shows only listings that satisfy the shared server-side browse-eligibility rules.
- [ ] Sold, reserved, cancelled, expired, non-VND, or otherwise unavailable listings are excluded server-side.
- [ ] Each listing summary may show listing id, ticket type, seat information, listing-level event platform/provider, asking price in VND, and transfer method.
- [ ] Seller id, seller identity, seller contact details, ownership data, and private seller data are not exposed.
- [ ] `public_notes` is excluded from the MVP listing summary because free-text sensitive-content classification is not implemented.
- [ ] Listing summaries are ordered by `asking_price_minor ASC`, then `created_at ASC`, then listing `id ASC`.
- [ ] Listings support 1-based pagination with a default page size of `20` and a maximum page size of `50`.
- [ ] An existing upcoming event with no currently available listings returns the event with an empty listing collection and the frontend shows a clear empty state.
- [ ] A missing event or an event that is no longer upcoming is treated as not found for the MVP public detail flow.
- [ ] Listing cards are read-only and non-clickable until a separate listing-selection or checkout flow is defined.
- [ ] Loading, empty, success, unavailable-event, and API-error states are handled.
- [ ] Sensitive ticket payload data, including QR codes, barcodes, ticket files, private transfer links, and platform credentials, is not returned or rendered.
- [ ] Availability and visibility rules are enforced server-side.
- [ ] Relevant documentation is updated.

## Out of Scope

- Reserving or purchasing a listing.
- Checkout, payment, or escrow.
- Ticket upload, storage, or reveal.
- Seller profile, seller reputation, or seller contact information.
- Public seller notes.
- Listing detail pages.
- Advanced listing filtering, recommendations, or sorting controls.
- Seller listing management.
- Event reviews or comments.

## Risks

- Listing availability may change after a buyer loads the event page and before a future reservation attempt.
- Event-local timezone information is not currently preserved separately from the absolute event timestamp.
- Event cancellation, rescheduling, hidden, and public/private states are not fully represented in the current event schema.
- Duplicate event records may split equivalent ticket inventory across separate event pages.
- Public event images still require a trusted source and moderation policy before non-placeholder images are broadly supported.

## Follow-up Issues

- `#44` Define event detail and available listings API contract - https://github.com/VietCT04/TicketPass/issues/44
- `#45` Implement public event detail and available listings API - https://github.com/VietCT04/TicketPass/issues/45
- `#46` Build public event detail and available listings page - https://github.com/VietCT04/TicketPass/issues/46
