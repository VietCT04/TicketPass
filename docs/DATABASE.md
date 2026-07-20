# Database

## Authentication Contract

Issue `#9` defines the initial database contract for user authentication. This is a documentation contract only; migrations are handled by implementation issues.

Issue `#10` implements this contract with Flyway migration `apps/api/src/main/resources/db/migration/V1__create_auth_tables.sql`.

## Auth Tables

### `users`

Stores TicketPass user accounts.

Issues `#141` and `#142` define and implement the display-name-only profile replacement without a migration: `display_name` is already `VARCHAR(120) NOT NULL`, and `updated_at` already exists. A normalized no-op must not change `updated_at`; an effective update changes only `display_name` and `updated_at`. No username, alias, profile, moderation, field-history, or per-field timestamp table is introduced.

| Column | Type | Notes |
|---|---|---|
| `id` | UUID/string id | Primary key. |
| `email` | string | Unique normalized email address. |
| `password_hash` | string | Strong password hash. Never store plaintext passwords. |
| `display_name` | string | User-facing account name. |
| `account_role` | enum/string | Server-authoritative account role: `USER` or `ADMIN`. Existing and new accounts default to `USER`; no public role-assignment API exists. |
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
- `users.account_role` must be constrained to `USER` or `ADMIN` and defaults to `USER`. Administrative access is derived from the persisted role after session authentication, never from a request field, browser state, or client claim.
- The profile-update service must reload and pessimistically lock the authenticated `users` row before mutation so concurrent account changes can serialize without lost updates.

## Seller Listing Contract

Issue `#2` defines the initial database contract for seller-created transferable ticket listings.

Issue `#3` implements the initial seller listing persistence contract with Flyway migration `apps/api/src/main/resources/db/migration/V2__create_listing_tables.sql`.

The user-facing seller listing flow is documented in `docs/flows/SELLER_LISTING_FLOW.md`.

Issue `#5` adds the first audit table and records seller listing creation with Flyway migration `apps/api/src/main/resources/db/migration/V3__create_audit_events.sql`. Issue `#113` defines a second future audit action, `LISTING_CANCELLED`, without requiring a schema change; backend implementation remains issue `#114`.

## Tables

### `events`

Stores normalized event information shared by listings.

| Column | Type | Notes |
|---|---|---|
| `id` | UUID/string id | Primary key. |
| `name` | string | Event display name. |
| `normalized_name` | string | Server-derived exact identity value: trimmed, internal whitespace collapsed to ASCII spaces, and lowercased locale-independently. |
| `venue` | string | Venue name. |
| `normalized_venue` | string | Server-derived exact identity value using the same normalization. |
| `city` | string | Event city. |
| `normalized_city` | string | Server-derived exact identity value using the same normalization. |
| `starts_at` | timestamp with timezone | Event start date and time. |
| `created_at` | timestamp | Creation time. |
| `updated_at` | timestamp | Last update time. For the future seller cancellation contract, the first `ACTIVE -> CANCELLED` transition time. |

### `event_requests`

Issue `#78` implements this table through Flyway migration `V9__create_event_requests.sql`. An event request is untrusted review metadata, not an event catalogue record.

| Column | Type | Notes |
|---|---|---|
| `id` | UUID | Primary key. |
| `requester_user_id` | UUID | References `users.id` with a non-cascading foreign key. Derived from the authenticated requester. |
| `event_name` | string | Trimmed display value. |
| `normalized_event_name` | string | Server-derived exact duplicate-detection value. |
| `starts_at` | timestamp with timezone | Absolute future event start time parsed from a timestamp with explicit offset. |
| `venue` | string | Trimmed display value. |
| `normalized_venue` | string | Server-derived exact duplicate-detection value. |
| `city` | string | Trimmed display value. |
| `normalized_city` | string | Server-derived exact duplicate-detection value. |
| `official_url` | string nullable | Untrusted review metadata only. |
| `status` | enum/string | `PENDING`, `APPROVED`, or `REJECTED`. Terminal decisions are immutable. |
| `resolution_type` | enum/string nullable | `CREATED_EVENT`, `LINKED_EVENT`, or `EXACT_MATCHED` for approved rows; null while pending or rejected. |
| `resolved_event_id` | UUID nullable | Non-cascading foreign key to the selected or created `events` row for approved requests only. |
| `reviewed_by_user_id` | UUID nullable | Non-cascading foreign key to the reviewing admin for direct decisions; null for automatic exact-sibling outcomes. |
| `reviewed_at` | timestamp with timezone nullable | Captured server decision time. |
| `rejection_reason` | string nullable | Bounded controlled reason visible only to the owning seller after rejection. |
| `resolution_message` | string nullable | Optional seller-facing message, maximum 500 characters; never audit content. |
| `created_at` | timestamp with timezone | Server-generated creation time. |
| `updated_at` | timestamp with timezone | Server-generated last-update time. |

