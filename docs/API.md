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
| `password` | string | Yes | Must satisfy the configured password policy. |
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

Authentication is required. If the session is already missing, invalid, expired, or revoked, the endpoint should still leave the client signed out.

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

Returns `401` when the session is missing, invalid, expired, or revoked.

### Session Cookie

- Cookie contains a random opaque session token.
- Only a hash of the token is stored server-side.
- Cookie is `HttpOnly`.
- Cookie is `SameSite=Lax`.
- Cookie is `Secure` in production.
- Logout revokes the session server-side.

## Listings

Listings let authenticated sellers offer one transferable ticket for resale.

### Create Listing

```http
POST /api/listings
```

Creates a seller-owned listing for one transferable ticket.

Authentication is required. The server derives `seller_id` from the authenticated user. Clients must not send or override seller ownership fields.

#### Request Body

```json
{
  "event_name": "Example Concert",
  "event_venue": "Example Arena",
  "event_city": "Singapore",
  "event_starts_at": "2026-08-15T19:30:00+08:00",
  "event_platform": "Ticketmaster",
  "seat_info": "Section 101, Row B, Seat 12",
  "ticket_type": "General Admission",
  "currency": "SGD",
  "asking_price_minor": 12500,
  "transfer_method": "PLATFORM_TRANSFER",
  "is_transferable_confirmed": true,
  "public_notes": "Mobile transfer available after purchase."
}
```

#### Request Fields

| Field | Type | Required | Notes |
|---|---|---:|---|
| `event_name` | string | Yes | Event display name. |
| `event_venue` | string | Yes | Venue name. |
| `event_city` | string | Yes | Event city. |
| `event_starts_at` | ISO-8601 datetime | Yes | Event start date and time. |
| `event_platform` | string | Yes | Platform or provider where the ticket originated. Used because transferability varies by platform. |
| `seat_info` | string | Yes | Human-readable seat, section, row, or standing-zone information. |
| `ticket_type` | string | Yes | Ticket category such as standard, VIP, student, or general admission. |
| `currency` | string | Yes | ISO-4217 currency code. |
| `asking_price_minor` | integer | Yes | Asking price in minor currency units. Must be greater than zero. |
| `transfer_method` | string | Yes | Expected transfer method. Initial value set is `PLATFORM_TRANSFER`, `PDF_UPLOAD`, `QR_UPLOAD`, or `MANUAL_TRANSFER`. |
| `is_transferable_confirmed` | boolean | Yes | Must be `true`; seller confirms the ticket is transferable. |
| `public_notes` | string | No | Buyer-visible notes. Must not contain QR codes, barcodes, private credentials, or sensitive ticket payload data. |

#### Response Body

```json
{
  "id": "lst_123",
  "seller_id": "usr_123",
  "event": {
    "id": "evt_123",
    "name": "Example Concert",
    "venue": "Example Arena",
    "city": "Singapore",
    "starts_at": "2026-08-15T19:30:00+08:00",
    "event_platform": "Ticketmaster"
  },
  "seat_info": "Section 101, Row B, Seat 12",
  "ticket_type": "General Admission",
  "quantity": 1,
  "currency": "SGD",
  "asking_price_minor": 12500,
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
- `seller_id` is always derived server-side.
- `quantity` is always `1` for MVP and is not accepted as a client-provided field.
- `is_transferable_confirmed` must be `true`.
- `asking_price_minor` must be greater than zero.
- `currency` must be a valid ISO-4217 currency code.
- Event fields and listing fields must be non-empty after trimming.
- Sensitive ticket data must not be included in public listing fields.

#### Sensitive Data Rule

The listing API does not accept or return raw QR codes, barcodes, ticket images, ticket PDFs, private transfer links, or platform credentials. Secure ticket upload, storage, and reveal belong to a separate ticket reveal flow.
