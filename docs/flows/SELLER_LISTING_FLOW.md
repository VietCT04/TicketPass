# Seller Listing Flow

## Source

User Story: `docs/user-stories/US-0001-list-transferable-ticket.md`  
GitHub Issue: `#7` - https://github.com/VietCT04/TicketPass/issues/7

## Goal

Allow an authenticated seller to create a public listing for one transferable ticket without exposing sensitive ticket data before payment, escrow, and ticket reveal rules allow it.

## Scope

This flow documents seller listing creation only.

In scope:

- Seller authentication requirement.
- Event and listing metadata collection.
- Seller transferability confirmation.
- Public listing metadata safety.
- Server-side validation expectations.
- Relationship to duplicate-sale prevention, audit logging, and later reveal flow.

Out of scope:

- Buyer checkout.
- Payment capture.
- Escrow funding or release.
- Ticket upload, storage, QR reveal, or barcode reveal.
- Dispute handling.
- Admin moderation.

## Actors

- Seller: authenticated user creating a listing.
- Backend API: validates listing data, derives seller ownership, and persists the listing.
- Buyer: future viewer of public listing metadata only.

## Preconditions

- Seller is authenticated.
- Seller has a ticket they believe is transferable.
- Seller can provide event metadata, ticket metadata, asking price, transfer method, and transferability confirmation.

## Listing Creation Steps

1. Seller opens the create listing flow.
2. Frontend collects event metadata:
   - Event name.
   - Venue.
   - City.
   - Start date and time.
   - Event platform.
3. Frontend collects listing metadata:
   - Seat information.
   - Ticket type.
   - Currency.
   - Asking price.
   - Transfer method.
   - Public notes.
4. Seller confirms the ticket is transferable.
5. Frontend submits `POST /api/listings`.
6. Spring Security validates the session before controller execution.
7. Backend receives the immutable `AuthenticatedUser` principal and derives `seller_id` from `AuthenticatedUser.id()`.
8. Backend validates all required fields server-side.
9. Backend creates or associates normalized event metadata.
10. Backend creates one listing with `quantity = 1`.
11. Backend returns public listing metadata only.

## Public Listing Metadata

The public listing may expose:

- Event name, venue, city, start date, and event platform.
- Seat information.
- Ticket type.
- Asking price and currency.
- Transfer method.
- Seller-provided public notes after validation.

The public listing must not expose:

- Raw QR codes.
- Barcodes.
- Ticket images.
- Ticket PDFs.
- Private transfer links.
- Platform credentials.
- Any private payload that would let a buyer use the ticket before controlled reveal.

## Server-Side Rules

- Seller identity must be derived from the authenticated `AuthenticatedUser` principal.
- Client-provided seller or ownership fields must be rejected.
- Listing services must not parse cookies or resolve raw session tokens.
- `quantity` is fixed to `1` for MVP.
- `is_transferable_confirmed` must be `true`.
- `asking_price_minor` must be greater than zero.
- `currency` must be a valid ISO-4217 currency code.
- Required event and listing fields must be non-empty after trimming.
- Sensitive ticket data must not be accepted in public metadata fields.

## Status And Duplicate Sale

Newly created listings start from the listing status contract in `docs/DATABASE.md`.

Duplicate-sale prevention is required for the product, but detailed status transition enforcement belongs to GitHub Issue `#4`.

The listing flow must not create a path where a `RESERVED`, `SOLD`, `CANCELLED`, or `EXPIRED` listing can be treated as available.

## Audit Expectations

Listing creation should produce an auditable record showing who created the listing, what listing was created, and when it happened.

Detailed audit implementation belongs to GitHub Issue `#5`.

Audit records must not include raw QR codes, barcodes, ticket PDFs, private transfer links, platform credentials, or other sensitive ticket payload data.

## Security Notes

- Frontend checks are usability aids only.
- Backend validation and authorization are mandatory.
- Seller transferability confirmation is not proof that the ticket is actually transferable.
- `event_platform` is captured because transferability rules vary by platform.
- Secure ticket upload, storage, and reveal must remain separate from public listing creation.

## Related Docs

- `docs/API.md`
- `docs/DATABASE.md`
- `docs/SECURITY.md`
- `docs/CONCERNS.md`
- `docs/user-stories/US-0001-list-transferable-ticket.md`
