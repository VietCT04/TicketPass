# API

## Authentication

Authentication lets users create accounts, log in, log out, and access protected TicketPass features. Backend services must derive the current user from authenticated session state and must not trust client-provided user IDs.

TicketPass uses email/password authentication with server-side opaque sessions for MVP.

### Sign Up

```http
POST /api/auth/signup
```

Creates a user account and starts an authenticated session.

#### Request Body

```json
{
  "email": "user@example.com",
  "password": "correct horse battery staple",
  "display_name": "Avery"
}
```

#### Request Fields

| Field | Type | Required | Notes |
|---|---|---:|---|
| `email` | string | Yes | Normalized before uniqueness checks. |
| `password` | string | Yes | Must be 12 to 128 characters. Passwords are not trimmed. |
| `display_name` | string | Yes | User-facing account name. |

#### Response Body

```json
{
  "user": {
    "id": "usr_123",
    "email": "user@example.com",
    "display_name": "Avery",
    "created_at": "2026-07-10T10:00:00Z"
  }
}
```

On success, the server also sets an `HttpOnly` session cookie.

Duplicate normalized emails return `409`.

### Log In

```http
POST /api/auth/login
```

Authenticates an existing user and starts a new session.

#### Request Body

```json
{
  "email": "user@example.com",
  "password": "correct horse battery staple"
}
```

#### Response Body

```json
{
  "user": {
    "id": "usr_123",
    "email": "user@example.com",
    "display_name": "Avery",
    "created_at": "2026-07-10T10:00:00Z"
  }
}
```

On success, the server also sets an `HttpOnly` session cookie.

Invalid credentials return `401` without revealing whether the email or password was incorrect.

### Log Out

```http
POST /api/auth/logout
```

Revokes the current session server-side and clears the session cookie.

Returns `204 No Content` regardless of whether the provided session was valid. If a matching active session exists, the server sets `auth_sessions.revoked_at`; the row is not deleted.

The response always sends a cookie-clearing header for `ticketpass_session`.

### Current User

```http
GET /api/me
```

Returns the authenticated user for the current session.

#### Response Body

```json
{
  "user": {
    "id": "usr_123",
    "email": "user@example.com",
    "display_name": "Avery",
    "created_at": "2026-07-10T10:00:00Z"
  }
}
```

Returns `401` when the session is missing, malformed, unknown, expired, or revoked.

### Update Profile

Issue `#141` defines this contract and issue `#142` implements the backend. The protected browser form remains issue `#143`.

```http
PUT /api/me/profile
```

Replaces the authenticated user's currently supported mutable profile resource. Authentication is required. The server derives the user ID only from the immutable `AuthenticatedUser` principal and reloads the current user row under a pessimistic write lock before making a decision. Clients cannot submit or change email, password, roles, permissions, user ID, account status, session state, timestamps, or any other server-controlled value.

The request accepts exactly `display_name`; unknown fields are rejected.

#### Request Body

```json
{
  "display_name": "Avery Nguyen"
}
```

#### Request Fields

| Field | Type | Required | Notes |
|---|---|---:|---|
| `display_name` | string | Yes | Required, maximum 120 raw-input characters, trimmed before comparison and persistence, and nonblank after trimming. Internal whitespace is preserved. |

Display names are not lowercased, case-folded, Unicode-normalized, made unique, or checked against reserved names in MVP. They remain untrusted text and clients must render them as text rather than HTML.

The service captures one injected-clock timestamp while processing the request. When the normalized submitted name equals the stored name, it returns `200 OK` without changing `users.updated_at`, flushing an update, writing an audit event, or touching cookies or sessions. An effective update changes only `users.display_name` and `users.updated_at`; all existing sessions remain valid.

#### Response Body

Both an effective update and a normalized no-op return `200 OK` using the same safe authenticated-user representation as signup, login, and `GET /api/me`:

```json
{
  "user": {
    "id": "usr_123",
    "email": "user@example.com",
    "display_name": "Avery Nguyen",
    "created_at": "2026-07-10T10:00:00Z"
  }
}
```

Successful responses send `Cache-Control: no-store`. They never include password hashes, roles, permissions, session IDs, token hashes, cookie values, audit data, internal timestamps, or marketplace-private information.

#### Errors

| Status | Meaning |
|---:|---|
| `400` | Malformed JSON, unknown field, missing or non-string `display_name`, blank value, or value longer than 120 raw-input characters. |
| `401` | Session is missing or invalid, or its authenticated user row no longer exists. |
| `403` | Existing unsafe cookie-authenticated request-origin protection rejected the request. |
| `5xx` | Controlled unexpected persistence failure without entity, SQL, stack-trace, credential, session, role, or account-metadata disclosure. |

This `PUT` is subject to the existing cookie-authenticated unsafe-request origin protection.

### Session Cookie

- Cookie name is `ticketpass_session`.
- Cookie contains a random opaque session token.
- Only a hash of the token is stored server-side.
- Cookie is `HttpOnly`.
- Cookie is `SameSite=Lax`.
- Cookie is `Secure` in production.
- Cookie uses path `/`.
- Cookie domain may be configured per environment.
- Cookie creation and clearing use the same centralized cookie configuration.
- Logout revokes the session server-side by setting `auth_sessions.revoked_at`.
- Logout clears the cookie using the same name, path, domain if configured, `HttpOnly`, `Secure`, and `SameSite` attributes, with `Max-Age=0`.

### Cookie-Authenticated Request Origin Protection

For MVP, the frontend and API must be deployed same-site, such as `app.ticketpass.com` and `api.ticketpass.com`. A fully cross-site deployment requiring `SameSite=None` is not supported.

Unsafe `/api/**` requests (`POST`, `PUT`, `PATCH`, and `DELETE`) that carry a `ticketpass_session` cookie must originate from an exact configured trusted frontend origin. The backend checks `Origin` first; only when it is absent does it check the origin component of `Referer`. Missing, malformed, or untrusted values return:

```http
403 Forbidden
Content-Type: application/json
```

```json
{
  "error": "Invalid request origin"
}
```

The default development trusted origin is `http://localhost:3000`, configured through `ticketpass.security.allowed-origins`. Production must explicitly configure its real frontend origin or origins. Trusted origins are normalized by scheme, host, and effective port, then compared exactly; wildcard and partial-host matching are not supported.

This applies to existing cookie-authenticated mutations, including signup or login when a session cookie is already present, logout, listing creation, missing-event request creation, and reservation creation. Requests without a session cookie, including ordinary signup and login, remain usable without origin headers. `GET`, `HEAD`, and `OPTIONS` remain unaffected.

Credentialed CORS uses the same trusted-origin configuration. It permits only those origins and never uses `*` with credentials. `SameSite=Lax` remains part of the cookie configuration, but is not the sole CSRF defense.

## Listings

Listings let authenticated sellers offer one transferable ticket for resale.

Flow details and security expectations are documented in `docs/flows/SELLER_LISTING_FLOW.md`.

### Create Listing

```http
POST /api/listings
```

Creates a seller-owned listing for one transferable ticket.

Authentication is required. Spring Security must validate the session before controller execution, and the controller must receive the immutable `AuthenticatedUser` principal with `@AuthenticationPrincipal`.

The server derives `seller_id` from `AuthenticatedUser.id()`. Clients must not send or override ownership fields such as `seller_id`, `sellerId`, `user_id`, `userId`, `owner_id`, or `ownerId`.

Seller-owned controllers should pass the trusted user id explicitly into business services, for example:

```java
listingService.createListing(authenticatedUser.id(), request);
```

Listing services must not parse cookies, resolve raw session tokens, or accept client-provided ownership values.

#### Request Body

```json
{
  "event_id": "22222222-2222-2222-2222-222222222222",
  "event_platform": "Ticketmaster",
  "seat_info": "Section 101, Row B, Seat 12",
  "ticket_type": "General Admission",
  "asking_price_minor": 1250000,
  "transfer_method": "PLATFORM_TRANSFER",
  "is_transferable_confirmed": true,
  "public_notes": "Mobile transfer available after purchase."
}
```

#### Request Fields

| Field | Type | Required | Notes |
|---|---|---:|---|
| `event_id` | string | Yes | Server-issued identifier for an existing TicketPass event selected through the seller event autocomplete flow. |
| `event_platform` | string | Yes | Platform or provider where this ticket originated. Used because transferability varies by platform and ticket source. Free-text string for MVP. |
| `seat_info` | string | Yes | Human-readable seat, section, row, or standing-zone information. |
| `ticket_type` | string | Yes | Ticket category such as standard, VIP, student, or general admission. |
| `asking_price_minor` | integer | Yes | Asking price. Must be greater than zero. For MVP, new listings are always stored as `VND`, and this integer represents whole dong. |
| `transfer_method` | string | Yes | Expected transfer method. Initial value set is `PLATFORM_TRANSFER`, `PDF_UPLOAD`, `QR_UPLOAD`, or `MANUAL_TRANSFER`. |
| `is_transferable_confirmed` | boolean | Yes | Must be `true`; seller confirms the ticket is transferable. |
| `public_notes` | string | No | Buyer-visible notes. MVP does not perform sensitive content classification on this free-text field. |

