# US-0001: List Transferable Ticket

## User Story

As a seller, I want to list a transferable ticket so that I can safely sell it to another user.

## Context

Sellers need a controlled way to create ticket listings without exposing sensitive ticket data too early. TicketPass should collect enough event, ticket, pricing, and transferability information to help buyers evaluate the listing while protecting the seller until escrow and reveal rules allow ticket access.

## Acceptance Criteria

- [ ] Seller can create a listing only when authenticated.
- [ ] Seller can provide required event details, ticket details, asking price, and transfer method.
- [ ] Seller must confirm the ticket is transferable before submitting the listing.
- [ ] Sensitive ticket data, including QR codes or barcodes, is not publicly visible in the listing.
- [ ] Listing creation validates required fields server-side.
- [ ] Newly listed tickets are not available for duplicate sale once reserved or purchased.
- [ ] Listing creation produces an auditable record.
- [ ] Relevant docs are updated.

## Out of Scope

- Payment capture and escrow funding.
- Buyer checkout flow.
- Ticket reveal after purchase.
- Dispute handling.
- Admin moderation tooling.

## Risks

- Transferability rules vary by event platform, venue, and ticket type.
- Sellers may incorrectly claim a ticket is transferable.
- Validation requirements may need to change once supported ticket providers or upload formats are defined.

## Follow-up Issues

- GitHub Issue: TBD
