# Admin Event-Request Review Flow

User Story: `docs/user-stories/US-0024-admin-reviews-event-requests.md`  
Contract Issue: `#145` - https://github.com/VietCT04/TicketPass/issues/145

## Purpose

Define the server-authoritative lifecycle for a private administrator to review a seller's missing-event request without exposing seller identity or allowing a request to bypass normal listing validation.

## Preconditions

- The reviewer has an authenticated session backed by persisted `ADMIN` authority.
- The request exists in `PENDING` state.
- The server uses the approved normalization for event name, venue, and city.

## Review Steps

1. The administrator loads the no-store queue or detail endpoint. The server applies database-side filtering, ordering, counting, and pagination.
2. The server exposes safe request metadata, untrusted official URL, safe resolution fields, and exact pending-sibling count only. It never exposes requester identity, normalized values, ticket, listing, payment, or session data.
3. The administrator chooses exactly one action: create a canonical event, link one existing future event, or reject the request.
4. The server captures one timestamp, serializes the exact identity, locks and revalidates the target and exact pending siblings, then creates/recover-validates or links the event.
5. The server resolves the target and exact pending siblings in the same transaction. Direct approvals use `CREATED_EVENT` or `LINKED_EVENT`; sibling approvals use `EXACT_MATCHED`. A rejection applies only to the target.
6. The server writes the minimal audit rows in the same transaction. Any audit failure rolls back the entire resolution.
7. The transaction commits once. An identical repeat returns the existing safe terminal state without a new timestamp or audit row; a conflicting decision returns a controlled conflict.

## Seller Continuation

An owning seller may later query their own request. An approved response includes only a safe resolved-event summary. The browser can open `/sell?event_request_id={owned-request-id}`, load the owned request, and preselect only that returned event. The request ID is never an event ID, and listing creation independently revalidates the selected event and all listing rules.

## Boundaries

- The backend never fetches, follows, scrapes, or trusts the submitted official URL.
- Similar but nonexact requests are never auto-resolved.
- A terminal request is not reopened, edited, or reassigned by this contract.
- This flow adds no event editing, notification, payment, escrow, ticket transfer, reveal, dispute, or seller/admin contact exchange.
