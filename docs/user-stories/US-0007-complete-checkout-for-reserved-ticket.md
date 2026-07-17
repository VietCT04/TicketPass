# US-0007: Complete Checkout for a Reserved Ticket

## User Story

As an authenticated buyer, I want to pay for a ticket I have actively reserved so that I can complete the purchase before the reservation expires.

## Context

TicketPass now lets a buyer browse events, compare currently available listings, and place a server-controlled 10-minute hold on one eligible listing. The reservation prevents competing buyers from taking the listing temporarily, but it does not create an order, collect payment, complete a sale, transfer the ticket, reveal ticket data, or release funds to the seller.

This story introduces the smallest checkout and payment-completion boundary after reservation. TicketPass will create one provider-neutral order for the active reservation, redirect the buyer to a hosted payment provider, accept payment completion only through a trusted server-to-server confirmation, and atomically move the order to `PAID` and the listing to `SOLD`.

TicketPass may collect payment in this story, but it must not describe that behavior as regulated escrow. Seller payout, escrow release, ticket delivery, ticket reveal, refunds, chargebacks, and disputes remain separate future stories.

## Acceptance Criteria

- [ ] Only an authenticated buyer who owns an active, unexpired reservation can start or recover checkout for that reservation.
- [ ] A seller cannot check out their own listing.
- [ ] The backend derives the buyer, seller, reservation, listing, asking price, and currency; the browser cannot choose or override those values.
- [ ] Checkout creates or returns exactly one order for the reservation and repeated attempts do not create duplicate orders or uncontrolled duplicate payment sessions.
- [ ] The order stores server-derived snapshots for buyer, seller, listing, reservation, amount, currency, status, expiry, and timestamps; operational payment-session records store provider references separately.
- [ ] Initial order statuses are `PAYMENT_PENDING`, `PAID`, `PAYMENT_FAILED`, `CANCELLED`, and `EXPIRED`.
- [ ] TicketPass keeps its core order and lifecycle rules provider-neutral; issue `#67` uses an in-application mock hosted provider, while production-provider selection is deferred to a later user story.
- [ ] The hosted payment session expires no later than the reservation `expires_at` and checkout never extends or renews the reservation.
- [ ] A browser success redirect, cancellation redirect, query parameter, or client-side state cannot mark an order paid.
- [x] Only a verified provider webhook or equivalent trusted server-to-server confirmation may complete payment.
- [x] Payment confirmation revalidates the order, reservation, listing, amount, currency, provider references, and current states server-side.
- [x] Successful payment atomically transitions the order from `PAYMENT_PENDING` to `PAID` and the listing from `RESERVED` to `SOLD`.
- [x] Duplicate or concurrently delivered provider events cannot create duplicate financial effects or sell the same listing twice.
- [x] Reservation expiration never reactivates a listing after it has become `SOLD`.
- [x] Abandoned, cancelled, failed, and expired unpaid checkout attempts reach controlled order states without leaving eligible inventory permanently stuck in `RESERVED`.
- [ ] Unexpected successful provider confirmation after local expiry follows an explicitly documented safe policy rather than silently overselling inventory or ignoring paid funds.
- [ ] The buyer can use a protected `/checkout/{orderId}` route to recover the current server-authoritative order state after refresh, navigation, or return from the payment provider.
- [ ] Frontend checkout state is not used as the source of truth and is not persisted in `localStorage` or `sessionStorage` as a replacement for server recovery.
- [x] Checkout and order responses exclude provider secrets, raw payment credentials, seller contact details, session data, private transfer information, and sensitive ticket payload data.
- [ ] The UI clearly distinguishes reservation, pending payment, paid order, and future ticket-delivery states and never implies that payment immediately reveals or transfers the ticket.
- [ ] Relevant API, database, security, listing-status, concern, flow, and continuity documentation is updated by the focused implementation issues.

## Out of Scope

- Seller payout or regulated escrow funding/release.
- Ticket upload, storage, transfer, delivery, or reveal.
- Seller contact exchange.
- Refund execution, chargebacks, disputes, or admin payment operations.
- Buyer-initiated reservation extension or renewal.
- Multi-currency, partial payment, installments, discounts, taxes, marketplace fees, invoices, or receipts.
- Account order history or a general purchases dashboard.
- Embedded handling of raw card or bank credentials by TicketPass.
- Supporting multiple payment providers simultaneously.
- Broad fraud detection, identity verification, KYC, AML, or marketplace compliance implementation.

## Risks

- Hosted payment and provider webhook timing may not align exactly with the short reservation window.
- A successful provider event may arrive after local reservation or order expiry and requires an explicit safe operational policy.
- Provider retries, duplicate webhooks, application concurrency, and network failures can create duplicate effects unless idempotency is database-backed.
- A provider outage after order creation can leave a pending order without a usable checkout session unless retry behavior is defined carefully.
- Browser return pages may appear successful before the verified webhook has been processed.
- Payment collection may create refund, chargeback, consumer-protection, tax, payout, and compliance obligations that are not solved by this story.
- Calling ordinary payment collection “escrow” without a compliant escrow design would misrepresent the product and its legal protections.
- Paid order completion still does not prove that the seller can transfer a valid ticket; delivery, reveal, evidence, refunds, and disputes remain required future work.

## Follow-up Issues

- GitHub Issue `#65`: Define checkout order and payment contract - https://github.com/VietCT04/TicketPass/issues/65
- GitHub Issue `#66`: Implement checkout order persistence - https://github.com/VietCT04/TicketPass/issues/66
- GitHub Issue `#67`: Integrate hosted payment checkout sessions - https://github.com/VietCT04/TicketPass/issues/67
- GitHub Issue `#68`: Process verified payment webhooks and complete sale - https://github.com/VietCT04/TicketPass/issues/68
- GitHub Issue `#69`: Handle checkout expiration and payment failures - https://github.com/VietCT04/TicketPass/issues/69
- GitHub Issue `#70`: Harden checkout payment security and operations - https://github.com/VietCT04/TicketPass/issues/70
- GitHub Issue `#71`: Build protected buyer checkout and recovery page - https://github.com/VietCT04/TicketPass/issues/71

## Implementation Order

1. Approve and complete the provider-neutral checkout, order, API, state, expiry, and payment-confirmation contract in `#65`.
2. Implement durable one-order-per-reservation persistence in `#66`.
3. Implement authenticated idempotent checkout-session creation with the approved mock hosted provider in `#67`; defer production-provider selection to a later user story.
4. Implement verified, replay-safe provider webhook handling and atomic `PAID` plus `SOLD` completion in `#68`.
5. Implement checkout expiration, cancellation, failure, reservation coordination, and late-confirmation handling in `#69`.
6. Complete the focused checkout security and operational review in `#70` before exposing the browser checkout flow.
7. Build the protected `/checkout/{orderId}` initiation and recovery experience in `#71`.

Ticket transfer, reveal, seller payout, escrow release, refunds, chargebacks, and disputes remain separate future user stories after checkout completion.
