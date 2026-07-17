# US-0008: Request a Missing Event

## User Story

As an authenticated seller, I want to request an event that is missing from TicketPass so that the catalogue team can review it and I can list my ticket after the event is approved and added.

## Context

TicketPass requires sellers to search for and select an existing event before creating a listing. This protects event consistency and prevents sellers from creating duplicate or untrusted event records through the listing form.

When autocomplete cannot find the event, the seller currently has no structured next step. This story introduces a catalogue-request path without weakening the existing rule that listings must reference an approved event from the `events` table.

Submitting a request does not create an event, approve an event, or create a listing. Admin review, catalogue insertion, and seller notification remain separate future work.

## Acceptance Criteria

- [ ] Only an authenticated user can submit a missing-event request.
- [ ] The request captures event name, start date and time, venue, city, and an optional official event URL.
- [ ] Request ownership and timestamps are derived server-side.
- [ ] Submitted text and URLs are treated as untrusted metadata and validated using bounded server-side rules.
- [ ] A new request is stored in an initial pending state.
- [ ] An obvious duplicate pending request returns the existing request rather than creating another row.
- [ ] Duplicate handling is database-backed and does not claim broad fuzzy matching across distinct performances.
- [ ] Submitting a request does not create or modify an event in the catalogue.
- [ ] A pending request cannot be used as an `event_id` or bypass the existing seller listing rules.
- [ ] The seller receives a clear confirmation that the request is pending review.
- [ ] The seller listing form remains blocked until an actual existing event is selected.
- [ ] Responses and frontend rendering exclude requester contact details, session data, credentials, ticket payload data, and backend internals.
- [ ] Request state is not persisted in `localStorage` or `sessionStorage`.
- [ ] Relevant API, database, security, seller-flow, concern, and continuity documentation is updated by the focused implementation issues.

## Out of Scope

- Admin review, approval, rejection, or moderation UI.
- Automatically creating an event from a seller request.
- External event-provider search or catalogue import.
- Seller request history or status-tracking pages.
- Email or in-app notifications when a request is reviewed.
- Automatically returning the seller to listing creation after approval.
- Broad fuzzy event matching or event-catalogue deduplication redesign.
- Event cancellation, rescheduling, image sourcing, or global timezone redesign.
- Changes to checkout, orders, reservations, payments, escrow, ticket transfer, or reveal.

## Risks

- Exact duplicate rules may merge distinct performances if date, venue, and normalized event identity are not considered together.
- Weak duplicate rules may allow many equivalent pending requests and create moderation noise.
- User-provided URLs or text may be misleading or unsafe if later rendered without escaping and validation.
- The project does not yet have an admin role or catalogue-review workflow, so pending requests cannot be completed until a later user story defines that capability.
- Event timezone preservation remains unresolved and must be addressed explicitly in the contract rather than guessed by the frontend.

## Follow-up Issues

- GitHub Issue `#77`: Define missing-event request contract - https://github.com/VietCT04/TicketPass/issues/77
- GitHub Issue `#78`: Implement missing-event request backend - https://github.com/VietCT04/TicketPass/issues/78
- GitHub Issue `#79`: Add seller missing-event request flow - https://github.com/VietCT04/TicketPass/issues/79

## Implementation Order

1. Define and approve the API, database, status, validation, privacy, and duplicate contract in `#77`.
2. Implement authenticated persistence and duplicate-safe request creation in `#78`.
3. Add the seller autocomplete fallback and pending confirmation in `#79`.

Issues `#77` through `#79` are independent of checkout reconciliation issue `#69` and may be developed concurrently by another agent.