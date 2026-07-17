package com.ticketpass.api.order;

import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    Optional<OrderEntity> findByReservationId(UUID reservationId);

    Optional<OrderEntity> findByIdAndBuyerUserId(UUID orderId, UUID buyerUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select checkoutOrder from OrderEntity checkoutOrder where checkoutOrder.reservation.id = :reservationId")
    Optional<OrderEntity> findByReservationIdForCheckout(@Param("reservationId") UUID reservationId);
}
