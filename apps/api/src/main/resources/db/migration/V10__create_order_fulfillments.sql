do $$
begin
    if exists (
        select 1
        from orders
        where status = 'PAID' and paid_at is null
    ) then
        raise exception 'Paid orders require paid_at before order fulfilments can be backfilled';
    end if;
end $$;

create table order_fulfillments (
    order_id uuid primary key references orders(id),
    transfer_status varchar(40) not null,
    settlement_status varchar(40) not null,
    transfer_deadline_at timestamptz not null,
    seller_confirmed_at timestamptz,
    buyer_confirmed_at timestamptz,
    settlement_released_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint chk_order_fulfillments_transfer_status check (
        transfer_status in (
            'AWAITING_SELLER_TRANSFER',
            'SELLER_CONFIRMED_TRANSFER',
            'BUYER_CONFIRMED_RECEIPT',
            'TRANSFER_TIMED_OUT',
            'REQUIRES_REVIEW'
        )
    ),
    constraint chk_order_fulfillments_settlement_status check (
        settlement_status in (
            'FUNDS_HELD',
            'RELEASED_TO_SELLER',
            'REFUND_REQUIRED',
            'REVIEW_REQUIRED'
        )
    ),
    constraint chk_order_fulfillments_updated_at check (updated_at >= created_at),
    constraint chk_order_fulfillments_transfer_deadline check (
        transfer_deadline_at = created_at + interval '15 minutes'
    ),
    constraint chk_order_fulfillments_awaiting_seller_transfer check (
        transfer_status <> 'AWAITING_SELLER_TRANSFER'
        or (settlement_status = 'FUNDS_HELD' and seller_confirmed_at is null)
    ),
    constraint chk_order_fulfillments_seller_confirmed_transfer check (
        transfer_status <> 'SELLER_CONFIRMED_TRANSFER'
        or (
            settlement_status = 'FUNDS_HELD'
            and seller_confirmed_at is not null
            and seller_confirmed_at < transfer_deadline_at
        )
    )
);

insert into order_fulfillments (
    order_id,
    transfer_status,
    settlement_status,
    transfer_deadline_at,
    created_at,
    updated_at
)
select
    id,
    'AWAITING_SELLER_TRANSFER',
    'FUNDS_HELD',
    paid_at + interval '15 minutes',
    paid_at,
    paid_at
from orders
where status = 'PAID';

create index idx_order_fulfillments_transfer_deadline
    on order_fulfillments (transfer_status, transfer_deadline_at, order_id);
