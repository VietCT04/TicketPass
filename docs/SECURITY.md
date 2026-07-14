# Security

## Authentication Security Rules

TicketPass uses email/password authentication with server-side opaque sessions for MVP.

## Password Handling

- Passwords must be hashed with BCrypt before storage.
- MVP passwords must be 12 to 128 characters.
- Email and display name inputs may be trimmed/normalized; passwords must not be trimmed.
- Plaintext passwords must never be stored or logged.
- Password hashes must never be returned by the API.
- Login failures must not reveal whether the email or password was incorrect.
- Email addresses must be normalized before uniqueness checks.

## Session Handling

- The server creates a random opaque session token after signup or login.
- The session cookie name is `ticketpass_session`.
- Only a hash of the session token is stored in `auth_sessions`.
- The browser receives the raw token in an `HttpOnly` cookie.
- The session cookie must use `SameSite=Lax`.
- The session cookie must use `Secure` in production.
- The session cookie must use path `/`.
- Cookie creation and clearing must share one centralized configuration, including name, path, domain if configured, `HttpOnly`, `Secure`, and `SameSite`.
- Logout must revoke the server-side session by setting `auth_sessions.revoked_at` and must clear the cookie with `Max-Age=0`.
- Missing, malformed, unknown, expired, or revoked sessions must return `401` for protected APIs.
- Logout must be idempotent and return `204 No Content` regardless of session validity.
- API authentication failures must return API-style `401` responses and must not redirect to a login page.
- Frontend code must store only derived current-user UI state in memory.
- Frontend code must not store the opaque session token or duplicate session credentials in JavaScript-accessible storage such as `localStorage` or `sessionStorage`.
- Frontend requests that rely on the session cookie must use credentialed requests.
- Frontend `GET /api/me` handling must treat `401` as the expected signed-out state, not as a user-facing failure.
- Frontend auth forms must prevent duplicate signup, login, or logout submissions while a request is in flight.

## Current User And Ownership

- Backend services must derive the current user from authenticated session state.
- Session authentication must place an immutable authentication-specific principal in Spring Security's `SecurityContext`.
- The principal must contain only the minimum required user information and must not be a JPA entity.
- Raw session tokens must never be stored in `SecurityContext` or written to logs.
- Clients must not be allowed to choose or override user IDs for ownership.
- Seller, buyer, order, payment, ticket, reveal, and dispute ownership checks must use the authenticated user.
- Frontend route protection is a usability layer only; backend authorization remains required.

## Seller-Owned API Identity Pattern

- Seller-owned routes must be protected by Spring Security before controller execution.
- Seller-owned controllers should receive the immutable `AuthenticatedUser` with `@AuthenticationPrincipal`.
- Business services should normally receive the trusted authenticated user id explicitly, such as `listingService.createListing(authenticatedUser.id(), request)`.
- Seller-owned request DTOs must not declare `sellerId`, `userId`, `ownerId`, or equivalent ownership fields.
- Seller ownership must be derived exclusively from `AuthenticatedUser.id()`.
- Raw session tokens must not be passed to controllers, business services, request DTOs, logs, or ownership logic.
- Do not add duplicate cookie parsing or session resolution for seller-owned APIs.
- A reusable current-user accessor should be added only when direct current-user access is required outside controller boundaries, and it must fail defensively when no valid authenticated principal exists.

## Seller Listing Security Rules

Seller-created listings are public marketplace metadata. They must not expose sensitive ticket data before the controlled reveal flow allows it.

The full seller listing flow is documented in `docs/flows/SELLER_LISTING_FLOW.md`.

Issue `#3` implements seller listing creation through a Spring Security-protected `POST /api/listings` endpoint that receives `AuthenticatedUser` with `@AuthenticationPrincipal`.

## Authentication And Ownership

