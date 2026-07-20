# Post-Payment Ticket Transfer Flow

## Source

User Story: `docs/user-stories/US-0011-seller-transfers-paid-ticket.md`  
GitHub Issues: `#92` - https://github.com/VietCT04/TicketPass/issues/92, `#93` - https://github.com/VietCT04/TicketPass/issues/93, `#95` - https://github.com/VietCT04/TicketPass/issues/95

## Goal

Define the server-authoritative boundary between trusted payment completion, seller transfer confirmation, buyer receipt confirmation, and eventual settlement. Issue `#96` implements the buyer confirmation and local provider-neutral release-operation path; production payout and operational recovery remain deferred.

## Separate State Dimensions

Payment, transfer, and settlement never share one overloaded order status:

```text
payment:    PAYMENT_PENDING -> PAID
transfer:   AWAITING_SELLER_TRANSFER -> SELLER_CONFIRMED_TRANSFER -> BUYER_CONFIRMED_RECEIPT
settlement: FUNDS_HELD -> RELEASED_TO_SELLER
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

The bodyless backend endpoint is:

```text
POST /api/seller/orders/{orderId}/transfer-confirmation
```

The server derives the seller from the authenticated session and locks in this order:

```text
listing -> reservation -> order -> fulfilment
```

It confirms only when the seller owns the paid, sold order; the fulfilment deadline matches `paid_at + 15 minutes`; settlement remains `FUNDS_HELD`; transfer is eligible; and captured server time is strictly before the deadline. The first eligible request changes `AWAITING_SELLER_TRANSFER` to `SELLER_CONFIRMED_TRANSFER` and records an immutable `seller_confirmed_at`. A coherent repeat returns the existing progress without writing a new timestamp, including after the deadline.

Seller confirmation is a claim that the transfer was performed. It is not proof that the buyer received a valid ticket, does not reveal ticket data, and cannot release or pay settlement.

## Buyer Receipt Confirmation And Release

The bodyless buyer endpoint defined by issue `#95` is:

```text
POST /api/orders/{orderId}/receipt-confirmation
```

It derives buyer ownership from the authenticated server session. A first confirmation requires a paid and sold order, `SELLER_CONFIRMED_TRANSFER`, `FUNDS_HELD`, a non-null immutable seller confirmation, no buyer confirmation, and no timeout, review, refund, dispute, or conflicting release state. Seller confirmation alone cannot authorize release. Once a seller has validly confirmed before the seller deadline, this flow adds no separate buyer deadline; browser time never decides eligibility.

The transaction lock order extends the seller flow:

```text
listing -> reservation -> order -> fulfilment -> release operation
```

The first valid local transition is:

```text
transfer_status:  SELLER_CONFIRMED_TRANSFER -> BUYER_CONFIRMED_RECEIPT
buyer_confirmed_at: one captured server timestamp
updated_at:         same captured server timestamp
settlement_status:  remains FUNDS_HELD
```

Buyer confirmation commits before any external settlement call. It authorizes a release attempt but does not mean external release completed. A coherent repeat preserves the buyer-confirmed timestamp and recovers the same operation without a second transition.

One durable private operation per order holds a stable key such as `settlement-release:<order-id>`. It makes a bounded `PROCESSING` claim in a short transaction, calls the provider outside marketplace locks, then reloads and locks the authoritative rows before applying the result. The provider receives order-derived amount and currency, not client input. A timeout or unknown result leaves settlement `FUNDS_HELD` and recovers with the same key through lookup or retry. A stale claim is recoverable after its lease. No release failure automatically refunds the buyer.

Only provider-confirmed success makes the settlement transition:

```text
settlement_status:      FUNDS_HELD -> RELEASED_TO_SELLER
settlement_released_at: one server-observed completion time
operation.status:       SUCCEEDED
operation.completed_at: same completion time
```

The effective first buyer confirmation writes `BUYER_RECEIPT_CONFIRMED`; confirmed release writes `SETTLEMENT_RELEASED`. Retries, lookup, and polling write no audit event. Development mock settlement may verify idempotent application control flow only and is explicit local-only infrastructure, not production financial capability.

## Deferred Outcomes

At or after the deadline, awaiting seller confirmation is ineligible and the endpoint returns a controlled conflict without writing a timeout state. Timeout transition and reconciliation are deferred to issues `#98` and `#99`. Issue `#95` defines buyer confirmation and settlement release, while persistence and endpoint work remain `#96` and browser work remains `#97`. Timed-out, review-required, unpaid, failed, cancelled, expired, refunded, missing, and inconsistent orders must not be reactivated by seller or buyer confirmation. Contradictory or permanent release outcomes enter review-required state; refunds, disputes, chargebacks, and administrative resolution remain separate approved work.

## Privacy And Errors

Seller-safe progress includes only approved identifiers, payment/transfer/settlement statuses and timestamps, plus safe event and ticket summaries. It excludes buyer identity/contact details, provider data, payment URLs, ticket files, QR codes, barcodes, credentials, and private transfer links.

Buyer-safe progress adds only `buyer_confirmed_at`, `settlement_released_at`, `buyer_action`, and `status_refresh_required` to the same safe order metadata. Pending release presents `BUYER_CONFIRMED_RECEIPT`, `FUNDS_HELD`, `buyer_action = NONE`, and `status_refresh_required = true`; it never exposes internal operation state, attempts, provider IDs, errors, or review details.

- `400`: malformed order ID.
- `401`: authentication required.
- `404`: missing or non-owned order, with the same response for both.
- `409`: ineligible lifecycle state or elapsed deadline.

Responses use `Cache-Control: no-store`. Buyer confirmation also returns `202` for durable acceptance with pending release, and `200` after confirmed release or a coherent completed retry. Operational logs may contain only order ID and bounded lifecycle outcome categories; they exclude identities, amounts, request bodies, provider payloads, cookies, sessions, ticket data, and errors.
