# US-0022 — Manage Active Account Sessions

## User Story

As an authenticated user, I want to review my active TicketPass sessions and revoke access from sessions I no longer recognize or use, while keeping my current browser signed in.

## Context

TicketPass uses server-side opaque sessions with hashed tokens, expiry timestamps, revocation timestamps, and last-used timestamps. Users can currently log out only the current browser, while password change will revoke every previous session and issue a replacement session.

A focused session-management capability should provide visibility and targeted control without storing or displaying IP addresses, device fingerprints, locations, browser names, or user-agent data. The current session must be identified exclusively from server authentication context.

## Scope

- Let an authenticated user retrieve their active, unrevoked, unexpired sessions.
- Mark the current authenticated session using server-derived session identity.
- Return only a private session identifier plus creation, last-used, and expiry timestamps.
- Use bounded pagination and deterministic `last_used_at DESC, id DESC` ordering.
- Let the user revoke one owned non-current session.
- Let the user revoke all owned active sessions except the current session.
- Keep individual and bulk revocation idempotent.
- Preserve historical rows by setting `revoked_at`; never delete session records.
- Write one safe audit event when a request effectively revokes one or more sessions.
- Add a protected account-security page using the approved endpoints.
- Keep the current session free of revoke controls; normal logout remains authoritative for it.
- Keep session data out of URLs, analytics, logs, and browser persistence.
- Reuse existing authentication, session, cookie, origin-protection, and audit infrastructure.

## Out of Scope

- Capturing or displaying IP addresses, locations, device names, operating systems, browser names, fingerprints, or user-agent strings.
- Trusted-device features, suspicious-login detection, or login notifications.
- Password change, forgotten-password recovery, MFA, passkeys, OAuth, or account deletion.
- Admin access to user sessions.
- Revoking the current session through the new management endpoints.
- Permanent deletion or archival cleanup of historical session rows.

## Acceptance Criteria

- [ ] An authenticated user can retrieve only their own active sessions.
- [ ] The current session is marked from server authentication context.
- [ ] The user can revoke one owned non-current session idempotently.
- [ ] The user can revoke all other active sessions while preserving the current session.
- [ ] Revocation sets `revoked_at` and retains historical records.
- [ ] Effective revocation writes exactly one safe audit event per request.
- [ ] Responses and UI exclude authentication tokens, cookie values, network metadata, fingerprints, and private account data.
- [ ] Session state is not stored in browser persistence.
- [ ] The current session remains managed through the existing logout flow.

## Focused Issues

1. `#137` — Define account session-management contract.
2. `#138` — Implement account session-management backend.
3. `#139` — Build account active-sessions page.

## Delivery Order

1. Approve endpoint, pagination, current-session identity, revocation, privacy, audit, and error behavior in `#137`.
2. Implement the protected backend behavior in `#138`.
3. Add the account-security experience after the backend is available in `#139`.

## Concerns

- The current principal contains user identity but not session identity, so implementation needs a safe internal extension that does not alter public current-user responses.
- `last_used_at` is informational and may change as part of the current request's authentication.
- Session identifiers are not authentication tokens, but they remain private account references and must not be logged broadly.
- A listed session can expire or be revoked immediately after the response; mutation endpoints must revalidate authoritative state.
- Timestamp-only records may look similar, so the UI must not invent device identity.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, or other verification commands in these issues. Complete application implementation first; verification will be handled later as a separate final phase.