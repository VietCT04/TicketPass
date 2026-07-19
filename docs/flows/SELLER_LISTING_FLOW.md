# Seller Listing Flow

## Source

User Story: `docs/user-stories/US-0001-list-transferable-ticket.md`
GitHub Issue: `#7` - https://github.com/VietCT04/TicketPass/issues/7

## Goal

Allow an authenticated seller to create a public listing for one transferable ticket without exposing sensitive ticket data before payment, escrow, and ticket reveal rules allow it.

## Scope

This flow documents seller listing creation only.

In scope:

- Seller authentication requirement.
- Event and listing metadata collection.
- Seller transferability confirmation.
- Public listing metadata boundaries.
- Server-side validation expectations.
- Relationship to duplicate-sale prevention, audit logging, and later reveal flow.

Out of scope:

- Buyer checkout.
- Payment capture.
- Escrow funding or release.
- Ticket upload, storage, QR reveal, or barcode reveal.
- Dispute handling.
- Admin moderation.

## Actors

- Seller: authenticated user creating a listing.
- Backend API: validates listing data, derives seller ownership, and persists the listing.
- Buyer: future viewer of public listing metadata only.

## Preconditions

- Seller is authenticated.
- Seller has a ticket they believe is transferable.
- Seller can search for and select an existing TicketPass event.
- Seller can provide ticket metadata, asking price, transfer method, event platform, and transferability confirmation.

## Listing Creation Steps

1. Seller opens the `/sell` create listing flow.
2. Frontend requires the seller to search for and select an existing event through the authenticated event autocomplete flow.
3. The event selector searches `GET /api/events/autocomplete` only after at least three trimmed characters and uses a short debounce.
4. If autocomplete returns no results, the seller may open the inline missing-event request panel. It collects only event name, local date/time, an editable bounded UTC offset, venue, city, and an optional official HTTPS URL, then submits `POST /api/event-requests` with credentials and no-store caching.
5. A missing-event request is `PENDING` catalogue-review metadata only. Success shows safe request details and must state that no event or listing was created. It must keep `selectedEvent = null`, never fabricate an `event_id`, and never submit the listing automatically.
6. The selected existing event supplies `event_id` for listing creation. Free-text input and an event-request ID do not count as a valid event selection. The create-listing action remains disabled until a real event is selected.
7. Frontend collects listing metadata:
   - Event platform or ticket provider.
   - Seat information.
   - Ticket type.
   - Asking price as a positive whole-VND amount.
   - Public notes.
8. Frontend submits `transfer_method = PLATFORM_TRANSFER` for MVP and does not offer PDF upload, QR upload, or manual transfer choices.
9. Seller confirms the ticket is transferable.
10. Frontend submits `POST /api/listings` with `event_id` and listing-specific fields.
11. Spring Security validates the session before controller execution.
12. Backend receives the immutable `AuthenticatedUser` principal and derives `seller_id` from `AuthenticatedUser.id()`.
13. Backend validates all required fields server-side, including selected event existence and eligibility.
14. Backend associates the listing to the selected event without creating, renaming, or modifying the event record.
15. Backend creates one listing with `quantity = 1`, listing-level `event_platform`, and `currency = VND`.
16. Backend records a `LISTING_CREATED` audit event for the authenticated seller and created listing in the same transaction.
17. Backend returns public listing metadata only.

Issue `#35` adds the frontend `/sell` event selector and selected-event summary. Issue `#6` extends the same page with ticket-specific fields, listing submission, same-page success confirmation, and a create-another-listing action. Issue `#79` adds the missing-event request fallback without changing listing eligibility.

After creating listings, an authenticated seller can use the read-only `/my-listings` page from issue `#84` to review only their stored listing metadata and current listing status. The page relies on the server-authoritative `GET /api/me/listings` ownership, ordering, filtering, and pagination contract; it does not add any listing mutation, payment, payout, transfer, reveal, or buyer details.

## Cancel Own Unsold Listing

Issue `#113` defines the future documentation-only seller cancellation contract. Backend implementation remains `#114`; the confirmation control belongs to `#115`.

