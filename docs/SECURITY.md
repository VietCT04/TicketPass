# Security

## Seller Listing Security Rules

Seller-created listings are public marketplace metadata. They must not expose sensitive ticket data before the controlled reveal flow allows it.

The full seller listing flow is documented in `docs/flows/SELLER_LISTING_FLOW.md`.

## Authentication And Ownership

- Listing creation requires authentication.
- `seller_id` must be derived from the authenticated user on the server.
- Clients must not be allowed to create listings for another seller by submitting `seller_id`.
- Frontend checks are only usability aids; listing ownership and validation must be enforced server-side.

## Sensitive Ticket Data

The listing contract must not accept, store, or return:

- Raw QR codes.
- Barcodes.
- Ticket images.
- Ticket PDFs.
- Private transfer links.
- Platform credentials.
- Any value that would let a buyer use the ticket before escrow and reveal rules allow it.

Secure ticket upload, storage, reveal, and audit logging are separate flows.

## Public Listing Data

The following fields are safe to expose as public listing metadata when validated:

- Event name, venue, city, start date, and event platform.
- Seat information.
- Ticket type.
- Asking price and currency.
- Transfer method.
- Seller-provided public notes that do not contain sensitive ticket payload data.

## Transferability Confirmation

Sellers must confirm the ticket is transferable before listing creation. This confirmation reduces accidental invalid listings but does not prove transferability. Platform-specific verification remains an unresolved concern.