#### Response Body

```json
{
  "id": "33333333-3333-3333-3333-333333333333",
  "seller_id": "11111111-1111-1111-1111-111111111111",
  "event": {
    "id": "22222222-2222-2222-2222-222222222222",
    "name": "Example Concert",
    "venue": "Example Arena",
    "city": "Ho Chi Minh City",
    "starts_at": "2026-08-15T19:30:00+07:00"
  },
  "event_platform": "Ticketmaster",
  "seat_info": "Section 101, Row B, Seat 12",
  "ticket_type": "General Admission",
  "quantity": 1,
  "currency": "VND",
  "asking_price_minor": 1250000,
  "transfer_method": "PLATFORM_TRANSFER",
  "is_transferable_confirmed": true,
  "status": "ACTIVE",
  "public_notes": "Mobile transfer available after purchase.",
  "created_at": "2026-07-09T10:00:00Z",
  "updated_at": "2026-07-09T10:00:00Z"
}
```

#### Validation Rules

- Request must be authenticated.
- `seller_id` is always derived server-side from `AuthenticatedUser.id()`.
- Request DTOs must not declare seller, user, owner, or equivalent ownership fields.
- `event_id` must be provided as a UUID and must identify an existing TicketPass event.
- `event_id` must come from a seller autocomplete selection in the normal frontend flow, but the backend must independently validate event existence and eligibility.
- Listing creation must not create, rename, or otherwise modify the selected event record.
- The selected event must have `starts_at` in the future at request time.
- The selected event does not need an existing active listing because this seller may create the first listing for it.
- Future event cancellation, hidden, public/private, or moderation checks must be added when the schema supports those states.
- Clients must not send or redefine event identity fields such as `event_name`, `event_venue`, `event_city`, or `event_starts_at`.
- `event_platform` is listing/ticket-specific and must be returned at the listing level, not inside the nested `event` object.
- `quantity` is always `1` for MVP and is not accepted as a client-provided field.
- New complete listings start with status `ACTIVE`.
- `is_transferable_confirmed` must be `true`.
- `asking_price_minor` must be greater than zero.
- Clients must not send `currency` for MVP listing creation.
- New MVP listings are always stored as `VND`.
- For VND, `asking_price_minor` represents whole dong because VND has no decimal minor unit in this contract. For example, `1250000` means `VND 1,250,000`, not `VND 12,500.00`.
- Listing fields must be non-empty after trimming.
- The public listing contract must not include dedicated fields for QR codes, barcodes, ticket files, private transfer links, platform credentials, or other sensitive ticket payload data.
- MVP does not classify free-text `public_notes` for sensitive content; this limitation is tracked in `docs/CONCERNS.md`.

Listing availability and duplicate-sale status rules are documented in `docs/flows/LISTING_STATUS_FLOW.md`.

#### Error Behavior

- Missing or malformed `event_id`: `400 Bad Request`.
- Seller-provided event identity fields such as `event_name`, `event_venue`, `event_city`, or `event_starts_at`: `400 Bad Request`.
- Seller-provided `currency`: `400 Bad Request`.
- Event not found: `404 Not Found`.
- Event exists but is no longer eligible, such as having already started: `409 Conflict`.

The `409` case covers an event that was valid when selected through autocomplete but became ineligible before form submission.

#### Sensitive Data Rule

The listing API does not define dedicated request or response fields for raw QR codes, barcodes, ticket images, ticket PDFs, private transfer links, or platform credentials. Secure ticket upload, storage, and reveal belong to a separate ticket reveal flow.

### View Own Listings

```http
GET /api/me/listings
```

Returns the authenticated seller's stored listing metadata and current marketplace statuses. Issue `#82` defines this contract, issue `#83` implements the backend, and the protected `/my-listings` page belongs to issue `#84`.

Authentication is required. Seller ownership is derived exclusively from `AuthenticatedUser.id()`; the endpoint accepts no seller or user identifier in its path, query string, or request body. As a safe `GET`, it does not require the unsafe-request origin check, but credentialed browser requests continue to use the existing cookie and CORS model.

Successful responses send `Cache-Control: no-store`.

#### Query Parameters

| Parameter | Required | Default | Rules |
|---|---:|---|---|
| `page` | No | `1` | 1-based integer; minimum `1`. |
| `page_size` | No | `20` | Integer from `1` through `100`. |
| `status` | No | None | One exact uppercase `ListingStatus` value: `DRAFT`, `ACTIVE`, `RESERVED`, `SOLD`, `CANCELLED`, or `EXPIRED`. |

Invalid pagination values or an unsupported status return `400 Bad Request`.

#### Ownership, Filtering, And Ordering

The database query must apply `listing.seller_id = authenticated user ID` before counting or pagination. When `status` is present, it must apply the exact status predicate in the database rather than loading a broader listing set and filtering in application memory.

Results use deterministic newest-first ordering: `created_at DESC, id DESC`. The endpoint returns the stored server-authoritative listing status only; it does not reconcile reservation, checkout, payment, or expiry state.

#### Response Body

```json
{
  "items": [
    {
      "id": "listing-uuid",
      "status": "ACTIVE",
      "event_platform": "Ticketmaster",
      "seat_info": "Section A, Row 3",
      "ticket_type": "Standard",
      "quantity": 1,
      "asking_price_minor": 1250000,
      "currency": "VND",
      "transfer_method": "PLATFORM_TRANSFER",
      "is_transferable_confirmed": true,
      "public_notes": "Optional seller-entered notes",
      "created_at": "2026-07-17T10:00:00Z",
      "updated_at": "2026-07-17T10:00:00Z",
      "event": {
        "id": "event-uuid",
        "name": "Example Concert",
        "starts_at": "2026-10-17T11:30:00Z",
        "venue": "National Stadium",
        "city": "Singapore"
      }
    }
  ],
  "page": 1,
  "page_size": 20,
  "total_items": 1,
  "total_pages": 1
}
```

Implementation must use explicit DTOs or projections and must not serialize JPA entities or relationships directly. Seller-entered metadata, including `public_notes`, remains untrusted display content.

The response must not include seller identity or contact data; buyer identity or reservation ownership; reservation IDs or expiry details; order, payment-session, provider, payout, refund, or dispute data; audit rows; QR codes, barcodes, ticket PDFs, private transfer links, platform credentials, or other ticket payload data; cookies, session data, or internal database fields. A `SOLD` listing means only that the stored lifecycle reached `SOLD`; it does not imply payout, ticket transfer, reveal, refund finality, or dispute completion.

#### Empty Pages And Errors

No owned listings is a normal `200 OK` response:

```json
{
  "items": [],
  "page": 1,
  "page_size": 20,
  "total_items": 0,
  "total_pages": 0
}
```

A valid page beyond the final page also returns `200 OK` with an empty `items` array and accurate totals. The endpoint does not redirect or return `404` for an empty page.

- `400 Bad Request`: malformed or out-of-range pagination, or unsupported status filter.
- `401 Unauthorized`: authentication is required.

Errors must not reveal SQL, repository details, seller identifiers, enum internals, or stack traces. Listing editing, deletion, renewal, relisting, and all buyer, reservation, checkout, payment, payout, transfer, reveal, refund, dispute, analytics, and bulk-action behavior remain out of scope.

### Cancel Own Listing

Issue `#113` defines this documentation-only contract. Backend implementation remains issue `#114`; the seller control remains issue `#115`.

```http
POST /api/listings/{listingId}/cancel
```

Cancels an authenticated seller's own unsold listing. The request has no body and must not accept seller, ownership, status, reservation, order, payment, or ticket fields. Seller identity is derived only from `AuthenticatedUser.id()`.

Only the terminal transition `ACTIVE -> CANCELLED` is eligible. The endpoint never deletes a listing or modifies reservation, order, payment, provider, or ticket-payload records. A seller may cancel an `ACTIVE` listing even when historical expired reservations or terminal unpaid orders exist, because those retained records are not changed.

#### Response Body

The first successful transition and an owning seller's idempotent retry against an already `CANCELLED` listing both return `200 OK` with `Cache-Control: no-store`:

```json
{
  "id": "33333333-3333-3333-3333-333333333333",
  "status": "CANCELLED",
  "updated_at": "2026-07-19T10:00:00Z"
}
```

The response must not include seller identity, buyer or reservation information, order, payment, provider, payout, refund, dispute, audit, public-note, or ticket-payload data.

#### Authorization, Eligibility, And Idempotency

The service parses the listing UUID, captures one timestamp from the injected application clock, and pessimistically locks the listing row before checking ownership or status. A missing listing and a listing owned by another seller both return the same controlled `404 Not Found` response.

