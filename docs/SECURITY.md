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

## CSRF And Credentialed CORS

Issue `#56` protects unsafe cookie-authenticated `/api/**` requests with server-side exact-origin validation. The protected methods are `POST`, `PUT`, `PATCH`, and `DELETE`; safe methods including `GET`, `HEAD`, and `OPTIONS` are not rejected. The rule applies to existing and future unsafe API endpoints without endpoint-specific bypasses.

When a request carries `ticketpass_session`, the server accepts an `Origin` header only when its normalized scheme, host, and effective port exactly match `ticketpass.security.allowed-origins`. If `Origin` is absent, the server may use only the normalized origin component of `Referer`. Missing, malformed, wildcard, partial-host, or untrusted origins return `403` JSON with `{"error":"Invalid request origin"}`. The configured allowlist is not returned to clients.

For MVP, frontend and API deployments must be same-site, for example `app.ticketpass.com` and `api.ticketpass.com`. The development default is `http://localhost:3000`; production must explicitly configure its real frontend origin or origins. Blank or invalid configured origins fail application startup. A deployment needing `SameSite=None` is outside this MVP and needs a broader review.

Spring Security's default CSRF mechanism remains disabled because TicketPass uses a focused origin filter rather than JavaScript-readable tokens. No CSRF token endpoint, token cookie, browser storage token, or custom header treated as standalone proof is used. Existing frontend calls continue to use `credentials: "include"` without accessing the opaque session token.

Credentialed CORS uses the same normalized trusted-origin source as the CSRF origin filter. It allows only configured exact origins, permits required API methods and `Accept`/`Content-Type` request headers, and allows `OPTIONS` preflight. The exact mock webhook path is excluded and emits no credentialed CORS headers. `SameSite=Lax` remains required for the session cookie, but is not considered the only CSRF control. HTTPS or non-loopback trusted origins require `ticketpass.auth.cookie-secure=true` at startup.

## Current User And Ownership

- Backend services must derive the current user from authenticated session state.
- Session authentication must place an immutable authentication-specific principal in Spring Security's `SecurityContext`.
- The principal must contain only the minimum required user information and must not be a JPA entity.
- Raw session tokens must never be stored in `SecurityContext` or written to logs.
- Clients must not be allowed to choose or override user IDs for ownership.
- Seller, buyer, order, payment, ticket, reveal, and dispute ownership checks must use the authenticated user.
- Frontend route protection is a usability layer only; backend authorization remains required.

## Frontend Protected Routes

Issue `#13` protects the existing `/sell` frontend route with a small reusable client-side auth guard.

The guard uses `GET /api/me` through the existing `getCurrentUser()` helper as the source of truth for session validity. It must not treat the presence of the opaque session cookie as proof of authentication, and it must not store session tokens or current-user data in `localStorage` or `sessionStorage`.

The `/sell` seller listing form must not mount until `GET /api/me` confirms an authenticated user.

Signed-out access to `/sell` redirects with `router.replace(...)` to:

```text
/login?next=/sell
```

Login and signup completion may redirect only to approved return destinations. For MVP, allowed `next` values are:

```text
/sell
/events/{valid UUID}
/events/{valid UUID}?page={positive integer}
/checkout/{valid UUID}
```

The checkout form accepts no query string on its return path. Missing, malformed, external, protocol-based, or unsupported `next` values must fall back to `/`. Login/signup links may preserve only these safe targets.

Unexpected session-check failures must show a generic retryable error state, not backend response bodies or technical session details.

Frontend route protection does not replace backend authorization. The seller listing API and event autocomplete API must continue enforcing authenticated access server-side, and submission-time `401` handling should preserve seller-entered form data rather than automatically redirecting away.

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

The MVP frontend seller form warns sellers not to enter QR codes, barcodes, ticket links, platform credentials, or other usable ticket payload data in public notes. This warning is a usability safeguard only; backend content classification is not implemented for MVP public notes.

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

Issue `#26` implements the endpoint as an explicitly public route in API security configuration. The permissive fallback must not be the only indicator that public event browse is allowed.

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

## Public Event Detail Security

Issue `#45` implements the public `GET /api/events/{eventId}` detail endpoint. The endpoint exposes one public event summary and browse-eligible listing summaries only.

Event eligibility, listing availability, pagination, and ordering must be enforced server-side. Frontend state must not be trusted to decide whether an event is upcoming, whether a listing is active, whether a listing is available, or whether a listing can proceed to a future reservation or checkout flow.

