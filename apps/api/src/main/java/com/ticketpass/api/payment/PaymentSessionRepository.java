package com.ticketpass.api.payment;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentSessionRepository extends JpaRepository<PaymentSessionEntity, UUID> {

    Optional<PaymentSessionEntity> findByProviderSessionId(String providerSessionId);

    Optional<PaymentSessionEntity> findByOrderIdAndStatusIn(
            UUID orderId,
            Collection<PaymentSessionStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select session from PaymentSessionEntity session
            where session.order.id = :orderId and session.status in :statuses
            """)
    Optional<PaymentSessionEntity> findByOrderIdAndStatusInForCheckout(
            @Param("orderId") UUID orderId,
            @Param("statuses") Collection<PaymentSessionStatus> statuses);
}
