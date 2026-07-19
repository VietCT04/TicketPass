# US-0024: Admin Reviews Event Requests

## User Story

As an administrator, I want to review pending missing-event requests and safely approve or reject them so that sellers can list tickets only against controlled catalogue events.

## Context

Sellers can submit a durable missing-event request, but a request is not a catalogue event and cannot currently become listing-eligible. TicketPass needs a private, auditable review lifecycle that prevents duplicate event creation, protects requester privacy, and returns an approved seller to the normal listing flow without weakening server-side validation.

## Acceptance Criteria

- [ ] Persisted `USER` and `ADMIN` roles are server-authoritative; there is no public role-assignment endpoint.
- [ ] Only an administrator can view the review queue and resolve a request.
- [ ] A request moves only from `PENDING` to immutable `APPROVED` or `REJECTED` terminal outcomes.
- [ ] Approval either creates one exact canonical event or links one existing future event.
- [ ] Exact pending siblings may resolve to the same event without exposing other requesters.
- [ ] Exact catalogue identity is database-enforced using normalized name, start time, venue, and city.
- [ ] Concurrent review cannot create conflicting decisions or duplicate exact catalogue events.
- [ ] Every direct decision and created event has a minimal transactional audit record.
- [ ] Sellers can see only their own request outcomes and continue from an approved request to the resolved event in `/sell`.
- [ ] Sensitive request, seller, reviewer, ticket, payment, session, and audit data remains private.
- [ ] Relevant API, database, security, seller-flow, admin-flow, concern, and continuity documentation is updated.

## Out of Scope

- Admin user-management UI or self-service role assignment.
- Notifications, external event-provider imports, URL fetching, scraping, or automated verification.
- Fuzzy matching, machine-learning deduplication, or automatic approval of nonexact requests.
- Editing or deleting existing catalogue events.
- Reopening terminal decisions, reviewer chat, or seller/admin contact exchange.
- Ticket, listing, reservation, checkout, payment, escrow, transfer, settlement, dispute, or reveal changes.

## Risks

- Exact normalization avoids obvious duplicates but cannot prove that similar performances are the same event.
- Admin bootstrap requires an explicit operational process outside the application.
- User-submitted URLs and text remain untrusted even when visible to an administrator.
- Event-local timezone preservation is still not modeled; `starts_at` remains an absolute instant.

## Follow-up Issues

- GitHub Issue `#145`: Define admin event-request review contract - https://github.com/VietCT04/TicketPass/issues/145
- GitHub Issues `#146` through `#149`: Implement role, event-identity, admin-review, and resolution slices.
- GitHub Issues `#150` and `#151`: Implement seller request tracking and approved-request continuation.