The detail endpoint reuses the same browse-eligible listing rule as `GET /api/events`: active, future, VND listings that are currently available for purchase under the listing status rules. The endpoint must not add a transfer-method-specific filter unless the shared browse-eligible rule is explicitly changed in `docs/API.md`.

The public browse route and public detail route must be explicitly permitted in API security configuration. The authenticated `/api/events/autocomplete` route must remain separately matched and protected so the public detail matcher does not make seller autocomplete public.

Public event detail responses may include:

- Event name, start timestamp, venue, city, and nullable image URL.
- Listing ID.
- Ticket type.
- Seat information.
- Listing-level event platform or ticket provider.
- Asking price and `VND` currency.
- Transfer method.

Public event detail responses must not include:

- Seller ID or seller identity.
- Seller contact information.
- Ownership data.
- `public_notes`.
- `quantity`.
- `is_transferable_confirmed`.
- Internal listing status.
- Creation and update timestamps.
- QR codes.
- Barcodes.
- Ticket files.
- Private transfer links.
- Platform credentials.
- Other sensitive ticket payload data.

The public detail response is only a current marketplace snapshot. Any future reservation, checkout, payment, escrow, or ticket-reveal flow must independently revalidate event eligibility, listing status, currency, availability, and ownership server-side before continuing.

## Buyer Listing Reservation Security

Issue `#53` defines the authenticated `POST /api/listings/{listingId}/reservations` contract. Issue `#54` implements atomic reservation creation; issue `#55` implements expiration and reactivation.

- Reservation creation requires Spring Security authentication before controller execution.
- The controller must receive the immutable `AuthenticatedUser` principal with `@AuthenticationPrincipal`; the buyer ID is derived exclusively from that principal.
- The request has no body and must not accept buyer, seller, owner, duration, expiry, listing status, payment, or reservation-status fields from the client.
- The server must revalidate listing existence, `ACTIVE` status, `VND` currency, future event, seller ownership, and competing reservation state at reservation time. A loaded event-detail page is not proof that a listing remains available.
- A seller must not reserve their own listing. Self-reservation uses the same general `409 Listing is no longer available` response as other unavailable states so the endpoint does not disclose availability or reservation ownership details.
- The `ACTIVE -> RESERVED` transition and creation of the associated reservation are atomic under a pessimistic listing lock, with a database partial unique index as the final active-reservation integrity guard. Concurrent buyer requests must not both succeed.
- The 10-minute expiry must be generated from the injected application clock. Clients cannot choose, extend, or renew it.
- Same-buyer active retries return the existing reservation without creating a duplicate or extending expiry.
- An expired reservation stops owning the listing at `expires_at <= now`, using the injected application clock. Request-time reconciliation and the bounded scheduled cleanup both lock the listing first, re-read the active reservation, and are safe when multiple application instances process the same candidate.
- Expiration changes a listing back to `ACTIVE` only while it remains `RESERVED`; it never restores a later sale-related or terminal status. Scheduler failure logs contain only reservation and listing identifiers, never buyer identity, ticket payloads, credentials, cookies, or session data.

Reservation responses may include only reservation ID, listing ID, reservation status, and expiry. They must not expose seller identity or contact details, buyer email, ticket payload data, `public_notes`, private transfer links, credentials, session tokens, or cookies.

Issue `#53` does not add audit events. Add reservation audit coverage only after audit retention and access policy are defined.

Issue `#57` exposes the browser action only after issue `#56` origin protection. The client submits the cookie-authenticated request directly with `credentials: "include"`; it sends no body, CSRF token, custom CSRF header, session token, or browser-stored security data. The server-side exact-origin rule remains the CSRF control.

The browser must not call `/api/me` before reservation submission or trust the public event-detail snapshot for eligibility. On `401`, it may redirect only through a safe login or signup return target: `/sell`, `/events/{valid UUID}`, or `/events/{valid UUID}?page={positive integer}`. No external URL, protocol-relative URL, fragment, unsupported path, unexpected parameter, malformed UUID, or invalid page may be preserved.

The frontend validates enough of a `200` or `201` response to avoid rendering a false hold, keeps reservation data only in memory, and refreshes server-rendered inventory after `404`, `409`, or local countdown expiry. A hard refresh may hide an active hold, but it must never change the backend reservation, ownership, or expiry state. The card never exposes seller contact, buyer identity, ticket payloads, session data, or private transfer information. Checkout, payment, transfer, and ticket reveal remain unavailable.

## Buyer Checkout And Order Security

