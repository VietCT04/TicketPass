create table payment_sessions (
    id uuid primary key,
    order_id uuid not null references orders(id),
    provider varchar(40) not null,
    provider_session_id varchar(120) not null,
    status varchar(40) not null,
    expires_at timestamptz not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uq_payment_sessions_provider_session unique (provider_session_id),
    constraint chk_payment_sessions_provider check (provider = 'MOCK'),
    constraint chk_payment_sessions_status check (
        status in ('CREATING', 'PENDING', 'PAID', 'FAILED', 'CANCELLED', 'EXPIRED')
    ),
    constraint chk_payment_sessions_expiry_after_creation check (expires_at > created_at)
);

create unique index uq_payment_sessions_usable_order
    on payment_sessions(order_id)
    where status in ('CREATING', 'PENDING');

create table mock_provider_sessions (
    id uuid primary key,
    provider_session_id varchar(120) not null unique,
    order_id uuid not null references orders(id),
    amount_minor bigint not null,
    currency varchar(3) not null,
    status varchar(40) not null,
    expires_at timestamptz not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint chk_mock_provider_sessions_amount_positive check (amount_minor > 0),
    constraint chk_mock_provider_sessions_currency_vnd check (currency = 'VND'),
    constraint chk_mock_provider_sessions_status check (
        status in ('PENDING', 'PAID', 'FAILED', 'CANCELLED', 'EXPIRED')
    ),
    constraint chk_mock_provider_sessions_expiry_after_creation check (expires_at > created_at)
);

create table mock_payment_events (
    id uuid primary key,
    provider_session_id varchar(120) not null references mock_provider_sessions(provider_session_id),
    event_type varchar(40) not null,
    delivery_status varchar(40) not null,
    created_at timestamptz not null,
    delivered_at timestamptz,
    constraint chk_mock_payment_events_type check (
        event_type in ('PAYMENT_SUCCEEDED', 'PAYMENT_FAILED', 'PAYMENT_CANCELLED')
    ),
    constraint chk_mock_payment_events_delivery_status check (
        delivery_status in ('PENDING', 'DELIVERED')
    ),
    constraint chk_mock_payment_events_delivered_at check (
        (delivery_status = 'DELIVERED' and delivered_at is not null)
        or (delivery_status = 'PENDING' and delivered_at is null)
    )
);
