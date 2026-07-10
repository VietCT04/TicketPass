# Concerns

## CONCERN-0004: Password Policy Needs Review

Date: 2026-07-10  
Related User Story: `docs/user-stories/US-0002-authenticate-user.md`  
Related GitHub Issue: `#9` - https://github.com/VietCT04/TicketPass/issues/9

### Concern

The MVP password policy is 12 to 128 characters, but it has not had final product/security review.

### Risk

A weak password policy may allow easily compromised accounts, while an overly strict policy may create poor usability and support issues.

### Recommendation

Review the MVP password policy before public launch and adjust if product or security requirements change.

### Status

Open

## CONCERN-0005: Session Cookie CSRF Hardening

Date: 2026-07-10  
Related User Story: `docs/user-stories/US-0002-authenticate-user.md`  
Related GitHub Issue: `#9` - https://github.com/VietCT04/TicketPass/issues/9

### Concern

The MVP auth contract uses `HttpOnly` cookies with `SameSite=Lax`, but CSRF hardening may need additional controls as deployment and cross-origin behavior become clearer.

### Risk

State-changing authenticated endpoints could be exposed to CSRF risk if cookie behavior, CORS, and frontend deployment domains are not aligned.

### Recommendation

Review CSRF strategy during backend auth implementation and add CSRF tokens or stricter cookie/domain rules if needed.

### Status

Open

## CONCERN-0006: Deferred Account Recovery And Verification

Date: 2026-07-10  
Related User Story: `docs/user-stories/US-0002-authenticate-user.md`  
Related GitHub Issue: `#9` - https://github.com/VietCT04/TicketPass/issues/9

### Concern

Email verification, password reset, MFA, OAuth/social login, admin roles, and account deletion are out of scope for the initial authentication contract.

### Risk

Users may be unable to recover accounts, verify identity, or use stronger login protections until follow-up work is planned.

### Recommendation

Create follow-up user stories or issues for account recovery, verification, MFA, and account lifecycle features before public launch.

### Status

Open

## CONCERN-0007: Local Java Runtime Cannot Verify Backend Tests

Date: 2026-07-10  
Related User Story: `docs/user-stories/US-0002-authenticate-user.md`  
Related GitHub Issue: `#10` - https://github.com/VietCT04/TicketPass/issues/10

### Concern

The backend project targets Java 21, but the current local Maven runtime uses Java 19.

### Risk

`mvn test` cannot compile the project locally, so the signup/login tests added for issue `#10` could not be fully verified in this environment.

### Recommendation

Run `mvn test` with Java 21 before merging or continuing backend implementation work.

### Status

Open

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
