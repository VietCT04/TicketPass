# US-0023 — Update Account Display Name

## User Story

As an authenticated user, I want to correct my TicketPass display name so that my account shows the name I currently want to use without changing my login credentials or active sessions.

## Context

TicketPass stores `users.display_name` and returns it through signup, login, and `GET /api/me`. The value is currently fixed after signup even though it is ordinary profile data rather than account identity or authentication state.

A focused profile update should therefore change only the existing display-name field, preserve email and credentials, keep all sessions active, and reuse the current authenticated-user response shape.

## Scope

- Let an authenticated user replace their own display name through `PUT /api/me/profile`.
- Accept exactly one mutable field: `display_name`.
- Derive account ownership exclusively from authenticated server context.
- Trim the submitted display name, require a non-empty result, and enforce the existing 120-character maximum.
- Keep user ID, email, password hash, roles, creation time, account state, and session records server-controlled.
- Serialize the update through the same user-row lock used by other account mutations.
- Treat normalized no-op requests as idempotent without changing `updated_at`.
- Return the existing safe authenticated-user representation after success.
- Keep every valid existing session active and unchanged.
- Add a focused account-profile form that replaces current-user UI state from the server response.
- Keep profile-edit state out of browser persistence and URLs.
- Reuse the existing users table without a migration or audit event.

## Out of Scope

- Changing email, password, roles, permissions, ownership, or account status.
- Session creation, rotation, revocation, device management, or logout changes.
- Account deletion or recovery.
- Profile images, usernames, biographies, contact information, or public profile pages.
- Display-name moderation, uniqueness, reserved-name policy, notifications, or analytics.

## Acceptance Criteria

- [ ] Only the authenticated user can update their own display name.
- [ ] Only `display_name` is accepted as mutable profile data.
- [ ] Validation and trimming match the approved signup-compatible rules.
- [ ] No-op retries do not change timestamps or persistence state.
- [ ] Effective changes update only `display_name` and `updated_at`.
- [ ] Email, password, roles, ownership, and sessions remain unchanged.
- [ ] The response and frontend use only the safe current-user representation.
- [ ] No schema migration or audit event is added.
- [ ] Profile-edit state is not stored in browser persistence or URLs.

## Focused Issues

1. `#141` — Define authenticated profile update contract.
2. `#142` — Implement authenticated profile update backend.
3. `#143` — Build account profile update flow.

## Delivery Order

1. Approve the endpoint, validation, locking, idempotency, response, and privacy contract in `#141`.
2. Implement the protected backend update in `#142`.
3. Add the account-profile form after the backend is available in `#143`.

## Concerns

- Display names remain untrusted text wherever rendered.
- Existing validation allows broad Unicode text; moderation and reserved-name policy remain future work.
- The authenticated principal is a request snapshot, so the mutation must reload the user row.
- Profile, password, and session-management work may share an account surface and should remain composable rather than becoming a broad redesign.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, or other verification commands in these issues. Complete application implementation first; verification will be handled later as a separate final phase.