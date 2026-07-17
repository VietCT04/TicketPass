# Buyer Reservation Flow

## Source

User Story: `docs/user-stories/US-0006-reserve-available-ticket-listing.md`  
GitHub Issues: `#53` - https://github.com/VietCT04/TicketPass/issues/53, `#54` - https://github.com/VietCT04/TicketPass/issues/54, `#55` - https://github.com/VietCT04/TicketPass/issues/55, `#56` - https://github.com/VietCT04/TicketPass/issues/56, `#57` - https://github.com/VietCT04/TicketPass/issues/57

## Goal

Allow an authenticated buyer to place a server-controlled 10-minute hold on one available listing from the public event-detail page before a later checkout flow.

## Browser Interaction

`/events/{eventId}` remains server-rendered and supplies the public listing snapshot and pagination. Each listing card owns only its in-memory reservation request, result, countdown, and error state.

The buyer selects `Reserve for 10 minutes`. The browser sends no request body, CSRF token, custom CSRF header, session token, or browser-stored security state:

```text
POST /api/listings/{listingId}/reservations
credentials: include
```

The backend remains authoritative for authentication and reservation eligibility. The browser does not first request `/api/me` and does not impose a buyer-wide reservation limit.

## Authentication Return

When reservation returns `401 Unauthorized`, the browser redirects to `/login` with only one of these safe return values:

```text
/events/{valid UUID}
/events/{valid UUID}?page={positive integer}
```

`/sell` remains an allowed return target. External URLs, protocol-relative URLs, fragments, malformed UUIDs, unexpected paths or query parameters, and non-positive page values fall back to `/`. Login or signup returns to the approved event route, but the buyer must select Reserve again; authentication never auto-submits a reservation.

## Hold Outcome

Both `201 Created` and an idempotent same-buyer `200 OK` are successful only when the response contains an active reservation with valid reservation and matching listing IDs plus a valid server-provided `expires_at` timestamp.

The selected card then displays `Held for you`, the full reservation ID, the exact `expires_at` value, and an `MM:SS` countdown. The countdown recalculates against the absolute server timestamp once per second and never extends the hold locally. At `00:00`, it stops and refreshes the current server-rendered event route while preserving its page parameter.

The hold panel states that checkout, payment, ownership transfer, ticket transfer, and reveal have not occurred. It never displays seller contact, buyer identity, ticket payload data, or private transfer information.

After a valid active hold, the card also shows `Continue to checkout`. This remains a separate explicit buyer action; creating or recovering a reservation never starts payment automatically. The action uses the authenticated no-body checkout-start request, validates the returned safe order, and either follows a validated ephemeral hosted-payment URL or opens the protected `/checkout/{orderId}` recovery route. It does not store a payment URL, order, or reservation in browser storage. The protected checkout behavior is documented in `docs/flows/BUYER_CHECKOUT_FLOW.md`.

## Error And Refresh Handling

- `401`: redirect through the safe login-return flow.
- `403`: show `Request rejected. Reload the page and try again.`
- Controlled `400`: show the safe backend error message when present.
- `404` or `409`: show `This ticket is no longer available. Refreshing availability...`, remove the usable Reserve action, and refresh the current event route.
- Network failures or malformed success responses: show `Could not reserve this ticket` with a Retry action after the current request ends.

## MVP Limitation

Reservation state remains only in React memory. A hard refresh can hide a still-active server reservation until the buyer submits the same listing again and receives the backend's idempotent response. A future current-reservation or checkout story should provide recovery without weakening server-side ownership or expiry checks.

## Out Of Scope

- Checkout, payment, escrow, or `RESERVED -> SOLD`.
- Seller contact exchange.
- Ticket upload, transfer, storage, or reveal.
- Buyer cancellation, release, extension, or renewal.
- Reservation history, a current-reservation read endpoint, or an account reservations page.
- A dedicated confirmation route.
- Browser storage for reservation or security state.
