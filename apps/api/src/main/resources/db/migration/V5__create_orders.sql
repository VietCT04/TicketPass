create table orders (
    id uuid primary key,
    reservation_id uuid not null references listing_reservations(id),
    buyer_user_id uuid not null references users(id),
    seller_user_id uuid not null references users(id),
    listing_id uuid not null references listings(id),
    amount_minor bigint not null,
    currency varchar(3) not null,
    status varchar(40) not null,
    expires_at timestamptz not null,
    paid_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uq_orders_reservation unique (reservation_id),
    constraint chk_orders_amount_positive check (amount_minor > 0),
    constraint chk_orders_currency_vnd check (currency = 'VND'),
    constraint chk_orders_status check (
        status in (
            'PAYMENT_PENDING',
            'PAID',
            'PAYMENT_FAILED',
            'CANCELLED',
            'EXPIRED'
        )
    ),
    constraint chk_orders_paid_at check (
        (status = 'PAID' and paid_at is not null)
        or
        (status <> 'PAID' and paid_at is null)
    ),
    constraint chk_orders_expiry_after_creation check (expires_at > created_at)
);

create index idx_orders_buyer_user_id
    on orders(buyer_user_id);

create index idx_orders_status_expires_at
    on orders(status, expires_at);
