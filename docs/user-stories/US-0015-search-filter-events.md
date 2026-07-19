# US-0015: Search And Filter Available Events

## User Story

As a buyer, I want to search and filter events with available tickets so that I can find relevant marketplace inventory efficiently.

## Context

TicketPass already has a public event-first browse API and page. They show upcoming events with eligible active listings, but do not support search or filtering. This story extends that flow while keeping visibility, aggregates, ordering, counting, and pagination server-authoritative.

## Scope

- Extend the existing public event browse endpoint.
- Support optional event text, city, lower event-time, and upper event-time filters.
- Use bounded literal case-insensitive text matching across approved event fields.
- Preserve existing listing eligibility, safe response fields, aggregates, and upcoming ordering.
- Apply filters before counting and pagination.
- Add accessible controls to the existing browse page.
- Use URL query parameters as the frontend source of truth.
- Preserve filters across pagination and reset to page 1 when filters change.
- Keep search state out of browser-persistent storage.
- Handle normal, filtered, empty, invalid, loading, and error states.

## Out of Scope

- Fuzzy search, typo correction, relevance ranking, autocomplete, recommendations, or personalization.
- Category, performer, map, distance, image, price, ticket, seat, or quantity filters.
- New search, city, event, listing, saved-search, or analytics tables.
- Saved searches, alerts, history, or notifications.
- Changes to visibility, listing eligibility, reservation, checkout, payment, transfer, or settlement behavior.
- Broad homepage redesign.

## Acceptance Criteria

- [ ] Buyers can search available public events by bounded text.
- [ ] Buyers can filter by city and an approved event-time window.
- [ ] Unfiltered requests preserve current browse behavior.
- [ ] Filters are validated and applied server-side before counting and pagination.
- [ ] Existing visibility, aggregate, response, and ordering rules remain unchanged.
- [ ] Active filters are represented by shareable URL parameters.
- [ ] Pagination preserves filters and filter changes reset to page 1.
- [ ] Empty filtered results are distinguished from a generally empty marketplace.
- [ ] Search state is not stored in browser persistence.
- [ ] Relevant documentation is updated.

## Focused Issues

- `#109` — define the public event search and filter contract.
- `#110` — implement database-backed search and filters.
- `#111` — build public search and filter controls.

## Delivery Order

1. Complete `#109`.
2. Complete `#110`.
3. Complete `#111`.

## Concerns

- Substring matching may become expensive as the catalogue grows; optimization should be evidence-driven.
- Event timestamps are instants and the current model has no IANA event timezone, so calendar controls require an explicit offset.
- City values are free text and may be inconsistent.
- Upcoming ordering remains deterministic but is not relevance-ranked.
- Unicode matching may vary by database collation.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, or other verification commands. Complete application implementation first; verification will be handled later as a separate final phase.