| Current status | Result |
|---|---|
| `ACTIVE` | Transition to `CANCELLED`, set `updated_at` to the captured timestamp, and return `200 OK`. |
| `CANCELLED` | Owning seller receives the unchanged safe response with `200 OK`; do not update `updated_at` or create another audit record. |
| `RESERVED`, `SOLD`, `EXPIRED`, or `DRAFT` | Return `409 Conflict` without mutation. |

The `409` response uses the generic message `Listing cannot be cancelled in its current state`. It must not disclose buyer identity, reservation expiry, order state, payment state, or the cause of the conflict.

Cancellation and reservation creation use the same listing-first pessimistic lock. Exactly one concurrent transition can win: cancellation first makes later reservation eligibility fail; reservation first makes cancellation return `409`. Cancellation must not query or lock a reservation before the listing, reconcile a stale reservation, or introduce another lock order.

#### Audit And Errors

On the first successful transition only, the same transaction writes an immutable `LISTING_CANCELLED` audit event with the authenticated seller ID, entity type `LISTING`, listing ID, and the same captured timestamp used for `listings.updated_at`. Audit persistence is mandatory: an audit failure rolls back cancellation. Audit rows must not contain metadata, seller notes, buyer identity, reservation/order/payment data, request bodies, IP addresses, credentials, or ticket payload data.

| Status | Meaning |
|---:|---|
| `400` | Malformed listing UUID. |
| `401` | Authentication is required. |
| `404` | Listing is missing or is not owned by the authenticated seller. |
| `409` | Owned listing is ineligible for cancellation. |
| `5xx` | Controlled unexpected persistence failure without SQL, entity, lock, audit, credential, session, or stack-trace disclosure. |

This unsafe cookie-authenticated request remains subject to the existing exact trusted-origin protection.

### Create Listing Reservation

```http
POST /api/listings/{listingId}/reservations
```

Creates a server-controlled, 10-minute hold for the authenticated buyer before a future checkout flow. This endpoint has no request body. The server derives the buyer from the authenticated session and calculates the expiry using the injected application clock; clients cannot choose, extend, or renew the hold duration.

Authentication is required. The controller must receive the immutable `AuthenticatedUser` principal with `@AuthenticationPrincipal`; reservation services must receive the trusted buyer ID and must not parse cookies, accept client-provided ownership, or trust an earlier public event-detail response as proof of availability.

#### Eligibility And Concurrency

A reservation may start only when all of the following are true at the server:

- The listing exists.
- Its status is `ACTIVE`.
- Its currency is `VND`.
- Its related event is still upcoming.
- The authenticated buyer is not the listing seller.
- No competing valid reservation owns the listing.

Every transfer method that satisfies these shared eligibility rules is reservable. The MVP does not add a `PLATFORM_TRANSFER`-only rule.

The listing transition from `ACTIVE` to `RESERVED` and the reservation creation are implemented atomically in issue `#54` using a transaction-scoped pessimistic listing lock plus a database partial unique index for active reservations. Concurrent buyers cannot both succeed: exactly one buyer may acquire the hold, while losing or stale requests receive the general availability conflict response. Frontend state and the earlier event-detail response are never authoritative for availability.

#### Response Body

A newly created reservation returns `201 Created`:

```json
{
  "reservation": {
    "id": "44444444-4444-4444-4444-444444444444",
    "listing_id": "33333333-3333-3333-3333-333333333333",
    "status": "ACTIVE",
    "expires_at": "2026-07-16T04:30:00Z"
  }
}
```

| Field | Type | Notes |
|---|---|---|
| `reservation.id` | UUID string | Server-issued reservation identifier. |
| `reservation.listing_id` | UUID string | Reserved listing identifier. |
| `reservation.status` | string | `ACTIVE` while the hold owns the listing. |
| `reservation.expires_at` | ISO-8601 datetime | Server-generated expiry, exactly 10 minutes after creation for a new reservation. |

The response must not include seller identity or contact information, buyer email, ticket payload data, `public_notes`, private transfer links, credentials, session tokens, or cookies.

#### Idempotent Retry

When the same authenticated buyer repeats the request while their reservation remains active, the server returns that existing reservation with `200 OK`. It must not create another row or extend `expires_at`.

#### Expiration

At `expires_at` or later, server time is authoritative: the reservation is expired when `expires_at <= now`. Issue `#55` reconciles expiry both through a configurable fixed-delay cleanup scan (default `60000` ms, at most 100 candidates ordered by `expires_at ASC, id ASC`) and during a new reservation request after the target listing is pessimistically locked.

Expiration changes the reservation from `ACTIVE` to `EXPIRED` and updates its timestamp using the captured server time. The listing returns from `RESERVED` to `ACTIVE` only when its current status is still `RESERVED`; a later `SOLD`, `CANCELLED`, `EXPIRED`, or other state is never overwritten. Expiry processing is idempotent across application instances. An expired hold is never returned as a `200 OK` idempotent retry: a requester instead receives a newly created `201 Created` reservation when the listing remains eligible.

#### Error Behavior

- Malformed `listingId`: `400 Bad Request`.
- Missing listing: `404 Not Found`.
- Missing, malformed, unknown, expired, or revoked session: `401 Unauthorized`.
- Self-owned, already reserved, cancelled, sold, expired, or otherwise unavailable listing: `409 Conflict` with the general message `Listing is no longer available`.
- Unexpected server failures use the standard API `5xx` behavior.

The `409` response must not reveal whether another buyer owns a reservation.

#### Out Of Scope

This endpoint does not define checkout, payment, escrow, `RESERVED -> SOLD`, ticket transfer or reveal, seller contact exchange, buyer-initiated release, extension or renewal, refunds, disputes, admin reservation management, or new audit event types.

## Orders And Checkout

Issue `#65` defines the provider-neutral checkout and order contract for `US-0007`. Issue `#66` provides core order persistence. Issue `#67` implements authenticated create-or-return checkout with a provider-neutral boundary and an in-application mock hosted provider. A production provider is intentionally deferred to a later user story; issue `#68` handles signed event delivery and trusted payment completion, while issue `#69` handles order reconciliation and inventory release.

### Start Checkout

```http
POST /api/reservations/{reservationId}/checkout
```

Starts checkout for the authenticated buyer's active reservation. This endpoint has no request body. The server derives the buyer, reservation, listing, seller, amount, currency, order state, and order expiry; the browser must not send or override any of those values, provider references, or payment success.

The server must revalidate all of the following using the injected application `Clock` before creating or returning an order:

- The caller is authenticated.
- The reservation exists, belongs to the caller, is `ACTIVE`, and has `expires_at > now`.
- The reservation and listing still belong to the same checkout path.
- The listing remains `RESERVED`.
- The caller is not the listing seller.
- The listing amount and currency remain valid for the MVP checkout scope.

An event-detail response, browser countdown, frontend state, or earlier reservation response is never proof that checkout remains eligible.

#### Success And Idempotency

- Return `201 Created` when a new order is created for the reservation.
- Return `200 OK` when the reservation already has its one order and that order may be returned.
- A `PAYMENT_PENDING` order returns the same order and its one active mock hosted session, when available.
- A `PAID` order returns the same safe order representation without a payment URL.
- A `PAYMENT_FAILED`, `CANCELLED`, or `EXPIRED` order returns `409 Conflict` with `Checkout is no longer available`.

Exactly one order may exist for each reservation. Issue `#67` locks listing, reservation, order, then payment session; it uses the unique `orders.reservation_id` and partial usable-session index as final concurrency guards, reloading the existing order after an insert race. One order has at most one usable `CREATING` or `PENDING` payment session. A replacement session is permitted only after the prior session is terminal and remains associated with the same order.

#### Checkout Response

Both `201` and successful `200` results use the following safe order representation:

```json
{
  "order": {
    "id": "55555555-5555-5555-5555-555555555555",
    "reservation_id": "44444444-4444-4444-4444-444444444444",
    "listing_id": "33333333-3333-3333-3333-333333333333",
    "status": "PAYMENT_PENDING",
    "amount_minor": 1250000,
    "currency": "VND",
    "expires_at": "2026-07-16T04:30:00Z",
    "created_at": "2026-07-16T04:20:00Z",
    "updated_at": "2026-07-16T04:20:00Z",
    "paid_at": null,
    "payment_review_required": false,
    "event": {
      "name": "Example Concert",
      "starts_at": "2026-08-15T19:30:00+07:00",
      "venue": "Example Arena",
      "city": "Ho Chi Minh City"
    },
    "ticket": {
      "ticket_type": "General Admission",
      "seat_info": "Section 101, Row B, Seat 12",
      "transfer_method": "PLATFORM_TRANSFER"
    }
  },
  "payment_url": "https://provider.example/hosted-session",
  "payment_url_expires_at": "2026-07-16T04:30:00Z"
}
```

