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
- Public listing metadata boundaries.
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
- Seller can search for and select an existing TicketPass event.
- Seller can provide ticket metadata, asking price, transfer method, event platform, and transferability confirmation.

## Listing Creation Steps

1. Seller opens the create listing flow.
2. Frontend requires the seller to search for and select an existing event through the authenticated event autocomplete flow.
3. The selected event supplies `event_id` for listing creation.
4. Frontend collects listing metadata:
   - Event platform or ticket provider.
   - Seat information.
   - Ticket type.
   - Asking price.
   - Transfer method.
   - Public notes.
5. Seller confirms the ticket is transferable.
6. Frontend submits `POST /api/listings` with `event_id` and listing-specific fields.
7. Spring Security validates the session before controller execution.
8. Backend receives the immutable `AuthenticatedUser` principal and derives `seller_id` from `AuthenticatedUser.id()`.
9. Backend validates all required fields server-side, including selected event existence and eligibility.
10. Backend associates the listing to the selected event without creating, renaming, or modifying the event record.
11. Backend creates one listing with `quantity = 1`, listing-level `event_platform`, and `currency = VND`.
12. Backend returns public listing metadata only.

## Public Listing Metadata

The public listing may expose:

- Selected event summary: event name, venue, city, and start date.
- Listing-level event platform or ticket provider.
- Seat information.
- Ticket type.
- Asking price and `VND` currency.
- Transfer method.
- Seller-provided public notes.

The public listing contract must not define dedicated fields for:

- Raw QR codes.
- Barcodes.
- Ticket images.
- Ticket PDFs.
- Private transfer links.
- Platform credentials.
- Any private payload that would let a buyer use the ticket before controlled reveal.

MVP does not classify free-text public notes for sensitive content. This limitation is tracked in `docs/CONCERNS.md`.

## Server-Side Rules

- Seller identity must be derived from the authenticated `AuthenticatedUser` principal.
- Client-provided seller or ownership fields must be rejected.
- Listing services must not parse cookies or resolve raw session tokens.
- `event_id` must reference an existing TicketPass event.
- Event identity fields such as event name, venue, city, or start time must not be accepted from the listing creation request.
- Backend validation must not trust frontend autocomplete selection as proof that the event exists or remains eligible.
- The selected event must have `starts_at` in the future at request time.
- Listing creation must not create, rename, or otherwise modify event records.
- Event-level cancellation, hidden, public/private, or moderation checks must be added once supported by schema and product rules.
- `event_platform` is listing/ticket-specific and does not redefine event identity.
- `quantity` is fixed to `1` for MVP.
- `is_transferable_confirmed` must be `true`.
- `asking_price_minor` must be greater than zero.
- New MVP listings must use `currency = VND`; the client must not choose currency.
- For VND, `asking_price_minor` represents whole dong.
- Required listing fields must be non-empty after trimming.
- The public listing contract must not include dedicated sensitive ticket payload fields.
- MVP does not classify free-text public notes for sensitive content.

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
- `event_platform` is captured at the listing/ticket level because transferability rules vary by platform.
- Sellers cannot list tickets for events missing from TicketPass in the MVP flow.
- Secure ticket upload, storage, and reveal must remain separate from public listing creation.

## Related Docs

- `docs/API.md`
- `docs/DATABASE.md`
- `docs/SECURITY.md`
- `docs/CONCERNS.md`
- `docs/user-stories/US-0001-list-transferable-ticket.md`
