# Buyer Event Browse Flow

## Purpose

Buyers discover upcoming events that currently have browse-eligible listings through the public `GET /api/events` endpoint. Search and filter choices narrow this server-authoritative event set; they do not reserve inventory or expose listing, seller, ticket, ownership, payment, or transfer data.

## Public Browse Request

The base request is public and read-only:

```http
GET /api/events?page=1&page_size=20
```

Issue `#109` defines optional `q`, `city`, `starts_from`, and `starts_before` parameters. Issue `#110` implements those database-side filters, and issue `#111` adds browser controls.

The server first applies the shared browse-eligible listing rule and all valid supplied event filters, then derives aggregate values, counts matching events, orders by `starts_at ASC, id ASC`, and paginates. A filtered request cannot make an otherwise ineligible event visible.

## Search And Filter Rules

- `q` searches event name, venue, and city with a case-insensitive literal substring match.
- `city` is a case-insensitive exact match after Unicode whitespace normalization.
- `starts_from` is inclusive and `starts_before` is exclusive against `events.starts_at`.
- Time bounds must carry `Z` or an explicit numeric UTC offset, and a supplied lower bound must be earlier than a supplied upper bound.
- Empty normalized text is omitted. Invalid text, timestamps, pagination, or time ranges return controlled `400` errors.
- Empty valid filtered results return a normal `200` page with an empty `events` collection and accurate pagination metadata.

## Browser Contract

For issue `#111`, URL query parameters are the source of truth for search and filters. Filter changes reset `page` to `1`, while pagination retains every active normalized filter. No search state is stored in browser storage.

Calendar-day controls require an explicit editable UTC offset. The selected start day becomes `starts_from` at local `00:00`; the selected end day becomes `starts_before` at local `00:00` on the following day, both using that offset. This is a request-construction convention only and does not claim an authoritative event timezone.

The browse page must distinguish a filtered empty result from an unfiltered marketplace-empty state and provide an action that clears all active filters.

## Safety Boundary

The public browse response remains a marketplace snapshot. Loading it does not reserve a listing or guarantee future availability. Reservation and checkout flows must independently revalidate listing eligibility and availability server-side.
