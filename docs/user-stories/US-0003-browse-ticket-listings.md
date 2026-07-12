# US-0003: Browse Ticket Listings

## User Story

As a buyer, I want to browse available ticket listings so that I can find tickets for events I want to attend.

## Context

Buyers need a safe way to discover available tickets before checkout. TicketPass should show enough public listing and event information for buyers to evaluate whether a ticket matches their needs while excluding listings that are no longer available or should not be visible.

## Acceptance Criteria

- [ ] Buyer can view active ticket listings.
- [ ] Listings show event name, event date, venue, ticket type, price, and seller-safe public information.
- [ ] Listings do not expose sensitive ticket data, including QR codes, barcodes, transfer links, or private seller details.
- [ ] Sold, cancelled, expired, or hidden listings are excluded from browse results.
- [ ] Browse results support basic pagination.
- [ ] Listing availability and visibility are enforced server-side.
- [ ] Relevant docs are updated.

## Risks

- Listing expiration rules need to be defined clearly enough to avoid showing stale tickets.
- Seller-safe public information must stay limited so buyers can evaluate listings without exposing sensitive ticket or seller data.
- Browse ordering, filtering, and search are intentionally deferred and may need separate issues.

## Follow-up Issues

- To be created after this user story is approved.
