package com.ticketpass.api.order;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    Optional<OrderEntity> findByReservationId(UUID reservationId);

    Optional<OrderEntity> findByIdAndBuyerUserId(UUID orderId, UUID buyerUserId);
}