`payment_url` and `payment_url_expires_at` are optional short-lived redirect data, present only when the order remains payable and a valid hosted session exists. They are not payment proof, must not be logged, and must not be persisted in `localStorage` or `sessionStorage`. Checkout-start and protected order-read responses send `Cache-Control: no-store`.

The response must exclude buyer identity and email, seller identity or contact data, provider customer/payment/session/event identifiers, provider secrets or raw payloads, `public_notes`, session tokens or cookies, private transfer information, and QR codes, barcodes, ticket files, credentials, or other sensitive ticket payload data.

#### Expiry And Statuses

`order.expires_at` is exactly `reservation.expires_at`. Checkout never creates a second inventory deadline and never extends, renews, or replaces the reservation deadline. The mock provider session uses that same deadline and rejects action at or after expiry using server time.

The MVP order statuses are `PAYMENT_PENDING`, `PAID`, `PAYMENT_FAILED`, `CANCELLED`, and `EXPIRED`. The only allowed transitions are:

```text
PAYMENT_PENDING -> PAID
PAYMENT_PENDING -> PAYMENT_FAILED
PAYMENT_PENDING -> CANCELLED
PAYMENT_PENDING -> EXPIRED
```

`PAID`, `PAYMENT_FAILED`, `CANCELLED`, and `EXPIRED` are terminal. Failed, cancelled, and expired orders must not be reactivated. A valid pending retry recovers the same order and its one usable provider session.

#### Error And Privacy Behavior

- Malformed, missing, or non-owned checkout `reservationId`: `404 Not Found`. The deferred order-read endpoint will define its own malformed `orderId` behavior.
- Missing, malformed, unknown, expired, or revoked session: `401 Unauthorized`.
- Missing reservation or a reservation owned by another buyer: `404 Not Found`.
- Expired reservation, seller self-checkout, inconsistent reservation or listing state, terminal order, or otherwise unavailable checkout: `409 Conflict` with `Checkout is no longer available`.
- Temporary hosted-payment-provider unavailability during later session creation: `503 Service Unavailable` with a controlled response.

Errors must not reveal seller identity, reservation ownership, provider references or configuration, webhook state, or internal financial details. The same `404` behavior for absent and non-owned reservations prevents ownership enumeration.

### Mock Hosted Provider

Issue `#67` exposes a local provider-style checkout route for development only:

```http
GET /mock-provider/checkout/{providerSessionId}
POST /mock-provider/sessions/{providerSessionId}/succeed
POST /mock-provider/sessions/{providerSessionId}/fail
POST /mock-provider/sessions/{providerSessionId}/cancel
```

These routes are public provider-facing routes and do not require a TicketPass login. The page displays only amount, currency, server-provided expiry, and provider session state. It accepts a success, decline, or cancellation action only while the mock session is `PENDING` and unexpired. A valid action changes only mock provider state, writes one durable pending provider event, and redirects to the configured frontend `/checkout/{orderId}` route with `provider_return=success`, `failed`, or `cancelled`.

The return query parameter is a presentation hint only. It cannot transition the TicketPass order, reservation, or listing. At or after expiry the mock provider marks the session unavailable and rejects payment actions.

### Mock Payment Webhook

```http
POST /api/payments/webhooks/mock
```

This public provider-to-provider endpoint accepts no browser authentication or CORS authority. It ignores TicketPass session cookies and is excluded from cookie-origin validation. The mock provider signs the exact UTF-8 JSON bytes with `X-Mock-Timestamp` (Unix seconds) and `X-Mock-Signature: v1=<lowercase HMAC-SHA256 hex>` over `<timestamp>.<raw body>`. TicketPass rejects empty bodies and bodies over `16 KiB`, validates the required header shapes, then verifies the HMAC in constant time and rejects timestamps more than five minutes from the injected server `Clock` before parsing JSON.

The minimal payload contains only `event_id`, `event_type`, `provider_session_id`, `amount_minor`, `currency`, and `occurred_at`. It must not contain TicketPass IDs, identities, checkout URLs, ticket data, or payment credentials. Valid requests return `200` for processing, duplicates, deferred failures/cancellations, ignored events, and safe late/inconsistent outcomes; malformed payloads return `400`; missing, malformed, stale, or invalid signatures return a controlled `401`; transient failures return `500`.

The mock outbox claims one due event in a short transaction by moving its next-attempt time to a lease and incrementing attempt metadata. It signs and posts outside a database transaction with bounded connection/request timeouts, redirects disabled, and discarded response bodies, then finalizes delivery in a separate short transaction. A lease that outlives a process permits later retry. It marks an event delivered only after `2xx`, retries failed delivery at bounded exponential delays, and dead-letters after eight attempts. TicketPass atomically deduplicates each provider event in a webhook receipt ledger. A verified successful event locks listing, reservation, order, then payment session; revalidates their relationships, amount, currency, matching expiry, and current state; then atomically moves `PENDING -> PAID`, `PAYMENT_PENDING -> PAID`, and `RESERVED -> SOLD`. Late or inconsistent success is recorded as `REQUIRES_ACTION` without changing marketplace state. Verified failure and cancellation events are initially `DEFERRED` and are reconciled by issue `#69` under the same listing-first lock order.

### Read Order

```http
GET /api/orders/{orderId}
```

Returns a safe order representation only to its authenticated buyer. It supports `/checkout/{orderId}`, hard-refresh recovery, navigation back to checkout, provider return routes, and server-authoritative state refresh.

Before responding, the server processes a matching verified `DEFERRED` failure/cancellation receipt when safe, reconciles an overdue `PAYMENT_PENDING` order with the injected `Clock`, and then reloads the server-authoritative result. Scheduled cleanup not having run is not a reason to return stale pending state. An unresolved `REQUIRES_ACTION` receipt blocks automated release and makes `payment_review_required` `true`. The response uses the safe order representation above but never includes a stored provider checkout URL. A new or recovered payable URL belongs only to checkout-start or later approved session recovery.

Malformed `orderId` returns `400 Bad Request`; missing, non-owned, or otherwise inaccessible orders return `404 Not Found`; missing or invalid authentication returns `401 Unauthorized`.

### Seller Transfer Confirmation

Issue `#92` defines this post-payment lifecycle contract. Issue `#93` implements the persistence and backend endpoint; the seller browser flow remains issue `#94`.

```http
POST /api/seller/orders/{orderId}/transfer-confirmation
```

The endpoint has no request body. It derives the authenticated seller solely from `AuthenticatedUser`; clients cannot submit a seller or buyer ID, transfer status, timestamp, or settlement state. It requires authentication and sends `Cache-Control: no-store` on a successful response.

A verified payment success must atomically retain the existing payment transition and create the one-to-one fulfilment state:

```text
order.status                 PAYMENT_PENDING -> PAID
listing.status               RESERVED -> SOLD
transfer_status              AWAITING_SELLER_TRANSFER
settlement_status            FUNDS_HELD
transfer_deadline_at         paid_at + 15 minutes
```

The server captures one trusted `Instant` for `paid_at`, fulfilment creation, and deadline calculation. Browser time, countdowns, provider redirects, provider-event `occurred_at`, and seller requests cannot choose or extend the deadline.

Under the established lock order `listing -> reservation -> order -> fulfilment`, the implementation revalidates seller ownership, `PAID` payment status, `SOLD` listing status, fulfilment ownership, deadline consistency, and coherent timestamps and statuses. A first eligible confirmation transitions `AWAITING_SELLER_TRANSFER -> SELLER_CONFIRMED_TRANSFER`, sets immutable `seller_confirmed_at` and `updated_at` to the captured server time, and leaves settlement `FUNDS_HELD`. A coherent repeated seller confirmation returns `200 OK` with the existing immutable timestamp and makes no write, including after the deadline. A later coherent buyer-confirmed or released state may also be returned idempotently without reversal.

At `now >= transfer_deadline_at`, an awaiting seller confirmation is ineligible and returns a controlled conflict without writing a timeout state. Timeout transition and request-time reconciliation remain deferred to issues `#98` and `#99`. Timed-out, review-required, unpaid, failed, cancelled, expired, missing, and inconsistent orders must never be reactivated.

The seller-safe response contains only `order_id`, payment/transfer/settlement statuses, `paid_at`, `transfer_deadline_at`, `seller_confirmed_at`, and approved event and ticket summaries. It excludes buyer identity or contact data, provider IDs, checkout URLs, webhook records, settlement-provider details, public notes, ticket files, QR codes, barcodes, credentials, and private transfer links. Seller confirmation is only a claim that transfer was performed: it does not prove buyer receipt, authorize payout, or release held settlement.

Malformed order IDs return `400 Bad Request`; missing authentication returns `401 Unauthorized`; missing and non-owned orders return the same `404 Not Found`; and unpaid, expired, timed-out, review-required, or otherwise ineligible states return controlled `409 Conflict` responses without payment, provider, review, buyer, or database details.

### Buyer Receipt Confirmation And Settlement Release