An event request is not itself an event catalogue record and must not store ticket, listing, payment, reservation, contact, session, or credential data. An approved request can reference an independently maintained existing or newly created event through `resolved_event_id`; it remains distinct from that event.

The requester-scoped partial unique index `uq_event_requests_pending_duplicate` prevents concurrent duplicate `PENDING` requests with the same normalized event name, start time, venue, and city. It excludes `official_url`, permits requests from different users, and does not provide cross-user or fuzzy deduplication.

Issue `#145` defines a separate unique index across `(normalized_name, starts_at, normalized_venue, normalized_city)` on `events` as the final exact-event duplicate guard. Before its migration is applied, existing exact duplicate event rows must be reported and resolved deliberately; they must not be silently merged with events, listings, or requests. Similar but nonexact names, venues, cities, or times are not equal under this rule.

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

### `listing_reservations`

Defines the reservation ownership record for the buyer listing reservation contract in issue `#53`. Issue `#54` implements the table through Flyway migration `V4__create_listing_reservations.sql`; issue `#55` implements database-backed expiration reconciliation and listing reactivation.

| Column | Type | Notes |
|---|---|---|
| `id` | UUID/string id | Primary key. |
| `listing_id` | UUID/string id | References `listings.id`. |
| `buyer_user_id` | UUID/string id | References the authenticated buyer/user. Must be derived server-side. |
| `status` | enum/string | Reservation lifecycle status. |
| `expires_at` | timestamp with timezone | Server-generated hold expiry. A newly created MVP reservation expires exactly 10 minutes after creation. |
| `created_at` | timestamp with timezone | Server-generated creation time. |
| `updated_at` | timestamp with timezone | Server-generated last-update time. |

### Reservation Statuses

| Status | Meaning |
|---|---|
| `ACTIVE` | Reservation currently owns the listing hold. |
| `EXPIRED` | Reservation reached `expires_at` and no longer owns the listing. |
| `CANCELLED` | Reservation was cancelled by a future authorized flow. This issue does not define who may cancel or how. |

Reservation records are separate from the listing so buyer ownership is not stored directly on `listings`. `V4__create_listing_reservations.sql` enforces at most one `ACTIVE` reservation row for a listing with a PostgreSQL partial unique index, while allowing historical `EXPIRED` and `CANCELLED` rows. It also indexes `buyer_user_id` and `(status, expires_at)` for ownership lookup and bounded expiration scans. Expiration is `expires_at <= now`, where `now` comes from the injected application clock. Expiring a row under the existing pessimistic listing lock flushes `ACTIVE -> EXPIRED` before a replacement active reservation can be inserted, preserving the partial unique-index invariant.

### `orders`

Issue `#65` defines the provider-neutral checkout contract. Issue `#66` implements its core persistence with Flyway migration `V5__create_orders.sql`, `OrderEntity`, `OrderStatus`, and `OrderRepository`. No checkout endpoint, provider integration, webhook, or transition service is included.

| Column | Type | Notes |
|---|---|---|
| `id` | UUID/string id | Primary key. |
| `reservation_id` | UUID/string id | References `listing_reservations.id`; unique so one reservation has exactly one order. |
| `buyer_user_id` | UUID/string id | Authenticated buyer snapshot derived server-side. |
| `seller_user_id` | UUID/string id | Listing seller snapshot derived server-side. |
| `listing_id` | UUID/string id | References the reserved listing. |
| `amount_minor` | integer | Server-derived listing amount; VND MVP values represent whole dong. |
| `currency` | string | Server-derived listing currency; VND only for the current MVP scope. |
| `status` | enum/string | Order lifecycle status. |
| `expires_at` | timestamp with timezone | Exactly equals the associated reservation `expires_at`; never extends the hold. |
| `paid_at` | timestamp with timezone nullable | Set only by trusted payment completion. |
| `created_at` | timestamp with timezone | Server-generated creation time. |
| `updated_at` | timestamp with timezone | Server-generated last-update time. |