1. The seller requests `POST /api/listings/{listingId}/cancel` with credentials and no request body.
2. The backend derives seller identity from `AuthenticatedUser`, captures one server timestamp, locks the listing first, then checks ownership and current status.
3. An owned `ACTIVE` listing becomes terminal `CANCELLED`; its `updated_at` and one immutable `LISTING_CANCELLED` audit event use the captured timestamp in the same transaction.
4. An owned `CANCELLED` listing returns the unchanged safe response without another write or audit event.
5. `RESERVED`, `SOLD`, `EXPIRED`, and `DRAFT` return a generic conflict without changing reservations, orders, payments, provider records, or ticket data.
6. A missing or non-owned listing returns the same controlled not-found response. The response exposes only listing ID, `CANCELLED`, and `updated_at` and sends `Cache-Control: no-store`.

Reservation creation and cancellation use the same listing-first lock. If a buyer reservation wins first, seller cancellation cannot bypass the reservation or checkout flow. The seller may retry only after the authoritative flow restores the listing to `ACTIVE`.

## Public Listing Metadata

The public listing may expose:

- Selected event summary: event name, venue, city, and start date.
- Listing-level event platform or ticket provider.
- Seat information.
- Ticket type.
- Asking price and `VND` currency.
- Transfer method.
- Seller-provided public notes.

The public listing contract must not define dedicated fields for:

- Raw QR codes.
- Barcodes.
- Ticket images.
- Ticket PDFs.
- Private transfer links.
- Platform credentials.
- Any private payload that would let a buyer use the ticket before controlled reveal.

MVP does not classify free-text public notes for sensitive content. This limitation is tracked in `docs/CONCERNS.md`.

## Server-Side Rules

- Seller identity must be derived from the authenticated `AuthenticatedUser` principal.
- Client-provided seller or ownership fields must be rejected.
- Listing services must not parse cookies or resolve raw session tokens.
- `event_id` must reference an existing TicketPass event.
- Event identity fields such as event name, venue, city, or start time must not be accepted from the listing creation request.
- Backend validation must not trust frontend autocomplete selection as proof that the event exists or remains eligible.
- The selected event must have `starts_at` in the future at request time.
- Listing creation must not create, rename, or otherwise modify event records.
- Event-level cancellation, hidden, public/private, or moderation checks must be added once supported by schema and product rules.
- `event_platform` is listing/ticket-specific and does not redefine event identity.
- `quantity` is fixed to `1` for MVP.
- `is_transferable_confirmed` must be `true`.
- `asking_price_minor` must be greater than zero.
- New MVP listings must use `currency = VND`; the client must not choose currency.
- For VND, `asking_price_minor` represents whole dong.
- Required listing fields must be non-empty after trimming.
- The public listing contract must not include dedicated sensitive ticket payload fields.
- MVP does not classify free-text public notes for sensitive content.

## Status And Duplicate Sale

Newly created listings start from the listing status contract in `docs/DATABASE.md`.

Duplicate-sale prevention is required for the product, but detailed status transition enforcement belongs to GitHub Issue `#4`.

The listing flow must not create a path where a `RESERVED`, `SOLD`, `CANCELLED`, or `EXPIRED` listing can be treated as available.

## Audit Expectations

Listing creation produces an auditable record showing who created the listing, what listing was created, and when it happened.

GitHub Issue `#5` implements this as a minimal `LISTING_CREATED` audit event.

Issue `#113` defines `LISTING_CANCELLED` for the first future seller-owned cancellation only. It records the seller actor ID, listing entity type and ID, and captured server timestamp; it contains no seller notes, buyer, reservation, order, payment, provider, request, credential, or ticket data.

The listing insert and audit insert must happen in the same transaction. If audit insertion fails, listing creation must roll back.

Audit records must not include public notes, seat information, ticket type, asking price, raw QR codes, barcodes, ticket PDFs, private transfer links, platform credentials, request bodies, passwords, session tokens, cookies, email addresses, or other sensitive ticket payload data.

## Security Notes

- Frontend checks are usability aids only.
- Backend validation and authorization are mandatory.
- Seller transferability confirmation is not proof that the ticket is actually transferable.
- `event_platform` is captured at the listing/ticket level because transferability rules vary by platform.
- Sellers cannot list tickets for events missing from TicketPass. They may submit a pending catalogue request, but it does not create an event or unblock listing creation.
- The MVP frontend seller form submits only `PLATFORM_TRANSFER`; PDF upload, QR upload, and manual transfer choices remain unavailable until controlled upload and reveal rules exist.
- Secure ticket upload, storage, and reveal must remain separate from public listing creation.

## Related Docs

- `docs/API.md`
- `docs/DATABASE.md`
- `docs/SECURITY.md`
- `docs/CONCERNS.md`
- `docs/user-stories/US-0001-list-transferable-ticket.md`
