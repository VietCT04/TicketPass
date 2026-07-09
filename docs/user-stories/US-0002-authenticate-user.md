# US-0002: Authenticate User

## User Story

As a user, I want to sign up, log in, and securely access my TicketPass account so that I can buy, sell, manage tickets, and track my transactions safely.

## Context

TicketPass requires trusted user identity before users can create listings, buy tickets, fund escrow, reveal tickets, manage disputes, or view private account and transaction data. Authentication must provide a secure foundation for account creation, login, logout, session handling, and protecting pages or API endpoints that require a signed-in user.

## Acceptance Criteria

- [ ] User can create an account with required credentials.
- [ ] User can log in with valid credentials.
- [ ] User can log out.
- [ ] Authenticated session state is maintained securely.
- [ ] Protected pages and API endpoints require a signed-in user.
- [ ] Backend derives the current user from authenticated session state, not client-provided user IDs.
- [ ] Invalid or expired sessions are rejected.
- [ ] Relevant auth and security docs are updated.

## Risks

- Weak authentication would allow users to impersonate sellers or buyers.
- Session handling mistakes could expose private tickets, orders, payments, or disputes.
- Password storage, token storage, and logout behavior need careful implementation.

## Follow-up Issues

- `#9` Define authentication model and API contract - https://github.com/VietCT04/TicketPass/issues/9
- `#10` Implement backend signup and login API - https://github.com/VietCT04/TicketPass/issues/10
- `#11` Implement backend session handling and protected current-user endpoint - https://github.com/VietCT04/TicketPass/issues/11
- `#12` Build frontend signup login logout flow - https://github.com/VietCT04/TicketPass/issues/12
- `#13` Protect authenticated frontend pages - https://github.com/VietCT04/TicketPass/issues/13
- `#14` Integrate authenticated user identity into seller-owned API work - https://github.com/VietCT04/TicketPass/issues/14
