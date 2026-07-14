create table events (
    id uuid primary key,
    name varchar(255) not null,
    venue varchar(255) not null,
    city varchar(120) not null,
    starts_at timestamptz not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table listings (
    id uuid primary key,
    seller_id uuid not null references users(id),
    event_id uuid not null references events(id),
    event_platform varchar(120) not null,
    seat_info varchar(255) not null,
    ticket_type varchar(120) not null,
    quantity integer not null,
    currency varchar(3) not null,
    asking_price_minor bigint not null,
    transfer_method varchar(40) not null,
    is_transferable_confirmed boolean not null,
    status varchar(40) not null,
    public_notes text,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint chk_listings_quantity_one check (quantity = 1),
    constraint chk_listings_asking_price_positive check (asking_price_minor > 0),
    constraint chk_listings_transferable_confirmed check (is_transferable_confirmed = true),
    constraint chk_listings_currency_length check (char_length(currency) = 3)
);

create index idx_events_starts_at on events(starts_at);
create index idx_listings_seller_id on listings(seller_id);
create index idx_listings_event_id on listings(event_id);
create index idx_listings_status on listings(status);