`V5__create_orders.sql` uses non-cascading foreign keys, so historical financial records cannot disappear through deletion of referenced marketplace records. It enforces one order per reservation with `uq_orders_reservation`, positive amounts, `VND` currency, bounded statuses, `paid_at` only for `PAID` orders, and an expiry after creation. The migration adds only `idx_orders_buyer_user_id` and `idx_orders_status_expires_at`; the reservation uniqueness constraint provides its own lookup index.

The entity maps `reservation` as a lazy one-to-one relationship and `listing` as a lazy many-to-one relationship. Buyer and seller remain UUID snapshot columns rather than JPA user relationships. Order timestamps are assigned explicitly by future services from the injected application `Clock`; the entity has no lifecycle timestamp callbacks.

Provider customer, payment, session, and event references belong in operational payment records, not in the public order API or core order row. Hosted payment URLs are short-lived redirect data and must not be stored as browser state. The core row excludes provider secrets, authorization data, browser session data, seller contact details, public notes, private transfer data, QR codes, barcodes, ticket files, and platform credentials.

### Order Statuses

| Status | Meaning |
|---|---|
| `PAYMENT_PENDING` | Order is valid and awaits trusted payment confirmation before its inherited reservation deadline. |
| `PAID` | Verified provider confirmation completed the sale atomically. Terminal. |
| `PAYMENT_FAILED` | Trusted provider failure ended payment. Terminal. |
| `CANCELLED` | Trusted cancellation flow ended payment. Terminal. |
| `EXPIRED` | The inherited payment deadline elapsed. Terminal. |

The only permitted transitions are from `PAYMENT_PENDING` to one terminal status. A failed, cancelled, or expired order cannot return to `PAYMENT_PENDING`. The database enforces the `reservation_id` uniqueness invariant. Issue `#67` implements transaction-safe create-or-return behavior by reloading the existing order when a concurrent insert reaches the unique constraint.

### `order_fulfillments`

Issue `#92` defines a separate one-to-one post-payment fulfilment record; issue `#93` implements it through `V10__create_order_fulfillments.sql`. The payment `orders.status`, ticket-transfer status, and settlement status are separate state dimensions. Before payment, the absence of a fulfilment row represents `NOT_STARTED` transfer and `NOT_FUNDED` settlement; these are response representations, not persisted fulfilment values.

| Column | Type | Notes |
|---|---|---|
| `order_id` | UUID/string id | Primary key and non-cascading foreign key to `orders.id`. |
| `transfer_status` | enum/string | Bounded ticket-transfer lifecycle value. |
| `settlement_status` | enum/string | Bounded settlement lifecycle value. |
| `transfer_deadline_at` | timestamp with timezone | Trusted `paid_at + 15 minutes`; immutable after creation. |
| `seller_confirmed_at` | timestamp with timezone nullable | Immutable first seller-transfer confirmation time. |
| `buyer_confirmed_at` | timestamp with timezone nullable | Immutable server time for the first valid buyer receipt confirmation. |
| `settlement_released_at` | timestamp with timezone nullable | Immutable server-observed time for confirmed settlement release. |
| `created_at` | timestamp with timezone | Server-generated creation time. |
| `updated_at` | timestamp with timezone | Server-generated last-update time. |

The approved transfer values are `AWAITING_SELLER_TRANSFER`, `SELLER_CONFIRMED_TRANSFER`, `BUYER_CONFIRMED_RECEIPT`, `TRANSFER_TIMED_OUT`, and `REQUIRES_REVIEW`. The approved settlement values are `FUNDS_HELD`, `RELEASED_TO_SELLER`, `REFUND_REQUIRED`, and `REVIEW_REQUIRED`. Issue `#93` writes only `AWAITING_SELLER_TRANSFER`, `SELLER_CONFIRMED_TRANSFER`, and `FUNDS_HELD`. Issue `#95` defines the later buyer-confirmed and release contract; implementation remains issue `#96`.

