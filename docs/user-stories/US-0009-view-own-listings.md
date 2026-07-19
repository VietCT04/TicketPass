# US-0009: View My Listings

## User Story

As an authenticated seller, I want to view the ticket listings I created and their current marketplace statuses so that I can understand what is active, reserved, sold, cancelled, expired, or otherwise pending.

## Context

TicketPass currently allows an authenticated seller to create a listing, but there is no protected seller-owned view after creation. Sellers need a basic read-only account workflow before later product work can safely introduce listing cancellation, editing, relisting, seller analytics, payout, or ticket-transfer operations.

This story exposes server-authoritative listing state only. It does not add any listing mutation or reveal buyer, reservation, order, payment, or private ticket payload information.

## Acceptance Criteria

- [x] Only an authenticated user can access the own-listings API and page.
- [x] Seller ownership is derived from the authenticated session and is never accepted from the client.
- [x] A seller can retrieve only listings they own.
- [x] Results use 1-based pagination and deterministic newest-first ordering.
- [x] The seller can optionally filter by one exact current listing status.
- [x] All current listing lifecycle statuses can be represented without misleading payment, delivery, or payout claims.
- [x] Each result includes approved seller-owned listing metadata and safe event metadata.
- [x] Responses exclude buyer identity, reservation ownership, order/payment records, provider data, session data, credentials, and QR/barcode/PDF/private transfer payloads.
- [x] Empty results return a normal empty page rather than an error.
- [x] The frontend renders loading, empty, unauthenticated, validation, and unexpected-error states safely.
- [x] Seller-entered text is treated as untrusted when rendered.
- [x] Listing data is not persisted in `localStorage` or `sessionStorage`.
- [x] Relevant API, security, seller-flow, concern, and continuity documentation is updated by the focused issues.

## Out of Scope

- Editing, cancelling, deleting, renewing, duplicating, or relisting a listing.
- Buyer, reservation-owner, order, payment, payout, transfer, reveal, refund, or dispute details.
- Seller analytics, exports, bulk actions, or broad account-dashboard redesign.
- New listing statuses or lifecycle transitions.
- Public seller profiles or exposing seller identity to buyers.

## Risks

- A seller-owned response can accidentally expose private buyer, reservation, or payment relationships if entities are serialized directly instead of using an explicit safe projection.
- Status labels could mislead sellers by implying that `SOLD` also means payout, ticket transfer, or dispute completion.
- Seller-entered notes and ticket descriptions remain untrusted text and must be escaped and visually bounded.
- Future listing mutations require separate locking, reservation, checkout, and audit decisions rather than being added to this read-only story.

## Follow-up Issues

- GitHub Issue `#82`: Define seller own-listings contract - https://github.com/VietCT04/TicketPass/issues/82
- GitHub Issue `#83`: Implement seller own-listings backend - https://github.com/VietCT04/TicketPass/issues/83
- GitHub Issue `#84`: Build seller own-listings page - https://github.com/VietCT04/TicketPass/issues/84

## Implementation Order

1. Define and approve the protected API, pagination, filtering, ordering, privacy, and response contract in `#82`.
2. Implement the authenticated database-backed API in `#83`.
3. Build the protected read-only `/my-listings` page in `#84`.

Issues `#82` through `#84` are independent of checkout reconciliation `#69` and missing-event request issues `#77` through `#79`, so they may be developed concurrently.
