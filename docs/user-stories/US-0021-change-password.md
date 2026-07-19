# US-0021 — Change Password and Revoke Existing Sessions

## User Story

As an authenticated user, I want to change my password securely so that I can protect my account and invalidate sessions that may no longer be trusted.

## Context

TicketPass already uses password hashes and server-side opaque sessions with revocation timestamps. Users can sign up, log in, inspect the current session, and log out, but they cannot replace their password.

A password change must verify the current credential, atomically replace the hash, revoke every existing session, and issue one fresh session for the current browser. It must remain separate from forgotten-password recovery, email delivery, MFA, and device-management features.

## Scope

- Let an authenticated user change their password through `POST /api/me/password`.
- Accept only the current password and new password from the client.
- Require the current password to match the stored hash.
- Apply the existing password length rules to the new password and reject reuse of the current password.
- Never trim, normalize, return, log, or persist plaintext password values.
- Atomically replace the password hash and revoke all existing server-side sessions.
- Insert one fresh opaque session after revocation and rotate the current browser's session cookie.
- Keep the current browser authenticated after success while invalidating every earlier session token.
- Write one safe immutable password-change audit event.
- Add a protected account-security form that keeps sensitive values in form memory only.
- Keep password, hash, token, cookie, and request-body values out of responses, logs, analytics, URLs, and browser persistence.

## Out of Scope

- Forgotten-password reset or recovery email.
- Email verification, MFA, OAuth, passkeys, or security questions.
- Password history or compromised-password-provider integration.
- Changing email, display name, roles, or account ownership.
- Account deletion.
- Listing or selectively revoking sessions by device.
- Dedicated authentication rate-limiting implementation.

## Acceptance Criteria

- [ ] Only an authenticated user with the correct current password can change it.
- [ ] The new password follows the approved validation rules and differs from the current password.
- [ ] Successful change atomically updates the hash and revokes all earlier sessions.
- [ ] One fresh session is created after revocation and returned through the standard cookie configuration.
- [ ] The current browser remains authenticated after success.
- [ ] Any failure leaves the old password and session state unchanged.
- [ ] Exactly one safe audit event records a successful password change.
- [ ] The frontend never persists or exposes password or session secrets.

## Focused Issues

1. `#133` — Define authenticated password-change contract.
2. `#134` — Implement password change and session rotation backend.
3. `#135` — Build account password-change flow.

## Delivery Order

1. Approve the endpoint, validation, transaction, session rotation, cookie, audit, error, and privacy contract in `#133`.
2. Implement the backend mutation after the contract is approved in `#134`.
3. Add the protected account-security form after the backend is available in `#135`.

## Concerns

- Session revocation must happen before inserting the replacement session.
- Password update, session revocation, replacement-session creation, and audit persistence must share one transaction.
- An authenticated stolen session could attempt current-password guesses; dedicated rate limiting should be handled separately.
- Cookie rotation occurs in the HTTP response after the database transaction commits, so a response-delivery failure may require the user to log in again with the new password.
- Error handling and telemetry must redact every password form value and session secret.

## Verification Decision

Do not add or run tests, lint, build, compilation, typecheck, formatting, or other verification commands in these issues. Complete application implementation first; verification will be handled later as a separate final phase.