# Concerns

## CONCERN-0001: Platform-Specific Transferability Rules

Date: 2026-07-09  
Related User Story: `docs/user-stories/US-0001-list-transferable-ticket.md`  
Related GitHub Issue: `#2` - https://github.com/VietCT04/TicketPass/issues/2

### Concern

Ticket transferability varies by event platform, venue, event organizer, and ticket type.

### Risk

A seller may create a listing for a ticket that cannot actually be transferred, which can cause buyer harm, disputes, refunds, or manual support work.

### Recommendation

Track `event_platform` from the start and later define platform-specific validation, seller warnings, or provider integrations before enabling higher-risk transfer methods at scale.

### Status

Open

## CONCERN-0002: Seller Transferability Confirmation Is Not Proof

Date: 2026-07-09  
Related User Story: `docs/user-stories/US-0001-list-transferable-ticket.md`  
Related GitHub Issue: `#2` - https://github.com/VietCT04/TicketPass/issues/2

### Concern

The listing contract requires `is_transferable_confirmed`, but this is only a seller assertion.

### Risk

Sellers may misunderstand the source platform rules or falsely claim the ticket is transferable.

### Recommendation

Use this confirmation as an MVP safeguard only. Add stronger verification, provider-specific guidance, and dispute evidence requirements in later user stories or implementation issues.

### Status

Open

## CONCERN-0003: Event Reuse And Deduplication

Date: 2026-07-09  
Related User Story: `docs/user-stories/US-0001-list-transferable-ticket.md`  
Related GitHub Issue: `#2` - https://github.com/VietCT04/TicketPass/issues/2

### Concern

Events are normalized into an `events` table, but the MVP contract does not define exact matching or deduplication rules for reusing existing events.

### Risk

The database may accumulate duplicate event rows for the same real-world event.

### Recommendation

Allow duplicate event rows for MVP if needed, then introduce event search, matching, or admin curation when buyer discovery and marketplace quality require it.

### Status

Open
