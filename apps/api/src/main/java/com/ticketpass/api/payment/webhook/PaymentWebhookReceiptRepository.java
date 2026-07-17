package com.ticketpass.api.payment.webhook;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class PaymentWebhookReceiptRepository {

    private final JdbcTemplate jdbcTemplate;

    PaymentWebhookReceiptRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    boolean insert(UUID id, MockWebhookPayload payload, String storedEventType, Instant now) {
        return jdbcTemplate.update("""
                insert into payment_webhook_receipts (
                    id, provider, provider_event_id, provider_session_id, event_type,
                    processing_status, received_at, processed_at, updated_at
                ) values (?, 'MOCK', ?, ?, ?, 'REQUIRES_ACTION', ?, ?, ?)
                on conflict (provider, provider_event_id) do nothing
                """,
                id,
                payload.eventId(),
                payload.providerSessionId(),
                storedEventType,
                Timestamp.from(now),
                Timestamp.from(now),
                Timestamp.from(now)) == 1;
    }

    void complete(UUID id, PaymentWebhookReceiptStatus status, Instant now) {
        jdbcTemplate.update("""
                update payment_webhook_receipts
                set processing_status = ?, processed_at = ?, updated_at = ?
                where id = ?
                """, status.name(), Timestamp.from(now), Timestamp.from(now), id);
    }
}
