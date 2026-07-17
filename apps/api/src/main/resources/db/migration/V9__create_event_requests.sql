create table event_requests (
    id uuid primary key,
    requester_user_id uuid not null references users(id),
    event_name varchar(255) not null,
    normalized_event_name varchar(255) not null,
    starts_at timestamptz not null,
    venue varchar(255) not null,
    normalized_venue varchar(255) not null,
    city varchar(120) not null,
    normalized_city varchar(120) not null,
    official_url varchar(2048),
    status varchar(40) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint chk_event_requests_status check (status = 'PENDING'),
    constraint chk_event_requests_event_name check (length(normalized_event_name) > 0),
    constraint chk_event_requests_venue check (length(normalized_venue) > 0),
    constraint chk_event_requests_city check (length(normalized_city) > 0),
    constraint chk_event_requests_future_start check (starts_at > created_at),
    constraint chk_event_requests_updated_at check (updated_at >= created_at)
);

create unique index uq_event_requests_pending_duplicate
    on event_requests (
        requester_user_id,
        normalized_event_name,
        starts_at,
        normalized_venue,
        normalized_city
    )
    where status = 'PENDING';
