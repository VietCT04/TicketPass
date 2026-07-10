# Security

## Authentication Security Rules

TicketPass uses email/password authentication with server-side opaque sessions for MVP.

## Password Handling

- Passwords must be hashed with a strong password hashing algorithm before storage.
- Plaintext passwords must never be stored or logged.
- Password hashes must never be returned by the API.
- Login failures must not reveal whether the email or password was incorrect.
- Email addresses must be normalized before uniqueness checks.

## Session Handling

- The server creates a random opaque session token after signup or login.
- Only a hash of the session token is stored in `auth_sessions`.
- The browser receives the raw token in an `HttpOnly` cookie.
- The session cookie must use `SameSite=Lax`.
- The session cookie must use `Secure` in production.
- Logout must revoke the server-side session and clear the cookie.
- Missing, invalid, expired, or revoked sessions must return `401` for protected APIs.

## Current User And Ownership

- Backend services must derive the current user from authenticated session state.
- Clients must not be allowed to choose or override user IDs for ownership.
- Seller, buyer, order, payment, ticket, reveal, and dispute ownership checks must use the authenticated user.
- Frontend route protection is a usability layer only; backend authorization remains required.

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
