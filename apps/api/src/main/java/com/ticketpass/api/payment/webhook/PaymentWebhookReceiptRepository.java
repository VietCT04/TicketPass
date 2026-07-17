package com.ticketpass.api.payment.webhook;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentWebhookReceiptRepository {

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

    List<UUID> findDeferredCandidateIds(int batchSize) {
        return jdbcTemplate.queryForList("""
                select id
                from payment_webhook_receipts
                where processing_status = 'DEFERRED'
                    and event_type in ('PAYMENT_FAILED', 'PAYMENT_CANCELLED')
                order by received_at asc, id asc
                limit ?
                """, UUID.class, batchSize);
    }

    List<UUID> findDeferredReceiptIdsForSession(String providerSessionId) {
        return jdbcTemplate.queryForList("""
                select id
                from payment_webhook_receipts
                where provider_session_id = ?
                    and processing_status = 'DEFERRED'
                    and event_type in ('PAYMENT_FAILED', 'PAYMENT_CANCELLED')
                order by received_at asc, id asc
                """, UUID.class, providerSessionId);
    }

    Receipt findById(UUID id) {
        List<Receipt> receipts = jdbcTemplate.query("""
                select id, provider_session_id, event_type, processing_status
                from payment_webhook_receipts
                where id = ?
                """, (resultSet, rowNumber) -> new Receipt(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("provider_session_id"),
                resultSet.getString("event_type"),
                PaymentWebhookReceiptStatus.valueOf(resultSet.getString("processing_status"))), id);
        return receipts.isEmpty() ? null : receipts.getFirst();
    }

    Receipt findByIdForReconciliation(UUID id) {
        List<Receipt> receipts = jdbcTemplate.query("""
                select id, provider_session_id, event_type, processing_status
                from payment_webhook_receipts
                where id = ?
                for update
                """, (resultSet, rowNumber) -> new Receipt(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("provider_session_id"),
                resultSet.getString("event_type"),
                PaymentWebhookReceiptStatus.valueOf(resultSet.getString("processing_status"))), id);
        return receipts.isEmpty() ? null : receipts.getFirst();
    }

    public boolean hasRequiresAction(String providerSessionId) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(*)
                from payment_webhook_receipts
                where provider_session_id = ? and processing_status = 'REQUIRES_ACTION'
                """, Integer.class, providerSessionId);
        return count != null && count > 0;
    }

    record Receipt(UUID id, String providerSessionId, String eventType, PaymentWebhookReceiptStatus status) {
    }
}