Issue `#65` defines the checkout security contract for `US-0007`. Issues `#66` and `#67` implement persistence and mock hosted checkout. Issue `#68` implements signed mock webhook delivery, database-backed replay protection, and verified sale completion. Issue `#69` implements guarded unpaid-terminal reconciliation and buyer-only order reads. Issue `#70` hardens the current mock lifecycle, and issue `#71` implements the protected browser checkout and recovery UI.

- `SecurityConfig` is fail-closed: only the documented auth, public event, mock hosted checkout/action, mock webhook, and actuator health routes are public; all other routes require an explicit security decision and otherwise receive `denyAll`.
- `POST /api/reservations/{reservationId}/checkout` and `GET /api/orders/{orderId}` require authenticated server-side session validation and are explicitly protected by `SecurityConfig`. Buyer ownership is derived from `AuthenticatedUser`, never from browser input. Order reads return the same controlled `404` for absent and non-owned orders. Both responses send `Cache-Control: no-store`.
- The browser permits checkout login return only to the exact `/checkout/{canonical UUID}` route. It rejects arbitrary checkout paths, query strings, fragments, protocol-based values, and external targets. Provider-return parameters are removed before login and never preserved in `next`.
- Checkout-start and order-read client calls use cookie credentials and `cache: no-store`, but browser state is never proof of ownership, order status, payment, or expiry. The frontend validates the safe response shape before rendering or following a hosted URL; it accepts a payment URL only for `PAYMENT_PENDING`, only with its matching server deadline, and never stores, logs, displays, or routes that URL.
- `provider_return=success`, `failed`, and `cancelled` are presentation hints only. A hint cannot mark an order paid, enable ticket access, alter inventory, or override the protected order read. Any polling after a hint is visible-page-only and bounded to 15 attempts; ordinary pending checkout routes do not poll.
- Checkout start has no body. The server derives and revalidates the buyer, seller, reservation, listing, amount, currency, order status, and expiry with the injected `Clock`.
- The server returns `404` for both absent and non-owned reservations or orders, preventing another buyer from discovering their existence. Self-checkout and unavailable checkout use the general `409 Checkout is no longer available` response.
- A public event page, reservation response, browser countdown, frontend state, provider return URL, query parameter, or hosted-session creation is never authority to start checkout, release inventory, mark an order paid, or sell a listing.
- `order.expires_at` must equal the reservation expiry. Hosted payment sessions must expire or become server-side unusable no later than that timestamp. Browser storage must not retain payment URLs or use frontend state as checkout recovery.
- Safe checkout and order responses may include only the order identifiers, status, amount, currency, timestamps, and minimum event/ticket summary. They must exclude buyer or seller identity/contact details, provider identifiers/secrets/payloads, `public_notes`, session data, private transfer data, and all ticket payload data.
- Exactly one order belongs to each reservation and at most one `CREATING` or `PENDING` provider session may be usable for that order at a time. Checkout uses listing-first pessimistic locking and database uniqueness as the final concurrency guards.
- Only a verified provider webhook or equivalent trusted server-to-server confirmation may atomically move an order from `PAYMENT_PENDING` to `PAID` and its listing from `RESERVED` to `SOLD`, after server-side revalidation of the full order, reservation, listing, monetary, and provider state.
- Provider failures, cancellation, expiry, and late confirmations must not reactivate a `SOLD` listing, sell unrelated inventory, or silently discard paid funds. Reconciliation locks listing, reservation, order, then payment session and checks the receipt ledger for `REQUIRES_ACTION` before releasing inventory. Browser cancellation returns are non-authoritative; operational late-payment handling and refunds remain future work.
- The public `/mock-provider/**` routes do not require a TicketPass login cookie and display only amount, currency, server expiry, and mock-provider state. They accept canonical UUID session identifiers only, send `Cache-Control: no-store` plus focused CSP, `nosniff`, and no-referrer headers, and never receive or expose TicketPass session data, seller/buyer identity, event or seat details, ticket data, private transfer details, payment credentials, or provider secrets. Their success, failure, and cancellation redirects are presentation hints only.
- `POST /api/payments/webhooks/mock` is the sole public payment API exception in `SecurityConfig`. It ignores browser session authentication and cookie-origin validation, emits no credentialed CORS headers, requires non-empty raw bodies no larger than `16 KiB`, and verifies required header shapes, the configured HMAC-SHA256 signature, and five-minute timestamp window before JSON parsing. Signature comparison is constant time and controlled `400` or `401` responses reveal no verification detail. It never logs request bodies, signatures, secrets, payment values, identities, or ticket data.
- Payment configuration is immutable startup-bound configuration. The current supported provider is explicitly enabled `mock`; frontend, provider, and webhook URLs are validated; the mock webhook URL must be exact; non-loopback URLs require HTTPS; mock non-loopback use needs an explicit development override; and `MOCK_PAYMENT_WEBHOOK_SECRET` must be externally configured, non-placeholder, and at least 32 characters. Secrets are never logged.
- The receiver ledger uses an atomic database uniqueness insert for `(MOCK, provider_event_id)`. Duplicates return `200` without repeating marketplace effects. The outbox uses the single validated webhook URL, bounded timeouts, redirects disabled, a short database claim/lease, and bounded retries; only `5xx` and network failures retry.
- Provider replay/deduplication records and provider references are restricted operational payment records. They are not automatically added to generic `audit_events`; current lifecycle records provide the mock operational record, while broader immutable financial audit design remains deferred.
- The implemented core `orders` row stores only reservation, listing, buyer and seller UUID snapshots, amount, currency, status, expiry, and timestamps. `payment_sessions` stores only operational provider session metadata; both exclude provider secrets, raw provider payloads, seller contact information, public notes, private transfer data, QR codes, barcodes, ticket files, credentials, and other sensitive ticket payloads.

