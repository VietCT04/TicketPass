create index idx_payment_webhook_receipts_processing
    on payment_webhook_receipts(processing_status, received_at, id);

create index idx_payment_webhook_receipts_session_status
    on payment_webhook_receipts(provider_session_id, processing_status);