The migration constrains bounded persisted statuses, `updated_at >= created_at`, and `transfer_deadline_at = created_at + 15 minutes`; it also constrains the two states written by issue `#93` so awaiting transfer is held and unconfirmed, while seller-confirmed transfer is held and timestamped before its deadline. It adds an index on `(transfer_status, transfer_deadline_at, order_id)` for bounded timeout scans. The table does not duplicate buyer, seller, listing, reservation, amount, or currency fields because their order relationships remain authoritative. Trusted payment completion creates the row atomically using the same captured server instant as `orders.paid_at`. Existing `PAID` orders are backfilled from trusted non-null `orders.paid_at`; a missing value fails the migration instead of inventing a deadline. Later timeout reconciliation may process a backfilled deadline that has already passed.

For the buyer-confirmation happy path, one transaction will revalidate the authoritative listing, reservation, order, and fulfilment rows, then transition `SELLER_CONFIRMED_TRANSFER -> BUYER_CONFIRMED_RECEIPT`. It sets `buyer_confirmed_at` and `updated_at` to one captured server time while retaining `FUNDS_HELD`. A repeated coherent request preserves both values. Provider-confirmed release is the only path that may then move `FUNDS_HELD -> RELEASED_TO_SELLER` and set `settlement_released_at`; it must not be inferred from the buyer action itself.

### `settlement_release_operations`

Issue `#95` defines one private release operation per order; issue `#96` implements it through `V11__create_settlement_release_operations.sql`. It makes external settlement execution durable without duplicating marketplace facts already held by `orders` and `order_fulfillments`.

| Column | Type | Notes |
|---|---|---|
| `order_id` | UUID/string id | Primary key and non-cascading foreign key to `orders.id`; one operation per order. |
| `provider` | string | Bounded provider-neutral settlement adapter identifier. |
| `idempotency_key` | string | Unique stable key, derived once from the order and reused across all retries. |
| `status` | enum/string | `PENDING`, `PROCESSING`, `RETRYABLE_FAILURE`, `SUCCEEDED`, or `REQUIRES_REVIEW`. |
| `provider_operation_id` | string nullable | Opaque provider reference, never exposed in marketplace responses. |
| `attempt_count` | integer | Bounded execution-attempt count. |
| `next_attempt_at` | timestamp with timezone nullable | Earliest future retry time. |
| `processing_lease_until` | timestamp with timezone nullable | Bounded lease for crash recovery and multi-instance exclusion. |
| `last_error_code` | string nullable | Bounded internal outcome category, never raw provider data. |
| `created_at` | timestamp with timezone | Server-generated creation time. |
| `updated_at` | timestamp with timezone | Server-generated update time. |
| `completed_at` | timestamp with timezone nullable | Immutable server-observed completion time after confirmed release. |

The stable key may use `settlement-release:<order-id>`. It cannot change for HTTP retries, worker retries, restarts, or uncertain provider responses. A unique constraint on the key and the one-to-one order relation prevent duplicate operations. The operation contains no buyer or seller IDs, amount, currency, listing, reservation, ticket data, or provider payload; the locked paid order remains authoritative for all release inputs.

External provider calls occur outside marketplace locks. A short transaction records buyer confirmation and creates or recovers the operation; a bounded claim commits `PROCESSING`; the provider is called with the stable key; then a later transaction locks and reloads the marketplace and operation rows before applying a confirmed result. Unknown or timeout results keep settlement held and recover through provider lookup or retry. A stale claim becomes recoverable after its lease. Confirmed success applies exactly once and sets both `settlement_released_at` and `completed_at` from one captured server time. Contradictory or permanent outcomes move the operation to `REQUIRES_REVIEW` without claiming release or refund.

### `payment_sessions`

Issue `#67` adds `V6__create_payment_sessions.sql` with provider-neutral operational session rows. Each row references one order without cascade deletion and stores provider name, opaque provider session id, bounded status, inherited expiry, and explicit application-clock timestamps. `provider_session_id` is unique; the partial unique index permits only one usable `CREATING` or `PENDING` session per order while retaining terminal history. The initial provider set contains `MOCK` only and session statuses are `CREATING`, `PENDING`, `PAID`, `FAILED`, `CANCELLED`, and `EXPIRED`.

`payment_sessions.expires_at` equals the order and reservation expiry. The entity has no lifecycle timestamp callbacks. It contains no payment credentials, provider payloads, browser URLs, cookies, buyer or seller contact data, ticket payloads, or TicketPass order transition state.