- Listing creation requires authentication.
- `seller_id` must be derived from the authenticated user on the server.
- Clients must not be allowed to create listings for another seller by submitting `seller_id`.
- Frontend checks are only usability aids; listing ownership and validation must be enforced server-side.
- Listing availability must be enforced server-side using the status rules in `docs/flows/LISTING_STATUS_FLOW.md`.
- Clients must not be trusted to decide whether a listing can be reserved, purchased, or sold.

## Sensitive Ticket Data

The public listing contract must not define dedicated fields that accept, store, or return:

- Raw QR codes.
- Barcodes.
- Ticket images.
- Ticket PDFs.
- Private transfer links.
- Platform credentials.
- Any value that would let a buyer use the ticket before escrow and reveal rules allow it.

Secure ticket upload, storage, reveal, and audit logging are separate flows.

## Public Listing Data

The following fields are intended as public listing metadata:

- Selected event summary: event name, venue, city, and start date.
- Listing-level event platform or ticket provider.
- Seat information.
- Ticket type.
- Asking price and `VND` currency.
- Transfer method.
- Seller-provided public notes.

MVP does not classify free-text public notes for sensitive content. This limitation is tracked in `docs/CONCERNS.md`.

## Public Event Browse Security

The public `GET /api/events` browse contract exposes event summaries and server-derived listing aggregates only.

Event visibility and aggregate values must be calculated server-side from the same browse-eligible listing rule documented in `docs/API.md`.

For MVP, browse-eligible listings are limited to active, future, VND listings that are currently available for purchase under the listing status rules.

Public event browse responses must not include:

- Ticket payload data.
- Seat details or seat-specific inventory details.
- Seller IDs.
- Seller email addresses.
- Private seller notes.
- Private transfer links.
- QR codes.
- Barcodes.
- Ownership information.
- Buyer-specific or seller-specific state.

`image_url` is nullable in the API contract. Until trusted event image source and moderation rules exist, clients should render safe placeholders when `image_url` is `null`.

## Seller Event Autocomplete Security

The authenticated `GET /api/events/autocomplete` contract exposes seller-safe existing event summaries only.

Authentication is required because autocomplete is part of the seller listing flow. Frontend checks are only usability aids; the backend must enforce authentication, query validation, result limits, and event eligibility.

Autocomplete may include future events that have no active listings so a seller can create the first listing for an existing event. The server must still enforce that returned events are existing TicketPass records, selectable by server-issued `event_id`, and eligible under the documented MVP rules.

Seller event autocomplete responses must not include:

- Ticket payload data.
- Listing IDs or listing details.
- Seat information.
- Prices.
- Seller IDs.
- Seller contact information.
- Ownership data.
- Private seller or event notes.
- Private transfer links.
- QR codes.
- Barcodes.

Autocomplete must use strict query limits to reduce unnecessary backend load and enumeration risk: trimmed `q` length from `3` to `100` characters, maximum `10` results, and no pagination for MVP.

## Event-Linked Listing Creation Security

Issue `#32` defines listing creation as an event-linked operation. Sellers submit `event_id` for an existing TicketPass event and ticket-specific listing data; they do not submit event identity fields that would create or redefine an event record.

The backend must independently validate the selected event. Frontend autocomplete selection is not trusted as authorization or eligibility proof.

Server-side listing creation must enforce:

- The request is authenticated.
- Seller ownership is derived from `AuthenticatedUser.id()`.
- The selected `event_id` exists.
- The selected event has `starts_at` in the future at request time.
- Listing creation does not create, rename, or otherwise modify event records.
- `event_platform` is listing/ticket-specific and does not redefine event identity.
- New MVP listings are stored as `VND`; clients cannot choose currency.
- `asking_price_minor` is a positive whole-dong integer for VND.

When event cancellation, hidden, public/private, or moderation fields exist, listing creation must also enforce those eligibility rules server-side.

## Transferability Confirmation

Sellers must confirm the ticket is transferable before listing creation. This confirmation reduces accidental invalid listings but does not prove transferability. Platform-specific verification remains an unresolved concern.
