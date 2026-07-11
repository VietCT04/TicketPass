# Database

## Authentication Contract

Issue `#9` defines the initial database contract for user authentication. This is a documentation contract only; migrations are handled by implementation issues.

Issue `#10` implements this contract with Flyway migration `apps/api/src/main/resources/db/migration/V1__create_auth_tables.sql`.

## Auth Tables

### `users`

Stores TicketPass user accounts.

| Column | Type | Notes |
|---|---|---|
| `id` | UUID/string id | Primary key. |
| `email` | string | Unique normalized email address. |
| `password_hash` | string | Strong password hash. Never store plaintext passwords. |
| `display_name` | string | User-facing account name. |
| `created_at` | timestamp | Creation time. |
| `updated_at` | timestamp | Last update time. |

### `auth_sessions`

Stores server-side opaque login sessions.

| Column | Type | Notes |
|---|---|---|
| `id` | UUID/string id | Primary key. |
| `user_id` | UUID/string id | References `users.id`. |
| `token_hash` | string | Hash of the random session token. The raw token is never stored. |
| `expires_at` | timestamp | Session expiry time. |
| `revoked_at` | timestamp nullable | Set when the session is logged out or revoked. Rows are retained for audit and security investigation. |
| `created_at` | timestamp | Creation time. |
| `last_used_at` | timestamp | Last successful use time. |

## Auth Constraints

- `users.email` must be unique after normalization.
- `users.password_hash` must be a BCrypt hash and must never contain plaintext passwords.
- `auth_sessions.token_hash` must be unique.
- Expired or revoked sessions must not authenticate requests.
- Logout sets `auth_sessions.revoked_at` instead of deleting the session row.
- Business records must reference authenticated `users.id` values derived server-side.

## Seller Listing Contract

Issue `#2` defines the initial database contract for seller-created transferable ticket listings.

Issue `#3` implements the initial seller listing persistence contract with Flyway migration `apps/api/src/main/resources/db/migration/V2__create_listing_tables.sql`.

The user-facing seller listing flow is documented in `docs/flows/SELLER_LISTING_FLOW.md`.

## Tables

### `events`

Stores normalized event information shared by listings.

| Column | Type | Notes |
|---|---|---|
| `id` | UUID/string id | Primary key. |
| `name` | string | Event display name. |
| `venue` | string | Venue name. |
| `city` | string | Event city. |
| `starts_at` | timestamp with timezone | Event start date and time. |
| `event_platform` | string | Platform or provider where the ticket originated. Transferability rules can vary by platform. |
| `created_at` | timestamp | Creation time. |
| `updated_at` | timestamp | Last update time. |

### `listings`

Stores seller-created listings. Each listing represents exactly one ticket for MVP.

| Column | Type | Notes |
|---|---|---|
| `id` | UUID/string id | Primary key. |
| `seller_id` | UUID/string id | References the authenticated seller/user. Must be derived server-side. |
| `event_id` | UUID/string id | References `events.id`. |
| `seat_info` | string | Combined seat, section, row, or standing-zone information. |
| `ticket_type` | string | Ticket category or type. |
| `quantity` | integer | Always `1` for MVP. Multi-ticket listings are not supported. |
| `currency` | string | ISO-4217 currency code. |
| `asking_price_minor` | integer | Asking price in minor currency units. |
| `transfer_method` | enum/string | Expected transfer method. |
| `is_transferable_confirmed` | boolean | Seller confirmation that the ticket is transferable. |
| `status` | enum/string | Listing lifecycle status. |
| `public_notes` | text | Buyer-visible notes. MVP does not perform sensitive content classification on this free-text field. |
| `created_at` | timestamp | Creation time. |
| `updated_at` | timestamp | Last update time. |

## Listing Statuses

Detailed transition rules and duplicate-sale invariants are documented in `docs/flows/LISTING_STATUS_FLOW.md`.

| Status | Meaning |
|---|---|
| `DRAFT` | Listing exists but is not visible or purchasable. |
| `ACTIVE` | Listing is visible and available for purchase. |
| `RESERVED` | Listing is temporarily held for a purchase attempt. |
| `SOLD` | Listing has completed sale flow and must not be sold again. |
| `CANCELLED` | Seller or admin cancelled the listing. |
| `EXPIRED` | Listing is unavailable because the event or listing window expired. |

Status transition implementation belongs to backend/database work after issue `#4`.

## Transfer Methods

Initial values:

- `PLATFORM_TRANSFER`
- `PDF_UPLOAD`
- `QR_UPLOAD`
- `MANUAL_TRANSFER`

These values describe the expected transfer path only. Raw ticket payload storage and reveal are separate flows.

## Constraints

- One listing represents one ticket for MVP.
- `quantity` must be `1`.
- `asking_price_minor` must be greater than zero.
- `is_transferable_confirmed` must be `true` before a listing can become `ACTIVE`.
- Only `ACTIVE` listings can be reserved or purchased.
- `SOLD` listings must never become purchasable again.
- Public listing metadata must not include dedicated columns for QR codes, barcodes, ticket files, private transfer links, platform credentials, or other sensitive ticket payload data.
- MVP does not classify free-text `listings.public_notes` for sensitive content; this limitation is tracked in `docs/CONCERNS.md`.