### `mock_provider_sessions` And `mock_payment_events`

The same migration persists an isolated mock-provider session record with amount, currency, inherited expiry, and provider-only state (`PENDING`, `PAID`, `FAILED`, `CANCELLED`, or `EXPIRED`). Issue `#68` adds V7 delivery fields to the mock event outbox: bounded attempts, next/last attempt timestamps, and `PENDING`, `DELIVERED`, or `DEAD_LETTER` status. Issue `#70` uses the existing `next_attempt_at` as a short delivery lease before performing network I/O outside the claim transaction; it adds no status, column, or migration. No response body, exception text, signature, secret, or payload is stored.

`payment_webhook_receipts` is the TicketPass receiver ledger. It records only provider, provider event/session identifiers, event type, final processing status, and timestamps, with a unique `(provider, provider_event_id)` constraint for authoritative deduplication. It excludes raw webhook data, secrets, payment credentials, checkout URLs, identities, and ticket data. `V8__add_checkout_reconciliation_indexes.sql` adds bounded reconciliation indexes on `(processing_status, received_at, id)` and `(provider_session_id, processing_status)`. Verified success may atomically update the operational payment session, order, and listing. Verified failure/cancellation starts as `DEFERRED`; reconciliation either reaches the matching terminal checkout state and marks the receipt `PROCESSED`, or retains uncertainty as `REQUIRES_ACTION`.

### `audit_events`

Stores immutable audit records for security-sensitive business actions.

Issue `#5` emits `LISTING_CREATED` records when an authenticated seller creates a listing. Issue `#113` defines a future `LISTING_CANCELLED` record for the first seller-owned `ACTIVE -> CANCELLED` transition; issue `#114` will implement it. Issue `#95` defines future `BUYER_RECEIPT_CONFIRMED` and `SETTLEMENT_RELEASED` records for effective buyer-confirmation and release transitions; issue `#96` will implement them. Issue `#145` defines future `EVENT_CREATED`, `EVENT_REQUEST_APPROVED`, and `EVENT_REQUEST_REJECTED` records for administrative review; implementation remains follow-up work.

| Column | Type | Notes |
|---|---|---|
| `id` | UUID/string id | Primary key. |
| `actor_user_id` | UUID/string id | Authenticated user who performed the action. References `users.id` and must not cascade delete. |
| `action` | string | Bounded application action value. Existing migration has no database action check constraint; `LISTING_CREATED`, future `LISTING_CANCELLED`, `BUYER_RECEIPT_CONFIRMED`, `SETTLEMENT_RELEASED`, `EVENT_CREATED`, `EVENT_REQUEST_APPROVED`, and `EVENT_REQUEST_REJECTED` are application-defined values. |
| `entity_type` | string | Bounded entity type value. Issue `#5` supports `LISTING`; event review adds `EVENT` and `EVENT_REQUEST`. |
| `entity_id` | UUID/string id | Identifier of the affected entity. Generic value; no foreign key is declared to `listings`. |
| `created_at` | timestamp with timezone | Server-generated audit timestamp. |

Administrative event-review audit rows contain only actor ID, action, entity type, entity ID, and the captured server timestamp. Submitted metadata, URLs, reviewer text, seller-facing messages, normalized values, and request bodies must never be stored in `audit_events`.

Buyer-confirmation audit rows use the authenticated buyer as actor and the order as entity. `BUYER_RECEIPT_CONFIRMED` is written only for the first effective local transition; `SETTLEMENT_RELEASED` is written only for the first provider-confirmed release. HTTP retries, worker retries, provider lookup, and polling write no additional audit event. These rows contain no amount, provider data, ticket data, user-entered text, or error detail.

## Listing Statuses

Detailed transition rules and duplicate-sale invariants are documented in `docs/flows/LISTING_STATUS_FLOW.md`.

