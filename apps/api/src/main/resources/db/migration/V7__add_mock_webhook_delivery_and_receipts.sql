alter table mock_payment_events
    add column attempt_count integer not null default 0,
    add column next_attempt_at timestamptz,
    add column last_attempt_at timestamptz;

update mock_payment_events set next_attempt_at = created_at where next_attempt_at is null;
alter table mock_payment_events alter column next_attempt_at set not null;

alter table mock_payment_events drop constraint chk_mock_payment_events_delivery_status;
alter table mock_payment_events add constraint chk_mock_payment_events_delivery_status
    check (delivery_status in ('PENDING', 'DELIVERED', 'DEAD_LETTER'));
alter table mock_payment_events drop constraint chk_mock_payment_events_delivered_at;
alter table mock_payment_events add constraint chk_mock_payment_events_delivered_at check (
    (delivery_status = 'DELIVERED' and delivered_at is not null)
    or (delivery_status in ('PENDING', 'DEAD_LETTER') and delivered_at is null)
);
create index idx_mock_payment_events_delivery on mock_payment_events(delivery_status, next_attempt_at, id);

create table payment_webhook_receipts (
    id uuid primary key,
    provider varchar(40) not null,
    provider_event_id varchar(120) not null,
    provider_session_id varchar(120) not null,
    event_type varchar(40) not null,
    processing_status varchar(40) not null,
    received_at timestamptz not null,
    processed_at timestamptz,
    updated_at timestamptz not null,
    constraint uq_payment_webhook_receipts_provider_event unique (provider, provider_event_id),
    constraint chk_payment_webhook_receipts_provider check (provider = 'MOCK'),
    constraint chk_payment_webhook_receipts_event_type check (event_type in ('PAYMENT_SUCCEEDED', 'PAYMENT_FAILED', 'PAYMENT_CANCELLED', 'UNSUPPORTED')),
    constraint chk_payment_webhook_receipts_status check (processing_status in ('PROCESSED', 'DEFERRED', 'REQUIRES_ACTION', 'IGNORED')),
    constraint chk_payment_webhook_receipts_processed_at check (
        (processing_status in ('PROCESSED', 'DEFERRED', 'REQUIRES_ACTION', 'IGNORED') and processed_at is not null)
    )
);
