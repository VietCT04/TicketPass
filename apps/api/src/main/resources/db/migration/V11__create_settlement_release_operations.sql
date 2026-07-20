alter table order_fulfillments
    add constraint chk_order_fulfillments_buyer_confirmed_receipt check (
        transfer_status <> 'BUYER_CONFIRMED_RECEIPT'
        or (seller_confirmed_at is not null and buyer_confirmed_at is not null
            and settlement_status in ('FUNDS_HELD', 'RELEASED_TO_SELLER'))
    ),
    add constraint chk_order_fulfillments_released_to_seller check (
        settlement_status <> 'RELEASED_TO_SELLER'
        or (transfer_status = 'BUYER_CONFIRMED_RECEIPT' and buyer_confirmed_at is not null
            and settlement_released_at is not null)
    ),
    add constraint chk_order_fulfillments_release_timestamp check (
        settlement_status = 'RELEASED_TO_SELLER' or settlement_released_at is null
    );

create table settlement_release_operations (
    order_id uuid primary key references orders(id),
    provider varchar(40) not null,
    idempotency_key varchar(160) not null unique,
    status varchar(40) not null,
    provider_operation_id varchar(255),
    attempt_count integer not null default 0,
    next_attempt_at timestamptz,
    processing_lease_until timestamptz,
    last_error_code varchar(80),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    completed_at timestamptz,
    constraint chk_settlement_release_operation_status check (status in ('PENDING', 'PROCESSING', 'RETRYABLE_FAILURE', 'SUCCEEDED', 'REQUIRES_REVIEW')),
    constraint chk_settlement_release_operation_attempts check (attempt_count >= 0),
    constraint chk_settlement_release_operation_updated check (updated_at >= created_at),
    constraint chk_settlement_release_operation_success check (status <> 'SUCCEEDED' or completed_at is not null),
    constraint chk_settlement_release_operation_lease check ((status = 'PROCESSING') = (processing_lease_until is not null))
);

create index idx_settlement_release_operations_claim
    on settlement_release_operations (status, next_attempt_at, processing_lease_until, created_at, order_id);
