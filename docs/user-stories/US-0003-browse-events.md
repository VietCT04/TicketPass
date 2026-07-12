# US-0003: Browse Events

## User Story

As a buyer, I want to browse events with available tickets so that I can find an event I am interested in.

## Context

Buyers normally search for an event before comparing individual ticket offers. TicketPass should provide an event-first discovery experience and show only events that currently have at least one publicly available ticket listing.

## Acceptance Criteria

- [ ] Buyer can view events that have at least one active and publicly visible ticket listing.
- [ ] Each event summary shows event name, event date and time, venue, location, and a safe event image or placeholder where available.
- [ ] Event summaries may show server-derived aggregate listing information such as the lowest available price and number of available listings.
- [ ] Events with no active visible listings are excluded from the default browse results.
- [ ] Expired, cancelled, hidden, and non-public events are not shown.
- [ ] Browse results support basic pagination.
- [ ] Event availability and visibility are enforced server-side.
- [ ] Sensitive ticket and seller information is not exposed.
- [ ] Relevant documentation is updated.

## Out of Scope

- Full-text search.
- Advanced event filtering.
- Event recommendations.
- Ticket purchasing.
- Seller listing management.

## Risks

- Event records created by different sellers may represent the same real-world event and require deduplication.
- Event cancellation and rescheduling rules need to be defined.
- Aggregate values such as lowest price and available listing count may become stale unless calculated carefully.
- Event images need source and moderation rules before user-uploaded images are supported.

## Follow-up Issues

- To be created after this user story is approved.
