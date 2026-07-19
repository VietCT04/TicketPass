# US-0023: Update Account Profile

## User Story

As an authenticated user, I want to update my display name so that my TicketPass account shows the name I want without changing my login or security settings.

## Context

TicketPass already stores and returns a display name during signup, login, and current-user reads, but users cannot correct it. This story introduces only a private, server-authoritative display-name update. Email, credentials, authorization, account state, and sessions need separate security contracts.

## Scope

- Let an authenticated user replace only their own display name through the approved private profile contract.
- Validate a required string of at most 120 raw-input characters, trim leading and trailing whitespace, and reject a value empty after trimming.
- Preserve internal whitespace and keep the name untrusted display text.
- Make normalized repeats idempotent without changing `updated_at`, session state, cookies, or audit records.
- Return the existing safe authenticated-user representation as the authoritative result.
- Keep ownership, locking, timestamps, origin protection, and response privacy server-authoritative.

## Out Of Scope

- Email, password, role, permission, account-status, session, or account-deletion changes.
- Profile images, usernames, aliases, biographies, contact details, public profiles, moderation, reserved-name rules, notifications, analytics, or profile history.
- Listing, reservation, order, payment, ticket transfer, reveal, escrow, settlement, dispute, or audit changes.

## Acceptance Criteria

- [ ] Only the authenticated user can update their own display name.
- [ ] The profile request accepts only `display_name`; server-controlled fields are not mutable.
- [ ] Validation and trimming match the documented signup-compatible rules.
- [ ] The server reloads and locks the current user before deciding whether to update.
- [ ] A normalized no-op returns success without changing `updated_at` or active sessions.
- [ ] An effective update changes only `display_name` and `updated_at`.
- [ ] The response is the safe current-user representation and sends `Cache-Control: no-store`.
- [ ] Profile text is not persisted in browser storage or URLs and is rendered as text.
- [ ] Relevant API, database, security, flow, concern, and continuity documentation is updated.

## Focused Issues

- `#141` - Define authenticated profile update contract.
- `#142` - Implement authenticated profile update backend.
- `#143` - Build account profile update flow.

## Delivery Order

1. Complete `#141` to approve the API, ownership, validation, locking, idempotency, privacy, and documentation contract.
2. Complete `#142` to implement the protected backend mutation.
3. Complete `#143` to add the protected account form after the endpoint exists.

## Risks

- Display names remain broad untrusted text; moderation, impersonation, reserved-name, and stronger Unicode policy are deferred.
- Concurrent password and profile changes must use the same user-row locking discipline.
- The authenticated principal is a request snapshot and must not be treated as a mutable or current persistence entity.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, or other verification commands while implementing this story. Complete application implementation first; verification will be handled later as a separate final phase.
