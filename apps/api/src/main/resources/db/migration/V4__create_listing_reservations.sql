create table listing_reservations (
    id uuid primary key,
    listing_id uuid not null references listings(id),
    buyer_user_id uuid not null references users(id),
    status varchar(40) not null,
    expires_at timestamptz not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint chk_listing_reservations_status
        check (status in ('ACTIVE', 'EXPIRED', 'CANCELLED'))
);

create unique index uq_listing_reservations_active_listing
    on listing_reservations(listing_id)
    where status = 'ACTIVE';

create index idx_listing_reservations_buyer_user_id
    on listing_reservations(buyer_user_id);

create index idx_listing_reservations_status_expires_at
    on listing_reservations(status, expires_at);
