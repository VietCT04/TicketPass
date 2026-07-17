package com.ticketpass.api.order;

import java.time.Instant;
import java.util.List;
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

    @Query(value = """
            select checkout_order.id
            from orders checkout_order
            where checkout_order.status = 'PAYMENT_PENDING'
                and checkout_order.expires_at <= :now
                and exists (
                    select 1
                    from payment_sessions payment_session
                    where payment_session.order_id = checkout_order.id
                        and payment_session.status in ('CREATING', 'PENDING')
                )
                and not exists (
                    select 1
                    from payment_webhook_receipts receipt
                    join payment_sessions payment_session on payment_session.provider_session_id = receipt.provider_session_id
                    where payment_session.order_id = checkout_order.id
                        and receipt.processing_status = 'REQUIRES_ACTION'
                )
            order by checkout_order.expires_at asc, checkout_order.id asc
            """, nativeQuery = true)
    List<UUID> findExpiredPendingOrderIds(@Param("now") Instant now, org.springframework.data.domain.Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select checkoutOrder from OrderEntity checkoutOrder where checkoutOrder.reservation.id = :reservationId")
    Optional<OrderEntity> findByReservationIdForCheckout(@Param("reservationId") UUID reservationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select checkoutOrder from OrderEntity checkoutOrder where checkoutOrder.id = :orderId")
    Optional<OrderEntity> findByIdForPayment(@Param("orderId") UUID orderId);

    @Query("""
            select checkoutOrder from OrderEntity checkoutOrder
            join fetch checkoutOrder.reservation reservation
            join fetch checkoutOrder.listing listing
            join fetch listing.event
            where checkoutOrder.id = :orderId and checkoutOrder.buyerUserId = :buyerUserId
            """)
    Optional<OrderEntity> findByIdAndBuyerUserIdForResponse(
            @Param("orderId") UUID orderId,
            @Param("buyerUserId") UUID buyerUserId);

    @Query("""
            select checkoutOrder from OrderEntity checkoutOrder
            join fetch checkoutOrder.reservation reservation
            join fetch checkoutOrder.listing listing
            join fetch listing.event
            where checkoutOrder.id = :orderId
            """)
    Optional<OrderEntity> findByIdForResponse(@Param("orderId") UUID orderId);
}
