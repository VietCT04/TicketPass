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

## Events

Events let buyers browse upcoming event-first marketplace inventory without exposing sensitive ticket or seller information.

Issue `#25` defines this public browse contract. Backend implementation belongs to issue `#26`, and frontend implementation belongs to issue `#27`.

Issue `#31` defines the authenticated seller event autocomplete contract. Issue `#33` implements the backend endpoint, and frontend autocomplete implementation belongs to issue `#35`.

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

Issue `#26` implements this endpoint using a database-side grouped query over events and browse-eligible listings.

#### Query Parameters

| Field | Type | Required | Default | Notes |
|---|---|---:|---:|---|
| `page` | integer | No | `1` | 1-based page number. Minimum `1`. |
| `page_size` | integer | No | `20` | Minimum `1`. Maximum `50`. |

Invalid pagination values return `400 Bad Request`.

Non-integer pagination values return `400 Bad Request` with a controlled API error message.

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

#### Currency Scope

Browse events support only `VND` for MVP. Listings in another currency are not browse-eligible and must not affect event visibility, lowest price, or available listing count.

The listing creation contract also stores new MVP listings as `VND` and does not accept `currency` from the client. Issue `#34` implements this backend and database alignment.