Issue `#95` defines this contract. Backend persistence and endpoint implementation remain issue `#96`; protected browser work remains issue `#97`.

```http
POST /api/orders/{orderId}/receipt-confirmation
```

The endpoint accepts no request body and requires authentication. It derives buyer ownership only from `AuthenticatedUser`. Clients cannot supply buyer or seller IDs, amount, currency, payment, transfer, settlement, timestamp, provider, or other server-controlled values. A successful response sends `Cache-Control: no-store`. The browser action must state that confirming receipt authorizes settlement release and cannot be undone through the normal fulfilment flow.

A first confirmation is eligible only when all authoritative conditions hold:

```text
order.status                 = PAID
listing.status               = SOLD
transfer_status              = SELLER_CONFIRMED_TRANSFER
settlement_status            = FUNDS_HELD
seller_confirmed_at          is not null
buyer_confirmed_at           is null
```

The server must also require buyer ownership and reject any timeout, review, refund, dispute, or conflicting release state. Once a seller validly confirmed before the seller deadline, this contract adds no buyer deadline. Browser time never decides eligibility. Timed-out, review-required, unpaid, failed, cancelled, expired, refunded, or inconsistent orders return controlled conflict responses.

Under one transaction, the implementation reloads and revalidates every authoritative row using the established lock order:

```text
listing -> reservation -> order -> fulfilment -> release operation
```

The first valid request transitions `SELLER_CONFIRMED_TRANSFER -> BUYER_CONFIRMED_RECEIPT`, captures one server time for `buyer_confirmed_at` and `updated_at`, and leaves settlement `FUNDS_HELD`. It commits that local transition before attempting external settlement. Buyer confirmation does not imply external release has completed. `buyer_confirmed_at` is immutable; repeated coherent requests recover the existing workflow without another confirmation or timestamp.

#### Durable Settlement Operation

Issue `#96` adds a private `settlement_release_operations` record for each order. Its stable unique idempotency key, `settlement-release:<order-id>`, is reused across HTTP retries, worker retries, restarts, and uncertain provider responses. The operation stores only provider execution state; amount and currency are always derived from the locked paid order. The authenticated endpoint rejects a non-empty body, locks and validates buyer-owned paid/sold fulfilment state, persists buyer confirmation before calling the provider, and returns `202 Accepted` while the release remains held or `200 OK` only when release is confirmed.

The provider-neutral settlement boundary is limited to:

```text
release(orderId, amountMinor, currency, idempotencyKey)
lookup(idempotencyKey or providerOperationId)
```

It is separate from checkout-session APIs. A development-only adapter must return the same provider operation for the same stable key. It validates application control flow only and is not production settlement infrastructure; it remains disabled outside explicit local configuration.

External calls must run outside marketplace locks. Transaction A records or recovers buyer confirmation and the operation. A bounded claim commits `PROCESSING`; the provider call follows; then Transaction B locks and reloads the marketplace rows and operation before applying the result. Provider-confirmed success performs exactly once:

```text
settlement_status       FUNDS_HELD -> RELEASED_TO_SELLER
settlement_released_at  -> server-observed completion time
operation.status        -> SUCCEEDED
operation.completed_at  -> same completion time
```

Timeout or unknown provider responses do not prove failure. Settlement remains `FUNDS_HELD`, and the operation is recovered through lookup or retry with the same key. Pending or unknown outcomes remain recoverable; retryable failures use a bounded retry; contradictory or permanent outcomes enter review-required state without claiming release or refund. A crashed claim becomes recoverable after its lease. The system must not automatically refund after a release failure; later refund or dispute work must inspect the operation first.

#### Idempotency, Response, And Errors

`200 OK` returns when release is confirmed or a later request finds the completed workflow. `202 Accepted` returns when buyer confirmation and durable release acceptance succeeded but provider completion remains pending, including a retry while pending. A controlled `5xx` is reserved for local failure before durable acceptance.

The buyer-safe response includes only `order_id`, payment/transfer/settlement statuses, `paid_at`, `transfer_deadline_at`, `seller_confirmed_at`, `buyer_confirmed_at`, `settlement_released_at`, `buyer_action`, `status_refresh_required`, safe event/ticket summaries, `amount_minor`, and `currency`. While release is pending, it represents `BUYER_CONFIRMED_RECEIPT`, `FUNDS_HELD`, `buyer_action = NONE`, and `status_refresh_required = true`.

Responses exclude seller identity/contact data, provider IDs, internal operation state, retries or errors, payment-session records, webhook data, private transfer data, ticket files, QR codes, barcodes, credentials, and internal review details.

- `400 Bad Request`: malformed order ID or unexpected body.
- `401 Unauthorized`: authentication required.
- `404 Not Found`: missing or non-owned order, using the same response for both.
- `409 Conflict`: seller transfer incomplete, timeout/review active, settlement ineligible, or conflicting terminal state.
- `5xx`: controlled local failure before durable acceptance.

Provider unavailability after stored buyer confirmation normally returns recoverable `202 Accepted`, never a false failure or false release. The implementation writes `BUYER_RECEIPT_CONFIRMED` only for the first effective buyer transition and `SETTLEMENT_RELEASED` only for first confirmed release. Logs may include only the order ID and bounded status categories; they exclude identities, amounts, bodies, provider payloads, cookies, sessions, ticket data, and raw errors.

### Browse Buyer Order Progress

```http
GET /api/me/orders
```

Issue `#87` defines this authenticated, read-only account-history contract. Backend implementation is deferred to issue `#88`, after the approved post-payment lifecycle persistence work. The server derives buyer ownership solely from `AuthenticatedUser`; clients must not supply a buyer ID or any ownership, payment, transfer, or settlement state.

The endpoint accepts these query parameters:

| Parameter | Required | Rules |
|---|---:|---|
| `page` | No | 1-based; default `1`; minimum `1`. |
| `page_size` | No | Default `20`; minimum `1`; maximum `50`. |
| `payment_status` | No | Exact approved payment lifecycle value. |
| `transfer_status` | No | Exact approved transfer lifecycle value. |
| `settlement_status` | No | Exact approved settlement lifecycle value. |

The server applies buyer ownership and every supplied exact filter in the database before pagination. Results are ordered by `created_at DESC, id DESC`. Empty results, including pages beyond the final page, return `200 OK` with an empty `items` collection and accurate totals. Invalid pagination or a status value outside its approved lifecycle returns controlled `400 Bad Request`; missing or invalid authentication returns `401 Unauthorized`.

Each order exposes three distinct status dimensions. They must never be collapsed into one client-derived order status:

```text
payment_status
transfer_status
settlement_status
```

Before post-payment fulfilment exists, the future response representation uses `NOT_STARTED` for transfer and `NOT_FUNDED` for settlement. The approved post-payment values are:

```text
transfer_status: AWAITING_SELLER_TRANSFER, SELLER_CONFIRMED_TRANSFER,
                 BUYER_CONFIRMED_RECEIPT, TRANSFER_TIMED_OUT, REQUIRES_REVIEW
settlement_status: FUNDS_HELD, RELEASED_TO_SELLER, REFUND_REQUIRED, REVIEW_REQUIRED
```

`PAID` means only that trusted payment confirmation succeeded. Seller transfer confirmation is only the seller's claim that transfer was performed; it neither proves buyer receipt nor releases funds. Only the approved buyer confirmation flow may authorize release on the happy path. Timeout and review states block release.

The safe paginated response shape is:

```json
{
  "items": [
    {
      "id": "11111111-1111-1111-1111-111111111111",
      "payment_status": "PAID",
      "transfer_status": "AWAITING_SELLER_TRANSFER",
      "settlement_status": "FUNDS_HELD",
      "amount_minor": 1250000,
      "currency": "VND",
      "expires_at": "2026-07-20T10:00:00Z",
      "paid_at": "2026-07-20T09:01:00Z",
      "transfer_deadline_at": "2026-07-20T09:16:00Z",
      "seller_confirmed_transfer_at": null,
      "buyer_confirmed_receipt_at": null,
      "settlement_released_at": null,
      "payment_review_required": false,
      "fulfilment_review_required": false,
      "status_refresh_required": false,
      "buyer_action": "WAIT_FOR_SELLER_TRANSFER",
      "event": {
        "name": "Example Concert",
        "starts_at": "2026-10-17T11:30:00Z",
        "venue": "National Stadium",
        "city": "Singapore"
      },
      "ticket": {
        "ticket_type": "General Admission",
        "seat_info": "Section A, Row 2, Seat 4",
        "transfer_method": "PLATFORM_TRANSFER"
      }
    }
  ],
  "page": 1,
  "page_size": 20,
  "total_items": 1,
  "total_pages": 1
}
```

`buyer_action` is bounded, server-derived presentation guidance only: `NONE`, `CONTINUE_PAYMENT`, `WAIT_FOR_SELLER_TRANSFER`, `CONFIRM_TICKET_RECEIPT`, `OPEN_ORDER_FOR_REFRESH`, or `REVIEW_REQUIRED`. It does not authorize a mutation. Checkout and later fulfilment endpoints remain the mutation authorities.

