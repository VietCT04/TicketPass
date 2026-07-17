# Buyer Checkout Flow

## Source

User Story: `docs/user-stories/US-0007-complete-checkout-for-reserved-ticket.md`  
GitHub Issue: `#71` - https://github.com/VietCT04/TicketPass/issues/71

## Goal

Allow an authenticated buyer to explicitly start or recover checkout for an order while keeping payment confirmation, inventory changes, ticket delivery, and seller payout under server-side boundaries.

## Reservation To Checkout

After a card has a server-confirmed active reservation, it displays the existing hold and countdown plus `Continue to checkout`. This is an explicit buyer action. It sends:

```text
POST /api/reservations/{reservationId}/checkout
credentials: include
cache: no-store
no request body
```

The card disables the action while it is pending. The browser validates the safe response and requires its `reservation_id` to match the submitted reservation. A valid `PAYMENT_PENDING` response with a valid ephemeral `payment_url` redirects with `window.location.assign`. The URL is never displayed, logged, placed in an application route or query string, or persisted in browser storage. A response without a usable payment URL navigates to `/checkout/{orderId}`.

The backend remains authoritative for buyer ownership, active reservation status, expiry, listing state, amount, currency, order creation, and provider session idempotency. A browser countdown or reservation response does not authorize checkout.

## Protected Checkout Recovery

`/checkout/{orderId}` accepts only a UUID-shaped order ID. A malformed route shows a generic unavailable state without a backend request. The route uses the existing `RequireAuth` mechanism and permits only the exact safe login return path `/checkout/{canonical UUID}`. It drops provider-return and every other query parameter before login.

After a hard refresh, direct navigation, browser back navigation, login return, or provider return, the route calls:

```text
GET /api/orders/{orderId}
credentials: include
cache: no-store
```

This response is the only authority for order status. The UI keeps order and checkout data in component memory only; it does not use `localStorage`, `sessionStorage`, IndexedDB, frontend-created cookies, route state, or prior checkout responses as recovery state.

## Provider Return And Refresh

The browser recognizes only `provider_return=success`, `failed`, or `cancelled`. Each is a temporary presentation hint, never proof of payment. A recognized hint is removed from the visible URL immediately, and the route loads the current order state from the server.

When the loaded order remains `PAYMENT_PENDING`, does not require review, and the page is visible, a recognized provider return enables a bounded refresh every two seconds for at most 15 attempts. The refresh stops for a terminal order, review-required state, hidden page, component unmount, or the attempt limit. Ordinary direct navigation does not poll. All pending states retain a manual `Refresh status` action.

The pending countdown displays the server-provided `expires_at` as presentation only. At local zero it triggers one order refresh; it never changes the displayed status to `EXPIRED`, extends the deadline, or creates a separate client deadline.

## State And Error Presentation

- `PAYMENT_PENDING`: payment is not confirmed. The buyer may explicitly continue to hosted payment only before the displayed deadline and when review is not required.
- `PAYMENT_PENDING` with `payment_review_required`: payment confirmation needs review. The UI does not start another payment attempt.
- `PAID`: payment is confirmed. Ticket delivery/reveal and seller payout are not implemented by this flow.
- `PAYMENT_FAILED`, `CANCELLED`, and `EXPIRED`: terminal states with no same-order payment retry.
- `401`: redirect through the validated checkout login-return path.
- `403`: show a generic request-rejected message.
- `404`: show the same generic unavailable state for absent and non-owned checkout.
- `409` while recovering a payment session: reload the protected order state.
- `503`: provide an explicit retry without showing provider internals.
- Other network, controlled, or malformed-payload failures: show a generic retryable failure.

The UI renders only the documented safe event, ticket, amount, currency, expiry, and order-status fields. It never renders or stores buyer/seller identity, contact details, provider identifiers, payment credentials, receipts, signatures, QR/barcode data, ticket files, private transfer links, or secrets.

## Out Of Scope

- Embedded payment forms or raw payment credentials.
- Ticket transfer, ticket storage, ticket reveal, or seller payout.
- Refunds, chargebacks, disputes, invoices, order history, or payment administration.
- Frontend authority over payment, order, reservation, listing, or escrow state.
