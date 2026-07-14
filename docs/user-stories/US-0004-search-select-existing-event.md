# US-0004: Search and Select an Existing Event

## User Story

As a seller, I want to search for and select an existing event when creating a ticket listing so that my ticket is attached to the correct marketplace event without creating duplicate event records.

## Context

TicketPass is moving toward an event-first marketplace. Sellers should not manually create or redefine event records from the listing form. Before entering ticket-specific listing details, a seller must find and select an existing event through an autocomplete search experience.

This user story provides the event-selection prerequisite for the seller listing form in issue `#6`.

## Acceptance Criteria

- [ ] Seller can search existing TicketPass events while creating a listing.
- [ ] Search results are presented through an autocomplete selection experience.
- [ ] Each result shows enough safe event information to distinguish similar events, such as event name, date/time, venue, and location.
- [ ] Seller must select a valid existing event result; free-text event creation is not accepted.
- [ ] The selected event is represented by `event_id` in the listing creation flow.
- [ ] Event identity and eligibility are validated server-side.
- [ ] If no matching event exists, the seller cannot create a listing for that event through the MVP flow.
- [ ] Search loading, empty, error, and selection states are handled.
- [ ] Search results do not expose sensitive ticket, seller, or ownership information.
- [ ] Relevant API, database, security, flow, concern, and continuity documentation is updated where required.

## Out of Scope

- Allowing sellers to create new event records.
- Reporting or requesting a missing event.
- Event administration or moderation.
- Event deduplication implementation.
- Advanced search ranking, recommendations, or full marketplace filtering.
- Seller ticket-detail form implementation beyond integrating the selected `event_id`.
- Listing detail page implementation.

## Risks

- The exact searchable fields and matching behavior are not yet defined and require follow-up contract work.
- Similar event names, repeated performances, and rescheduled events may make selection ambiguous without sufficient date, venue, and location context.
- Autocomplete queries may create unnecessary backend load unless minimum query length, debouncing, result limits, and pagination behavior are defined.
- Existing duplicate event records may still appear as separate results until event deduplication rules are implemented.
- Sellers cannot list tickets for missing events until a separate event-reporting or event-request flow exists.

## Dependencies

- Existing event data and event identity model.
- A public or seller-safe event search API contract and backend implementation.

## Follow-up Work

- Define the event autocomplete API contract, searchable fields, matching rules, result limits, and eligibility rules.
- Implement the event search/autocomplete backend endpoint.
- Implement the frontend autocomplete event selector.
- Update the listing creation contract to accept `event_id` instead of seller-provided event fields.
- Add a later user story for reporting or requesting a missing event.
- Unblock issue `#6` only after the required event search, selection, and listing-contract dependencies are complete.
