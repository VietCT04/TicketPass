# Post-Payment Ticket Transfer Flow

## Source

User Story: `docs/user-stories/US-0011-seller-transfers-paid-ticket.md`  
GitHub Issue: `#92` - https://github.com/VietCT04/TicketPass/issues/92

## Goal

Define the server-authoritative boundary between trusted payment completion, seller transfer confirmation, buyer receipt confirmation, and eventual settlement.

## Separate State Dimensions

Payment, transfer, and settlement never share one overloaded order status:

```text
payment:    PAYMENT_PENDING -> PAID
transfer:   AWAITING_SELLER_TRANSFER -> SELLER_CONFIRMED_TRANSFER
settlement: FUNDS_HELD
```

Before payment, no fulfilment row exists. Safe progress may represent that absence as `NOT_STARTED` transfer and `NOT_FUNDED` settlement. Payment failure, cancellation, or expiry does not create fulfilment state.

## Trusted Payment Completion

Only verified server-to-server payment success may atomically:

```text
order.status                 PAYMENT_PENDING -> PAID
listing.status               RESERVED -> SOLD
fulfilment.transfer_status   AWAITING_SELLER_TRANSFER
fulfilment.settlement_status FUNDS_HELD
transfer_deadline_at         paid_at + 15 minutes
```

One captured server `Instant` supplies `paid_at`, fulfilment creation time, and the deadline. A browser, hosted-provider redirect, provider event timestamp, seller request, or client countdown cannot choose or extend it.

## Seller Confirmation

The future bodyless endpoint is:

```text
POST /api/seller/orders/{orderId}/transfer-confirmation
```

The server derives the seller from the authenticated session and locks in this order:

```text
listing -> reservation -> order -> fulfilment
```

It confirms only when the seller owns the paid, sold order; settlement remains `FUNDS_HELD`; transfer is eligible; and captured server time is strictly before the deadline. The first eligible request changes `AWAITING_SELLER_TRANSFER` to `SELLER_CONFIRMED_TRANSFER` and records an immutable `seller_confirmed_at`. A repeat returns the existing progress without writing a new timestamp.

Seller confirmation is a claim that the transfer was performed. It is not proof that the buyer received a valid ticket, does not reveal ticket data, and cannot release or pay settlement.

## Deferred Outcomes

At or after the deadline, confirmation is ineligible. Timeout transition and reconciliation are deferred to issues `#98` and `#99`. Buyer receipt confirmation and settlement release are deferred to issues `#95` through `#97`. Timed-out, review-required, unpaid, failed, cancelled, expired, missing, and inconsistent orders must not be reactivated by seller confirmation.

## Privacy And Errors

Seller-safe progress includes only approved identifiers, payment/transfer/settlement statuses and timestamps, plus safe event and ticket summaries. It excludes buyer identity/contact details, provider data, payment URLs, ticket files, QR codes, barcodes, credentials, and private transfer links.

- `400`: malformed order ID.
- `401`: authentication required.
- `404`: missing or non-owned order, with the same response for both.
- `409`: ineligible lifecycle state or elapsed deadline.

Responses use `Cache-Control: no-store`. Operational logs may contain only order ID, result category, and transfer status.
