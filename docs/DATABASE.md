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

Issue `#5` adds the first audit table and records seller listing creation with Flyway migration `apps/api/src/main/resources/db/migration/V3__create_audit_events.sql`.

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
| `created_at` | timestamp | Creation time. |
| `updated_at` | timestamp | Last update time. |

### `listings`

Stores seller-created listings. Each listing represents exactly one ticket for MVP.

| Column | Type | Notes |
|---|---|---|
| `id` | UUID/string id | Primary key. |
| `seller_id` | UUID/string id | References the authenticated seller/user. Must be derived server-side. |
| `event_id` | UUID/string id | References `events.id`. |
| `event_platform` | string | Platform or provider where this ticket originated. Transferability rules can vary by platform. |
| `seat_info` | string | Combined seat, section, row, or standing-zone information. |
| `ticket_type` | string | Ticket category or type. |
| `quantity` | integer | Always `1` for MVP. Multi-ticket listings are not supported. |
| `currency` | string | Always `VND` for new MVP listings under the issue `#32` contract. |
| `asking_price_minor` | integer | Asking price. For VND MVP listings, this integer represents whole dong. |
| `transfer_method` | enum/string | Expected transfer method. |
| `is_transferable_confirmed` | boolean | Seller confirmation that the ticket is transferable. |
| `status` | enum/string | Listing lifecycle status. |
| `public_notes` | text | Buyer-visible notes. MVP does not perform sensitive content classification on this free-text field. |
| `created_at` | timestamp | Creation time. |
| `updated_at` | timestamp | Last update time. |

### `audit_events`

Stores immutable audit records for security-sensitive business actions.

Issue `#5` emits only `LISTING_CREATED` records when an authenticated seller creates a listing.

| Column | Type | Notes |
|---|---|---|
| `id` | UUID/string id | Primary key. |
| `actor_user_id` | UUID/string id | Authenticated user who performed the action. References `users.id` and must not cascade delete. |
| `action` | string | Bounded action value. Issue `#5` supports `LISTING_CREATED` only. |
| `entity_type` | string | Bounded entity type value. Issue `#5` supports `LISTING` only. |
| `entity_id` | UUID/string id | Identifier of the affected entity. Generic value; no foreign key is declared to `listings`. |
| `created_at` | timestamp with timezone | Server-generated audit timestamp. |

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
- `event_id` must reference an existing event.
- Listing creation must not create, rename, or otherwise modify the referenced event record.
- The referenced event must have `starts_at` in the future at listing creation time.
- `event_platform` belongs to the listing/ticket because the same real-world event may have tickets from multiple platforms or providers.
- New MVP listings must use `currency = VND`; clients must not choose a currency.
- `asking_price_minor` must be greater than zero.
- For VND, `asking_price_minor` represents whole dong, not cents.
- `is_transferable_confirmed` must be `true` before a listing can become `ACTIVE`.
- Only `ACTIVE` listings can be reserved or purchased.
- `SOLD` listings must never become purchasable again.
- Public listing metadata must not include dedicated columns for QR codes, barcodes, ticket files, private transfer links, platform credentials, or other sensitive ticket payload data.
- MVP does not classify free-text `listings.public_notes` for sensitive content; this limitation is tracked in `docs/CONCERNS.md`.

## Audit Constraints

- Listing creation and its `LISTING_CREATED` audit record must be written in the same transaction.
- A listing creation failure must not leave an audit record without the listing.
- An audit insertion failure must roll back listing creation.
- `audit_events.created_at` must be generated server-side with the injected application clock.
- Application code may insert audit records, but existing audit records must not be updated or deleted as part of normal product workflows.
- Audit records must not contain request bodies, seller contact data, public notes, seat information, ticket type, asking price, QR codes, barcodes, ticket files, private transfer links, platform credentials, passwords, session tokens, cookies, or email addresses.
- Issue `#5` adds only `idx_audit_events_entity` on `(entity_type, entity_id)`. Actor, action, and timestamp indexes should wait for a concrete audit search or viewer use case.

## Public Browse Events Contract

Issue `#25` defines how the current `events` and `listings` tables support event-first browse results for MVP. It is a documentation contract only; endpoint implementation belongs to issue `#26`.

The public `GET /api/events` contract derives event visibility and aggregate values from browse-eligible listings.

A listing is browse-eligible for MVP only when all of these are true:

- `listings.status` is `ACTIVE`.
- The related `events.starts_at` is in the future at request time.
- `listings.currency` is `VND`.
- The listing is currently available for purchase under the listing status rules in `docs/flows/LISTING_STATUS_FLOW.md`.

An event appears in browse results only if it has at least one browse-eligible listing.

The same browse-eligible listing set must be used to calculate:

- `lowest_price_minor`: minimum `listings.asking_price_minor` for the event.
- `available_listing_count`: count of listings currently available for purchase for the event.

For MVP, aggregate values should be server-derived at query time rather than stored on `events`. If performance later requires cached or denormalized aggregate columns, invalidation rules must account for listing status, listing price, listing currency, and event start time changes.

The current schema does not define event-level cancellation, rescheduling, hidden, public/private, or image-source fields. Event expiration can be inferred from `events.starts_at`, and listing availability can be inferred from `listings.status`, but richer event lifecycle and image rules require follow-up schema work.

The browse contract is VND-only for MVP. Issue `#32` also defines new listing creation as VND-only, so issue `#34` must align backend validation and persistence with `currency = VND`. Non-VND listings, if any exist before that implementation, are not browse-eligible and do not affect browse event visibility or aggregate values.

## Event-Linked Listing Creation Contract

Issue `#32` defines the event-linked listing creation contract. Issue `#34` implements the backend and database alignment.

Under this contract, listing creation references an existing event by `event_id`. Seller-provided event identity fields such as event name, venue, city, or start time are not accepted by `POST /api/listings`.

The selected event must exist and must have `starts_at` in the future at request time. Listing creation must not create, rename, or otherwise modify event records. Event-level cancellation, hidden, public/private, and moderation checks must be added later when the schema supports those states.

`event_platform` is listing/ticket-specific rather than shared event identity. This lets multiple listings for the same real-world event represent tickets sourced from different platforms or providers. Because migration `V2__create_listing_tables.sql` has not been run in a persistent environment, issue `#34` updates that migration directly and does not add backfill SQL.

New MVP listings are always stored as `VND`; clients do not submit `currency`. For VND, `asking_price_minor` represents whole dong.

## Event Autocomplete Contract

Issue `#31` defines how the current `events` table supports authenticated seller event autocomplete for MVP. It is a documentation contract only; endpoint implementation belongs to issue `#33`.

The seller autocomplete endpoint searches existing event records so a seller can select a server-issued `event_id` before creating a listing.

MVP autocomplete searches these existing `events` fields:

- `name`
- `venue`
- `city`

The result payload uses these existing `events` fields:

- `id`
- `name`
- `starts_at`
- `venue`
- `city`

Autocomplete eligibility uses `events.starts_at` to include only future events at request time. Unlike public browse events, autocomplete may return future events that currently have no active listings so a seller can create the first listing for an existing event.

The approved issue `#31` contract requires deterministic ordering by match quality, then `starts_at ASC`, then `id ASC`. Backend implementation should account for query performance when matching `name`, `venue`, and `city`, but this issue does not add indexes or migrations. If autocomplete performance requires new indexes, add them in the backend implementation issue with a migration and updated database documentation.

The current schema does not define event-level cancellation, rescheduling, hidden, public/private, or moderation fields. Autocomplete must not rely on unsupported event lifecycle fields until those schema and product rules are defined.
