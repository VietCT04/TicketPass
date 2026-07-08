# Continuity

## Current Project State

TicketPass is an early monorepo scaffold with a Next.js frontend, Spring Boot API, shared package placeholder, and initial project documentation rules.

## Latest Completed Work

- Date: 2026-07-08
- GitHub Issue: `#1` - https://github.com/VietCT04/TicketPass/issues/1
- Summary: Split broad seller listing tracker `#1` into focused implementation issues `#2` through `#7`, added issue-sizing rules to `AGENTS.md`, and linked the split issues back to `US-0001`.
- Files changed:
  - `AGENTS.md`
  - `docs/user-stories/US-0001-list-transferable-ticket.md`
  - `docs/CONTINUITY.md`

## Active Work

- Current GitHub Issue: `#2` - Define listing data model and API contract
- Current goal: Start the seller listing implementation with the smallest contract/model slice before backend or frontend implementation.
- Current blocker: None.

## Important User Stories

- `docs/user-stories/US-0001-list-transferable-ticket.md`: Seller can list a transferable ticket safely without exposing sensitive ticket data too early.

## Known Concerns

- See `docs/CONCERNS.md` if created.
- Transferability rules vary by platform, venue, and ticket type.
- Sellers may incorrectly claim a ticket is transferable.

## Next Recommended Steps

1. Read `docs/user-stories/US-0001-list-transferable-ticket.md`.
2. Complete GitHub Issue `#2` to define the listing API, data model, statuses, and validation rules.
3. Use the documented contract to implement GitHub Issues `#3` through `#7` in focused slices.