The list read is a persisted snapshot. It must not reconcile every row, call a payment provider, or perform bulk timeout processing. Scheduled reconciliation owns bulk expiry and timeout handling; `GET /api/orders/{orderId}` remains the request-time authoritative route for one selected order. `status_refresh_required` is `true` only when persisted state may be stale against a passed server deadline; the browser must open or refresh that single-order route instead of inventing a local status.

Successful responses send `Cache-Control: no-store`. Responses must exclude seller identity and contact data, listing or reservation identifiers, hosted payment URLs, payment-provider records and webhook receipts, credentials, public notes, QR codes, barcodes, ticket files, and all private ticket-transfer data.

### Payment Completion Authority

Only a verified provider webhook or equivalent trusted server-to-server confirmation may atomically perform:

```text
order:   PAYMENT_PENDING -> PAID
listing: RESERVED -> SOLD
```

That confirmation must revalidate the order, reservation identity/ownership/status/expiry, listing identity/status, amount, currency, approved provider references, and trusted provider payment status. Browser redirects, query parameters, frontend state, hosted-session creation, and browser API calls can never mark an order paid or a listing sold.

A trusted failure received before the inherited deadline atomically moves `PENDING -> FAILED`, `PAYMENT_PENDING -> PAYMENT_FAILED`, `ACTIVE -> CANCELLED`, and `RESERVED -> ACTIVE`; cancellation follows the equivalent `CANCELLED` path. At or after the inherited deadline, local expiry wins and moves the operational session, order, and reservation to `EXPIRED` before releasing a still-`RESERVED` listing. A browser cancellation redirect alone is non-authoritative. Automated release is blocked by an unresolved `REQUIRES_ACTION` receipt and never reverses `PAID`/`SOLD` state.

When verified success arrives after local order or reservation expiry, the server must not mark the listing `SOLD`, overwrite the terminal order state, or alter unrelated inventory. It must durably record or deduplicate the trusted provider event and surface it for future manual handling or refund processing. Refund execution is outside this contract.

Provider-specific objects and payloads must not become part of the TicketPass order API or core domain model. Issue `#67` uses a provider-neutral `PaymentProvider` interface and a configured mock implementation only. The mock’s hosted route is `/mock-provider/checkout/{providerSessionId}`; its public actions create durable provider events but do not transition TicketPass orders or listings. `payment_url` and return redirects are built only from startup-validated server configuration, never browser input. Mock session path identifiers must be canonical UUIDs; malformed and unknown values return controlled `404` without reflection. Hosted pages and action responses send `Cache-Control: no-store`, and hosted pages also send a script-free CSP, `X-Content-Type-Options: nosniff`, and `Referrer-Policy: no-referrer`. Production-provider selection and SDK integration are deferred to a later user story. This contract adds no generic payment audit events; provider replay/deduplication records are operational payment records.

## Events

Events let buyers browse upcoming event-first marketplace inventory without exposing sensitive ticket or seller information.

Issue `#25` defines this public browse contract. Backend implementation belongs to issue `#26`, and frontend implementation belongs to issue `#27`.

Issue `#31` defines the authenticated seller event autocomplete contract. Issue `#33` implements the backend endpoint, and frontend autocomplete implementation belongs to issue `#35`.

Issue `#77` defines the authenticated missing-event request contract. Issue `#78` implements the backend endpoint and persistence, and seller UI implementation belongs to issue `#79`. Issue `#145` defines the future admin-review and seller-tracking contract; backend delivery proceeds through issues `#146` to `#149`, and seller tracking through `#150` and `#151`.

### Create Missing-Event Request

```http
POST /api/event-requests
```

Allows an authenticated seller to submit untrusted metadata for a future event that is missing from the TicketPass catalogue. The request does not create, approve, publish, or modify an `events` row, and it cannot be used to create a listing.

The implemented endpoint requires an authenticated TicketPass session and the existing trusted-origin protection for unsafe cookie-authenticated requests. The controller derives requester ownership only from `AuthenticatedUser`. Request bodies accept only the documented request fields; they must not include `requester_id`, `user_id`, seller identity, status, timestamps, or an event ID.

#### Request Body

```json
{
  "event_name": "Example Concert",
  "starts_at": "2026-10-17T19:30:00+08:00",
  "venue": "National Stadium",
  "city": "Singapore",
  "official_url": "https://example.com/events/example-concert"
}
```

#### Request Fields

| Field | Type | Required | Notes |
|---|---|---:|---|
| `event_name` | string | Yes | Nonblank after normalization; maximum 255 characters. |
| `starts_at` | RFC 3339 timestamp | Yes | Must contain `Z` or an explicit UTC offset. Offset-free local date-times are rejected. |
| `venue` | string | Yes | Nonblank after normalization; maximum 255 characters. |
| `city` | string | Yes | Nonblank after normalization; maximum 120 characters. |
| `official_url` | string | No | Absolute HTTPS URL with host, no username/password component, maximum 2048 characters. Untrusted review metadata only. |

#### Validation And Duplicate Behavior

- The server parses `starts_at` to an `Instant` and stores the absolute time. It must be strictly after one server timestamp captured from the injected application `Clock`.
- The current event model does not preserve an IANA event timezone. This endpoint must not infer or invent one.
- Preserve trimmed display values. For duplicate detection, normalize `event_name`, `venue`, and `city` by trimming leading/trailing whitespace, collapsing each internal whitespace sequence to one ASCII space, and lowercasing with locale-independent rules.
- Required fields that become blank after normalization are invalid.
- `official_url` is stored only as untrusted review metadata. The backend must not fetch it, follow redirects, scrape it, infer authenticity from it, or expose it as trusted public catalogue data.
- The initial and only status defined by this contract is `PENDING`. It means awaiting future catalogue review; it does not mean verified, approved, public, or listing-eligible.
- An obvious duplicate is scoped to the authenticated requester and exists only when a `PENDING` request has the same requester, normalized event name, `starts_at`, normalized venue, and normalized city. `official_url` is not part of the duplicate key.
- Duplicate detection must be database-backed so concurrent submissions cannot create duplicate pending rows. Cross-user requests are never merged by this contract.

#### Response Body

New requests return `201 Created`; an existing obvious duplicate pending request for the same requester returns `200 OK`. Both use the same safe representation:

```json
{
  "id": "11111111-1111-1111-1111-111111111111",
  "status": "PENDING",
  "event_name": "Example Concert",
  "starts_at": "2026-10-17T11:30:00Z",
  "venue": "National Stadium",
  "city": "Singapore",
  "official_url": "https://example.com/events/example-concert",
  "created_at": "2026-07-18T11:00:00Z",
  "updated_at": "2026-07-18T11:00:00Z"
}
```

Responses must not include requester identity, normalized values, duplicate-key details, moderation internals, or an `event_id`.

Both successful responses include `Cache-Control: no-store`.

#### Error Behavior

- Malformed JSON, missing fields, invalid bounds, invalid URL, invalid timestamp, or an event time that is not in the future: `400 Bad Request`.
- Missing, malformed, unknown, expired, or revoked session: `401 Unauthorized`.
- Request rejected by the trusted-origin protection: `403 Forbidden`.

An obvious duplicate is not a conflict and returns `200 OK`. Errors must not expose database constraints, normalization values, requester information, catalogue internals, or stack traces.

#### Listing Boundary And Sensitive Data

An event-request ID is never an event ID. `POST /api/listings` continues accepting only an existing `events.id`; a pending request must not bypass the existing seller event-selection and listing-creation rules.

Submitted text and URLs are untrusted metadata. Responses and logs must exclude requester identity, email, session or credential data, ticket data, request bodies, raw submitted text, raw official URLs, normalized values, and moderation internals. Logs may contain only safe operational identifiers, creation/recovery outcome, and controlled error category.

### Admin Event-Request Review

Issue `#145` defines this contract only. All endpoints below require an authenticated persisted `ADMIN` role, are subject to the existing trusted-origin protection when unsafe, send `Cache-Control: no-store`, and never fetch an `official_url`.

```http
GET  /api/admin/event-requests
GET  /api/admin/event-requests/{requestId}
POST /api/admin/event-requests/{requestId}/approve-create
POST /api/admin/event-requests/{requestId}/approve-link
POST /api/admin/event-requests/{requestId}/reject
```

#### Admin Queue And Detail

`GET /api/admin/event-requests` accepts bounded 1-based `page` and `page_size`, an optional exact `status` (`PENDING`, `APPROVED`, or `REJECTED`), and optional bounded literal text `q`. Filtering, counting, ordering, and pagination occur in the database. A pending queue is ordered `created_at ASC, id ASC`; terminal rows are ordered `reviewed_at DESC, id DESC`.

