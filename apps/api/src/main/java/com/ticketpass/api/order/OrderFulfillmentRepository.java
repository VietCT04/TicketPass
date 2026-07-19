package com.ticketpass.api.order;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderFulfillmentRepository extends JpaRepository<OrderFulfillmentEntity, UUID> {

    Optional<OrderFulfillmentEntity> findByOrderId(UUID orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select fulfillment from OrderFulfillmentEntity fulfillment where fulfillment.orderId = :orderId")
    Optional<OrderFulfillmentEntity> findByOrderIdForUpdate(@Param("orderId") UUID orderId);
}
