package com.ticketpass.api.payment.mock;

import java.util.UUID;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MockPaymentEventRepository extends JpaRepository<MockPaymentEventEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select event from MockPaymentEventEntity event where event.id = :id")
    java.util.Optional<MockPaymentEventEntity> findByIdForDelivery(@Param("id") UUID id);

    @Query("""
            select event from MockPaymentEventEntity event
            where event.deliveryStatus = :status and event.nextAttemptAt <= :now
            order by event.nextAttemptAt asc, event.id asc
            """)
    List<MockPaymentEventEntity> findDeliveryCandidates(
            @Param("status") String status, @Param("now") Instant now, Pageable pageable);
}