The queue and detail response expose request metadata, the untrusted `official_url`, exact pending-sibling count, status, and safe resolution fields. They exclude requester identity, normalized values, sessions, ticket data, listings, payments, and reviewer identity. The service must not follow, inspect, scrape, or trust the submitted URL.

#### Resolve A Request

`approve-create` accepts corrected `event_name`, an offset-bearing future `starts_at`, `venue`, and `city`. It creates or recovers the one exact normalized event identity. `approve-link` accepts exactly one existing future `event_id`. `reject` accepts one bounded controlled `rejection_reason` and an optional bounded seller-facing `resolution_message` of at most 500 characters.

Resolution captures one server timestamp and completes in one transaction: serialize the exact request identity, lock and revalidate the target and exact pending siblings, create/recover or validate the event, resolve the target and matching siblings, persist the required audit rows, then commit once. The unique event-identity index is the final duplicate guard. Similar but nonexact requests are never automatically resolved.

Repeated identical terminal decisions return the current safe state without changing timestamps or audit history. A conflicting terminal decision returns `409 Conflict`; concurrent decisions allow exactly one winner. Missing, non-admin, and non-owned internal targets must use controlled responses that do not expose request ownership or catalogue internals.

`approve-create` produces `CREATED_EVENT` for the direct request and `EXACT_MATCHED` for resolved pending siblings. `approve-link` produces `LINKED_EVENT` for the direct request and `EXACT_MATCHED` for resolved pending siblings. A direct rejection produces `REJECTED` and never resolves siblings.

### Seller Event-Request Tracking

```http
GET /api/me/event-requests
GET /api/me/event-requests/{requestId}
```

These authenticated seller endpoints return only rows whose `requester_user_id` is the session-derived user. Results are ordered `created_at DESC, id DESC` and support bounded database-side pagination. `PENDING` exposes the seller-safe request metadata. `APPROVED` additionally exposes the safe resolved-event summary. `REJECTED` exposes only the seller-facing reason and optional message.

Seller responses exclude reviewer identity, sibling data, normalized fields, audit rows, other requesters, sessions, ticket data, listings, payments, and moderation internals. A missing or non-owned request returns the same controlled not-found response. All successful responses send `Cache-Control: no-store`.

An approved seller may return to `/sell?event_request_id={owned-request-id}`. The seller form loads the owned request from the server and preselects only its server-returned resolved event. The request ID is never treated as an event ID, and normal listing creation continues to revalidate event and listing rules server-side.

### Event Autocomplete

```http
GET /api/events/autocomplete?q={query}
```

Returns a small set of seller-safe existing event summaries for the seller listing flow. Authentication is required because this endpoint exists specifically to let sellers select an existing TicketPass event before creating a listing.

Backend implementation enforces authentication, query validation, matching, result limits, and event eligibility server-side.

#### Query Parameters

| Field | Type | Required | Notes |
|---|---|---:|---|
| `q` | string | Yes | Search query. Trim surrounding whitespace before validation and matching. Minimum `3` characters and maximum `100` characters after trimming. |

Invalid, missing, blank, shorter-than-3, or longer-than-100 `q` values return `400 Bad Request`.

#### Query Behavior

- Maximum response size is `10` events.
- No pagination for MVP.
- No matches return `200 OK` with an empty `events` array.
- Frontend autocomplete requests should use an approximately `300 ms` debounce.

#### Searchable Fields

MVP autocomplete searches these existing event fields:

- `events.name`
- `events.venue`
- `events.city`

Event name, venue, and city matches use case-insensitive substring matching. Accent-insensitive matching is deferred and must not be introduced implicitly through a database extension, migration, or dependency change.

#### Ranking And Ordering

Results must be ordered deterministically:

1. Exact event-name match.
2. Event-name prefix match.
3. Event-name substring match.
4. Venue or city match.
5. `starts_at ASC`.
6. `id ASC` as the final stable tie-breaker.

Event-name exact and prefix matches receive higher ranking than general substring matches. Venue and city matches are substring matches and are ranked after event-name substring matches. When an event matches multiple categories, its highest-ranking category is used.

Advanced fuzzy matching, recommendations, and full marketplace search are out of scope.

#### Event Eligibility

Autocomplete includes existing future events even when they currently have no active listings. This allows a seller to create the first listing for an existing event.

For MVP:

- `events.starts_at` must be in the future at request time.
- The event must exist in TicketPass and be selectable by its server-issued `event_id`.
- Event-level cancelled, hidden, public/private, or moderation filtering must be applied only when supported by the schema and documented rules.

The current MVP schema does not define event-level cancellation, hidden, public/private, moderation, or rescheduling fields. These lifecycle gaps are tracked in `docs/CONCERNS.md`; this contract must not invent unsupported event schema fields.

#### Response Body

```json
{
  "events": [
    {
      "id": "evt_123",
      "name": "Example Concert",
      "starts_at": "2026-08-15T19:30:00+07:00",
      "venue": "Example Arena",
      "city": "Ho Chi Minh City"
    }
  ]
}
```

#### Response Fields

| Field | Type | Notes |
|---|---|---|
| `events` | array | Seller-safe event summaries. Empty when a valid query has no matches. |
| `events[].id` | string | Server-issued event identifier to use as `event_id` in the listing flow. |
| `events[].name` | string | Event display name. |
| `events[].starts_at` | ISO-8601 datetime | Event start timestamp with timezone offset, such as `2026-08-15T19:30:00+07:00`. |
| `events[].venue` | string | Venue name. |
| `events[].city` | string | Event city/location. |

#### Error And Empty Behavior

- Missing, blank, shorter-than-3, or longer-than-100 `q`: `400 Bad Request`.
- Unauthenticated request: `401 Unauthorized`.
- Valid query with no results: `200 OK` and `{"events": []}`.
- Unexpected server failure: standard API `5xx` error behavior.

#### Sensitive Data Exclusions

The event autocomplete response must not include:

- Event platform.
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

### Browse Events

```http
GET /api/events
```

Returns event summaries for events with at least one browse-eligible listing.

This endpoint is public, but all event availability, visibility, aggregate calculation, pagination, and ordering must be enforced server-side.

Issue `#26` implements the original paginated endpoint using a database-side grouped query over events and browse-eligible listings. Issue `#109` defines the optional search and filter contract below; backend implementation belongs to issue `#110` and frontend controls belong to issue `#111`.

#### Query Parameters

| Field | Type | Required | Default | Notes |
|---|---|---:|---:|---|
| `page` | integer | No | `1` | 1-based page number. Minimum `1`. |
| `page_size` | integer | No | `20` | Minimum `1`. Maximum `100`. |
| `q` | string | No | None | Event text query. After normalization, must contain `2` to `100` characters. |
| `city` | string | No | None | Exact city filter. After normalization, must contain `1` to `120` characters. |
| `starts_from` | RFC 3339 timestamp | No | None | Inclusive lower event-time bound. Must include `Z` or an explicit numeric offset. |
| `starts_before` | RFC 3339 timestamp | No | None | Exclusive upper event-time bound. Must include `Z` or an explicit numeric offset. |

Invalid pagination or filter values return `400 Bad Request`.

Non-integer pagination values return `400 Bad Request` with a controlled API error message.

#### Search And Filter Normalization

For `q` and `city`, the server must trim leading and trailing Unicode whitespace, then collapse each internal Unicode whitespace run to one space before validation and matching. An empty normalized value is treated as omitted.

`q` is optional, but when present after normalization it must be `2` through `100` characters. `city` is optional, but when present after normalization it must be `1` through `120` characters.

`starts_from` and `starts_before` must be RFC 3339 timestamps with `Z` or an explicit numeric offset. When both bounds are supplied, `starts_from` must be earlier than `starts_before`. A valid time range entirely before the current upcoming-event window may return an empty page; it is not malformed.

#### Search And Filter Matching

`q` performs a case-insensitive literal substring match across `events.name`, `events.venue`, and `events.city`. Characters such as `%`, `_`, and the database query's chosen escape character must be escaped before use in `LIKE`; client input must not become a wildcard expression.

`city` performs a case-insensitive exact match after normalization. When both `q` and `city` are supplied, both predicates must match.

Time predicates apply to `events.starts_at`:

```text
event.starts_at >= starts_from     when supplied
event.starts_at < starts_before    when supplied
```

These optional predicates do not replace the existing requirement that returned events remain upcoming at request time.

#### Filter, Aggregate, And Pagination Behavior

All supplied filters must be applied in the database inside the existing aggregate query before grouping, counting, ordering, and pagination. Do not load broad event or listing sets for application-memory filtering.

The existing shared browse-eligible listing predicate, active eligible VND listing scope, server-derived lowest price and listing count, safe event-summary fields, and deterministic `starts_at ASC, id ASC` ordering remain unchanged. Requests without filters must retain the existing browse behavior.

A valid page with no matches returns `200 OK` with an empty `events` array and accurate pagination metadata.

#### Browse-Eligible Listing Rule