## Seller Event Autocomplete Security

The authenticated `GET /api/events/autocomplete` endpoint exposes seller-safe existing event summaries only.

Authentication is required because autocomplete is part of the seller listing flow. Frontend checks are only usability aids; the backend must enforce authentication, query validation, result limits, and event eligibility.

Autocomplete may include future events that have no active listings so a seller can create the first listing for an existing event. The server must still enforce that returned events are existing TicketPass records, selectable by server-issued `event_id`, and eligible under the documented MVP rules.

Seller event autocomplete responses must not include:

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

Autocomplete must use strict query limits to reduce unnecessary backend load and enumeration risk: trimmed `q` length from `3` to `100` characters, maximum `10` results, and no pagination for MVP.

## Event-Linked Listing Creation Security

Issue `#32` defines listing creation as an event-linked operation, and issue `#34` implements the backend enforcement. Sellers submit `event_id` for an existing TicketPass event and ticket-specific listing data; they do not submit event identity fields that would create or redefine an event record.

The backend must independently validate the selected event. Frontend autocomplete selection is not trusted as authorization or eligibility proof.

The issue `#6` frontend seller form reuses the issue `#35` event selector and submits the selected server-issued `event_id`. It does not collect event name, venue, city, start time, quantity, or currency. It displays `VND` as fixed MVP currency and submits the entered whole-dong amount as `asking_price_minor`.

Server-side listing creation must enforce:

- The request is authenticated.
- Seller ownership is derived from `AuthenticatedUser.id()`.
- The selected `event_id` exists.
- The selected event has `starts_at` in the future at request time.
- Listing creation does not create, rename, or otherwise modify event records.
- `event_platform` is listing/ticket-specific and does not redefine event identity.
- New MVP listings are stored as `VND`; clients cannot choose currency.
- `asking_price_minor` is a positive whole-dong integer for VND.

The MVP frontend form submits `transfer_method = PLATFORM_TRANSFER` and does not expose `PDF_UPLOAD`, `QR_UPLOAD`, or `MANUAL_TRANSFER` choices. Adding upload-backed transfer methods must wait for controlled ticket upload, storage, reveal, and audit rules.

When event cancellation, hidden, public/private, or moderation fields exist, listing creation must also enforce those eligibility rules server-side.

## Audit Log Security

Issue `#5` records the first backend audit event for seller listing creation.

When an authenticated seller creates a listing, the backend must insert a `LISTING_CREATED` audit event in the same transaction as the listing insert. If the audit insert fails, listing creation must roll back so a successful listing cannot exist without its required audit record.

The issue `#5` audit record may contain only:

- Authenticated actor user ID.
- Fixed action value `LISTING_CREATED`.
- Fixed entity type value `LISTING`.
- Created listing ID.
- Server-generated timestamp.

Audit records must not include:

- Request bodies.
- `public_notes`.
- Seat information.
- Ticket type.
- Asking price.
- QR codes.
- Barcodes.
- Ticket files.
- Private transfer links.
- Platform credentials.
- Passwords.
- Session tokens.
- Cookies.
- Email addresses.

Audit records are append-only for normal product workflows. TicketPass does not expose audit editing, deletion, viewer, or search APIs in issue `#5`.

## Transferability Confirmation

Sellers must confirm the ticket is transferable before listing creation. This confirmation reduces accidental invalid listings but does not prove transferability. Platform-specific verification remains an unresolved concern.