| Status | Meaning |
|---|---|
| `DRAFT` | Listing exists but is not visible or purchasable. |
| `ACTIVE` | Listing is visible and available for purchase. |
| `RESERVED` | Listing is temporarily held for a purchase attempt. |
| `SOLD` | Listing has completed sale flow and must not be sold again. |
| `CANCELLED` | Terminal unavailable listing. Issue `#113` defines seller-owned `ACTIVE -> CANCELLED`; separate future rules are required for draft or admin cancellation. |
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
- Issue `#113` defines seller cancellation only as a listing-first locked `ACTIVE -> CANCELLED` transition. It never modifies historical reservations, orders, payments, provider records, or ticket payload data; an owning seller retry against `CANCELLED` is a no-write idempotent read of the existing terminal state.
- Reservation creation atomically writes an `ACTIVE` reservation record and transitions its listing from `ACTIVE` to `RESERVED` under a pessimistic listing lock.
- A reservation is valid only while its status is `ACTIVE` and `expires_at` has not been reached according to server time.
- A reservation without an order expires through the generic reservation-expiry path. A reservation with an order is owned exclusively by checkout reconciliation, preventing generic expiry from releasing inventory while payment uncertainty remains.
- Checkout reconciliation locks listing, reservation, order, then payment session. It may release only an `ACTIVE` reservation and `RESERVED` listing with consistent relationships, matching inherited expiry, and no `REQUIRES_ACTION` receipt for the provider session. A later terminal or sale-related listing status is never overwritten.
- A listing seller must not be able to own a reservation for that listing.
- A valid reservation must not be inferred from frontend state or from a prior public event-detail response.
- Each reservation may have exactly one order. The order expiry must equal the reservation expiry and must not extend, renew, or replace it.
- Only a verified provider webhook or equivalent trusted server-to-server confirmation may atomically transition `PAYMENT_PENDING -> PAID` and `RESERVED -> SOLD` after revalidating order, reservation, listing, amount, currency, provider references, and trusted payment status.
- An order read must reconcile an overdue `PAYMENT_PENDING` order using the injected application clock; stale pending status must not rely on scheduler timing.
- Provider failure, cancellation, or expiry may reactivate a listing only while it remains `RESERVED` by that checkout path. A `SOLD` listing must never be reactivated by reservation or order expiry.
- A verified late payment after terminal local expiry must not sell a listing or overwrite terminal order state; it requires durable operational handling for manual review or refund processing.
- `SOLD` listings must never become purchasable again.
- Public listing metadata must not include dedicated columns for QR codes, barcodes, ticket files, private transfer links, platform credentials, or other sensitive ticket payload data.
- MVP does not classify free-text `listings.public_notes` for sensitive content; this limitation is tracked in `docs/CONCERNS.md`.
- A missing-event request must reference its requester through a non-cascading foreign key and must start in `PENDING`.
- Event-request display values retain trimming; their normalized duplicate values trim leading/trailing whitespace, collapse internal whitespace to one ASCII space, and lowercase with locale-independent rules.
- An event-request `starts_at` must be parsed from an offset-bearing RFC 3339 timestamp and be strictly future according to one captured server `Clock` timestamp.
- A database-backed uniqueness rule must prevent concurrent duplicate `PENDING` requests for the same requester, normalized event name, `starts_at`, normalized venue, and normalized city. `official_url` is excluded. The rule must permit requests from different users and future non-pending request states without claiming cross-user or fuzzy deduplication.
- `event_requests.id` must never be accepted where an existing `events.id` is required, including listing creation.

## Audit Constraints

- Listing creation and its `LISTING_CREATED` audit record must be written in the same transaction.
- The future seller `ACTIVE -> CANCELLED` transition and its first `LISTING_CANCELLED` audit record must be written in the same transaction with the same captured application-clock timestamp.
- A listing creation or cancellation failure must not leave an audit record without its listing transition.
- An audit insertion failure must roll back the associated listing mutation.
- `audit_events.created_at` must be generated server-side with the injected application clock.
- Application code may insert audit records, but existing audit records must not be updated or deleted as part of normal product workflows.
- Audit records must not contain request bodies, seller contact data, public notes, seat information, ticket type, asking price, QR codes, barcodes, ticket files, private transfer links, platform credentials, passwords, session tokens, cookies, or email addresses.
- Issue `#5` adds only `idx_audit_events_entity` on `(entity_type, entity_id)`. Actor, action, and timestamp indexes should wait for a concrete audit search or viewer use case.

Issue `#53` does not add reservation audit events. Issue `#65` also does not add generic payment audit events: provider replay/deduplication records are operational payment records, and broader payment-audit coverage remains deferred to issue `#70` after retention, access, and compliance requirements are defined.

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