A listing is browse-eligible for MVP only when all of these are true:

- `listings.status` is `ACTIVE`.
- The related `events.starts_at` is in the future at request time.
- `listings.currency` is `VND`.
- The listing is currently available for purchase under the listing status rules in `docs/flows/LISTING_STATUS_FLOW.md`.

This same browse-eligible listing set must be used for:

- deciding whether an event appears in `GET /api/events`;
- calculating `lowest_price_minor`;
- calculating `available_listing_count`.

An event appears in browse results only if it has at least one browse-eligible listing.

The event-first user story excludes expired, cancelled, hidden, and non-public events. The current MVP schema can enforce expired events through `events.starts_at` and listing availability through `listings.status`, but it does not yet define event-level cancellation, hidden, or public/private fields. Those lifecycle and visibility gaps are tracked in `docs/CONCERNS.md`.

#### Ordering

Results are ordered deterministically:

1. `starts_at ASC`
2. `id ASC`

Only future events are included, so the default order shows the soonest upcoming events first.

#### Response Body

```json
{
  "events": [
    {
      "id": "evt_123",
      "name": "Example Concert",
      "starts_at": "2026-08-15T19:30:00+07:00",
      "venue": "Example Arena",
      "city": "Ho Chi Minh City",
      "image_url": null,
      "lowest_price_minor": 1250000,
      "currency": "VND",
      "available_listing_count": 3
    }
  ],
  "pagination": {
    "page": 1,
    "page_size": 20,
    "total_items": 42,
    "total_pages": 3
  }
}
```

#### Response Fields

| Field | Type | Notes |
|---|---|---|
| `events` | array | Event summaries for the requested page. Empty when no events match. |
| `events[].id` | string | Event identifier. |
| `events[].name` | string | Event display name. |
| `events[].starts_at` | ISO-8601 datetime | Event start timestamp with timezone offset, such as `2026-08-15T19:30:00+07:00`. |
| `events[].venue` | string | Venue name. |
| `events[].city` | string | Event city/location. |
| `events[].image_url` | string or null | Always present. `null` means the frontend should render a safe placeholder. |
| `events[].lowest_price_minor` | integer | Lowest asking price among browse-eligible listings for the event. |
| `events[].currency` | string | Always `VND` for browse MVP. |
| `events[].available_listing_count` | integer | Count of browse-eligible listings currently available for purchase. |
| `pagination.page` | integer | Current 1-based page. |
| `pagination.page_size` | integer | Applied page size after defaulting and validation. |
| `pagination.total_items` | integer | Total number of matching events before pagination. |
| `pagination.total_pages` | integer | Total number of pages for the applied page size. |

#### Sensitive Data Exclusions

The public browse events response must not include:

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

#### Error Behavior

Return a controlled `400 Bad Request` for malformed pagination, an overlong text value, a one-character `q`, malformed timestamps, or an invalid time range. Errors must not expose SQL, JPQL, repository details, stack traces, or normalized internal query strings.

The response shape remains unchanged for filtered and unfiltered requests.

#### Currency Scope

Browse events support only `VND` for MVP. Listings in another currency are not browse-eligible and must not affect event visibility, lowest price, or available listing count.

The listing creation contract also stores new MVP listings as `VND` and does not accept `currency` from the client. Issue `#34` implements this backend and database alignment.

### Event Detail With Available Listings

```http
GET /api/events/{eventId}?page=1&page_size=20
```

Returns a public event summary and the event's paginated browse-eligible listings in one response.

This endpoint is public, but event eligibility, listing availability, pagination, ordering, and sensitive-data exclusions must be enforced server-side. The public event summary and listing collection are intentionally not split into separate MVP requests.

Issue `#44` defines this contract only. Backend implementation belongs to issue `#45`, and the frontend event-detail page belongs to issue `#46`.

#### Path Parameters

| Field | Type | Required | Notes |
|---|---|---:|---|
| `eventId` | UUID | Yes | Event identifier. Malformed UUID values return `400 Bad Request`. |

#### Query Parameters

| Field | Type | Required | Default | Notes |
|---|---|---:|---:|---|
| `page` | integer | No | `1` | 1-based page number. Minimum `1`. |
| `page_size` | integer | No | `20` | Minimum `1`. Maximum `50`. |

Invalid or non-integer pagination values return `400 Bad Request`.

A page beyond the final page returns `200 OK` with an empty `listings` array and accurate `total_items` and `total_pages`.

#### Browse-Eligible Listing Rule

The event detail listing collection must reuse the exact same browse-eligible listing definition as `GET /api/events`:

- `listings.status` is `ACTIVE`.
- The related `events.starts_at` is in the future at request time.
- `listings.currency` is `VND`.
- The listing is currently available for purchase under the listing status rules in `docs/flows/LISTING_STATUS_FLOW.md`.

Do not add a `PLATFORM_TRANSFER`-only filter. Return listings for every transfer method that satisfies the shared browse-eligibility rule so event browse counts, lowest-price aggregates, and event-detail inventory stay consistent.

The current seller form creates only `PLATFORM_TRANSFER` listings, but this public contract must not silently define a different browse-eligible set from `GET /api/events`.

#### Ordering

Listings are ordered deterministically:

1. `asking_price_minor ASC`
2. `created_at ASC`
3. listing `id ASC`

`created_at` is used only for ordering and is not exposed in the response.

#### Response Body

```json
{
  "event": {
    "id": "22222222-2222-2222-2222-222222222222",
    "name": "Example Concert",
    "starts_at": "2026-08-15T12:30:00Z",
    "venue": "Example Arena",
    "city": "Ho Chi Minh City",
    "image_url": null
  },
  "listings": [
    {
      "id": "33333333-3333-3333-3333-333333333333",
      "ticket_type": "General Admission",
      "seat_info": "Section 101, Row B, Seat 12",
      "event_platform": "Ticketmaster",
      "asking_price_minor": 1250000,
      "currency": "VND",
      "transfer_method": "PLATFORM_TRANSFER"
    }
  ],
  "pagination": {
    "page": 1,
    "page_size": 20,
    "total_items": 3,
    "total_pages": 1
  }
}
```

#### Response Fields

| Field | Type | Notes |
|---|---|---|
| `event` | object | Public event summary. |
| `event.id` | UUID string | Event identifier. |
| `event.name` | string | Event display name. |
| `event.starts_at` | ISO-8601 datetime | Event start timestamp. |
| `event.venue` | string | Venue name. |
| `event.city` | string | Event city/location. |
| `event.image_url` | string or null | Always present. `null` means the frontend should render a safe placeholder. MVP currently returns `null`. |
| `listings` | array | Browse-eligible listing summaries for the requested page. Empty when the event has no currently browse-eligible listings or the requested page is beyond the final page. |
| `listings[].id` | UUID string | Listing identifier. |
| `listings[].ticket_type` | string | Public ticket type label. |
| `listings[].seat_info` | string | Public seat/location description. |
| `listings[].event_platform` | string | Listing-level event platform or ticket provider. |
| `listings[].asking_price_minor` | integer | Asking price in whole minor units for the listing currency. For MVP `VND`, this is whole dong. |
| `listings[].currency` | string | Always `VND` for browse MVP. |
| `listings[].transfer_method` | string | Listing transfer method. |
| `pagination.page` | integer | Current 1-based page. |
| `pagination.page_size` | integer | Applied page size after defaulting and validation. |
| `pagination.total_items` | integer | Total number of browse-eligible listings for this event before pagination. |
| `pagination.total_pages` | integer | Total number of pages for the applied page size. |

Do not repeat `lowest_price_minor` or `available_listing_count` inside the `event` object. `pagination.total_items` represents the available listing count, and the first ordered listing provides the lowest displayed price.

#### Event And Error Behavior

- Upcoming existing event with browse-eligible listings returns `200 OK`.
- Upcoming existing event with no browse-eligible listings returns `200 OK` with `listings: []` and zero pagination totals.
- Valid UUID for a missing event returns `404 Not Found`.
- Existing event whose `starts_at` is no longer in the future returns `404 Not Found`.
- Malformed event UUID path values return `400 Bad Request`.
- Unexpected server failures use the standard API `5xx` behavior.

The public detail flow intentionally treats no-longer-upcoming events as not found for MVP.

#### Sensitive Data Exclusions

The public event-detail response must not include:

- Seller ID or seller identity.
- Seller contact information.
- Ownership data.
- `public_notes`.
- `quantity`, because MVP quantity is fixed at one.
- `is_transferable_confirmed`.
- Internal listing status.
- Creation and update timestamps.
- QR codes.
- Barcodes.
- Ticket files.
- Private transfer links.
- Platform credentials.
- Other sensitive ticket payload data.

#### Availability Guarantee

The response is a current marketplace snapshot, not a reservation guarantee.

Future reservation or checkout logic must independently revalidate listing status, event eligibility, currency, transfer method rules, and availability because inventory may change after this response is loaded.
