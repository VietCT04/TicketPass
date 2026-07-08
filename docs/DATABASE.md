# Database

## Seller Listing Contract

Issue `#2` defines the initial database contract for seller-created transferable ticket listings. This is a documentation contract only; migrations are handled by implementation issues.

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
| `public_notes` | text | Buyer-visible notes. Must not contain sensitive ticket payload data. |
| `created_at` | timestamp | Creation time. |
| `updated_at` | timestamp | Last update time. |

## Listing Statuses

| Status | Meaning |
|---|---|
| `DRAFT` | Listing exists but is not visible or purchasable. |
| `ACTIVE` | Listing is visible and available for purchase. |
| `RESERVED` | Listing is temporarily held for a purchase attempt. |
| `SOLD` | Listing has completed sale flow and must not be sold again. |
| `CANCELLED` | Seller or admin cancelled the listing. |
| `EXPIRED` | Listing is unavailable because the event or listing window expired. |

Status transition enforcement belongs to issue `#4`.

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
- Sensitive ticket payload data must not be stored in `listings.public_notes` or other public listing metadata.
