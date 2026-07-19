# Account Profile Flow

## Source

User Story: `docs/user-stories/US-0023-update-account-profile.md`  
GitHub Issues: `#141` - https://github.com/VietCT04/TicketPass/issues/141, `#142` - https://github.com/VietCT04/TicketPass/issues/142, `#143` - https://github.com/VietCT04/TicketPass/issues/143

## Goal

Let an authenticated user correct only their display name while keeping identity, credentials, authorization, and session lifecycle server-controlled.

## Contract Status

Issues `#141` and `#142` define and implement this contract. The protected backend mutation is available; the account UI remains deferred to `#143`.

## Server Flow

1. The browser sends `PUT /api/me/profile` with credentials and exactly `{ "display_name": "..." }`.
2. Spring Security authenticates the opaque session, and the existing exact trusted-origin protection evaluates this unsafe request.
3. The controller obtains `AuthenticatedUser` and passes only its ID and the request to a transactional account service.
4. The service captures one injected-clock timestamp, locks and reloads the user row, and returns standard `401 Authentication required` if that row no longer exists.
5. The service validates the raw string, trims leading and trailing whitespace, rejects an empty trimmed value, and preserves internal whitespace.
6. A normalized value equal to the stored display name returns the current safe user response without modifying persistence, timestamps, audit data, cookies, or sessions.
7. An effective change updates only `users.display_name` and `users.updated_at`, then returns the safe user response with `Cache-Control: no-store`.

## Browser Flow

The future protected account surface loads the authoritative current user with `GET /api/me`, pre-fills only `display_name`, and may show email as read-only context. It submits the complete one-field payload with `credentials: "include"`, prevents duplicate submissions, and replaces in-memory current-user state from the `200` response. A successful update must not log the user out, rotate cookies, or redirect to login.

The browser treats `401` as session loss through the existing safe login-return flow. It presents controlled validation, malformed-response, network, and unexpected errors without exposing backend bodies or technical details. The form value remains in component memory only and is never placed in URLs or browser persistence.

## Boundaries

- No email, password, role, permission, account-status, user-ID, session, cookie, timestamp, listing, reservation, order, payment, fulfilment, or audit mutation is allowed.
- No audit event is created for this low-risk profile-text change in MVP.
- No profile image, username, alias, biography, contact information, public profile, moderation, reserved-name rule, notification, analytics, or account deletion is included.
- Display names are untrusted text and must be rendered as text, never HTML.
