package com.ticketpass.api.payment.webhook;

import com.ticketpass.api.listing.ListingEntity;
import com.ticketpass.api.listing.ListingRepository;
import com.ticketpass.api.listing.ListingReservationEntity;
import com.ticketpass.api.listing.ListingReservationRepository;
import com.ticketpass.api.listing.ListingReservationStatus;
import com.ticketpass.api.listing.ListingStatus;
import com.ticketpass.api.order.OrderEntity;
import com.ticketpass.api.order.OrderRepository;
import com.ticketpass.api.order.OrderStatus;
import com.ticketpass.api.payment.PaymentSessionEntity;
import com.ticketpass.api.payment.PaymentSessionRepository;
import com.ticketpass.api.payment.PaymentSessionStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutReconciliationService {

    private final ListingRepository listingRepository;
    private final ListingReservationRepository reservationRepository;
    private final OrderRepository orderRepository;
    private final PaymentSessionRepository paymentSessionRepository;
    private final PaymentWebhookReceiptRepository receiptRepository;

    public CheckoutReconciliationService(
            ListingRepository listingRepository,
            ListingReservationRepository reservationRepository,
            OrderRepository orderRepository,
            PaymentSessionRepository paymentSessionRepository,
            PaymentWebhookReceiptRepository receiptRepository) {
        this.listingRepository = listingRepository;
        this.reservationRepository = reservationRepository;
        this.orderRepository = orderRepository;
        this.paymentSessionRepository = paymentSessionRepository;
        this.receiptRepository = receiptRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String reconcileDeferredReceipt(UUID receiptId, Instant now) {
        PaymentWebhookReceiptRepository.Receipt receipt = receiptRepository.findById(receiptId);
        if (receipt == null || receipt.status() != PaymentWebhookReceiptStatus.DEFERRED) {
            return "skipped";
        }
        PaymentSessionEntity session = paymentSessionRepository.findByProviderSessionId(receipt.providerSessionId()).orElse(null);
        if (session == null) {
            PaymentWebhookReceiptRepository.Receipt currentReceipt = receiptRepository.findByIdForReconciliation(receiptId);
            if (currentReceipt != null && currentReceipt.status() == PaymentWebhookReceiptStatus.DEFERRED) {
                receiptRepository.complete(receiptId, PaymentWebhookReceiptStatus.REQUIRES_ACTION, now);
            }
            return "requires_action";
        }
        return reconcileLocked(session.getOrder().getId(), receiptId, now);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String reconcileExpiredOrder(UUID orderId, Instant now) {
        return reconcileLocked(orderId, null, now);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reconcileOrderOnRead(UUID orderId, Instant now) {
        PaymentSessionEntity session = paymentSessionRepository.findFirstByOrderIdOrderByCreatedAtAsc(orderId).orElse(null);
        if (session != null) {
            for (UUID receiptId : receiptRepository.findDeferredReceiptIdsForSession(session.getProviderSessionId())) {
                reconcileLocked(orderId, receiptId, now);
            }
        }
        reconcileLocked(orderId, null, now);
    }

    private String reconcileLocked(UUID orderId, UUID receiptId, Instant now) {
        OrderEntity initialOrder = orderRepository.findById(orderId).orElse(null);
        if (initialOrder == null) {
            return resolveMissingOrder(receiptId, now);
        }
        ListingEntity listing = listingRepository.findByIdForPayment(initialOrder.getListing().getId()).orElse(null);
        if (listing == null) {
            return resolveConflict(receiptId, now);
        }
        ListingReservationEntity reservation = reservationRepository
                .findByIdForPayment(initialOrder.getReservation().getId())
                .orElse(null);
        OrderEntity order = orderRepository.findByIdForPayment(orderId).orElse(null);
        PaymentSessionEntity session = paymentSessionRepository.findFirstByOrderIdForReconciliation(orderId).orElse(null);
        if (reservation == null || order == null || session == null || !relationshipsMatch(listing, reservation, order, session)) {
            return resolveConflict(receiptId, now);
        }

        PaymentWebhookReceiptRepository.Receipt receipt = receiptId == null ? null : receiptRepository.findByIdForReconciliation(receiptId);
        if (receipt != null && receipt.status() != PaymentWebhookReceiptStatus.DEFERRED) {
            return "skipped";
        }
        if (receipt != null && !receipt.providerSessionId().equals(session.getProviderSessionId())) {
            return resolveConflict(receiptId, now);
        }
        if (receiptRepository.hasRequiresAction(session.getProviderSessionId())) {
            return receipt == null ? "blocked" : resolveConflict(receiptId, now);
        }
        if (isCompletedSale(listing, order, session)) {
            return receipt == null ? "completed_sale" : resolveConflict(receiptId, now);
        }

        if (receipt != null) {
            if (!order.getExpiresAt().isAfter(now)) {
                return reconcileExpiry(listing, reservation, order, session, receiptId, now);
            }
            return reconcileDeferredOutcome(listing, reservation, order, session, receipt, now);
        }
        if (!order.getExpiresAt().isAfter(now)) {
            return reconcileExpiry(listing, reservation, order, session, null, now);
        }
        return "not_due";
    }

    private String reconcileDeferredOutcome(
            ListingEntity listing,
            ListingReservationEntity reservation,
            OrderEntity order,
            PaymentSessionEntity session,
            PaymentWebhookReceiptRepository.Receipt receipt,
            Instant now) {
        PaymentSessionStatus targetSessionStatus = "PAYMENT_FAILED".equals(receipt.eventType())
                ? PaymentSessionStatus.FAILED
                : PaymentSessionStatus.CANCELLED;
        OrderStatus targetOrderStatus = "PAYMENT_FAILED".equals(receipt.eventType())
                ? OrderStatus.PAYMENT_FAILED
                : OrderStatus.CANCELLED;
        if (isExactOutcome(listing, reservation, order, session, targetOrderStatus, targetSessionStatus,
                ListingReservationStatus.CANCELLED)) {
            receiptRepository.complete(receipt.id(), PaymentWebhookReceiptStatus.PROCESSED, now);
            return "already_terminal";
        }
        if (!canRelease(listing, reservation, order, session, List.of(PaymentSessionStatus.PENDING))) {
            return resolveConflict(receipt.id(), now);
        }
        session.setStatus(targetSessionStatus);
        session.setUpdatedAt(now);
        order.setStatus(targetOrderStatus);
        order.setUpdatedAt(now);
        reservation.setStatus(ListingReservationStatus.CANCELLED);
        reservation.setUpdatedAt(now);
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setUpdatedAt(now);
        receiptRepository.complete(receipt.id(), PaymentWebhookReceiptStatus.PROCESSED, now);
        return "released";
    }

    private String reconcileExpiry(
            ListingEntity listing,
            ListingReservationEntity reservation,
            OrderEntity order,
            PaymentSessionEntity session,
            UUID receiptId,
            Instant now) {
        if (isExactOutcome(listing, reservation, order, session, OrderStatus.EXPIRED,
                PaymentSessionStatus.EXPIRED, ListingReservationStatus.EXPIRED)) {
            if (receiptId != null) {
                receiptRepository.complete(receiptId, PaymentWebhookReceiptStatus.PROCESSED, now);
            }
            return "already_expired";
        }
        if (!canRelease(listing, reservation, order, session,
                List.of(PaymentSessionStatus.CREATING, PaymentSessionStatus.PENDING))) {
            return resolveConflict(receiptId, now);
        }
        session.setStatus(PaymentSessionStatus.EXPIRED);
        session.setUpdatedAt(now);
        order.setStatus(OrderStatus.EXPIRED);
        order.setUpdatedAt(now);
        reservation.setStatus(ListingReservationStatus.EXPIRED);
        reservation.setUpdatedAt(now);
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setUpdatedAt(now);
        if (receiptId != null) {
            receiptRepository.complete(receiptId, PaymentWebhookReceiptStatus.PROCESSED, now);
        }
        return "expired";
    }

    private String resolveMissingOrder(UUID receiptId, Instant now) {
        if (receiptId != null) {
            receiptRepository.complete(receiptId, PaymentWebhookReceiptStatus.REQUIRES_ACTION, now);
            return "requires_action";
        }
        return "skipped";
    }

    private String resolveConflict(UUID receiptId, Instant now) {
        if (receiptId != null) {
            receiptRepository.complete(receiptId, PaymentWebhookReceiptStatus.REQUIRES_ACTION, now);
            return "requires_action";
        }
        return "conflict";
    }

    private static boolean relationshipsMatch(
            ListingEntity listing,
            ListingReservationEntity reservation,
            OrderEntity order,
            PaymentSessionEntity session) {
        return session.getOrder().getId().equals(order.getId())
                && order.getReservation().getId().equals(reservation.getId())
                && order.getListing().getId().equals(listing.getId())
                && reservation.getListing().getId().equals(listing.getId())
                && order.getExpiresAt().equals(reservation.getExpiresAt())
                && order.getExpiresAt().equals(session.getExpiresAt())
                && order.getBuyerUserId().equals(reservation.getBuyerUserId())
                && order.getSellerUserId().equals(listing.getSeller().getId());
    }

    private static boolean canRelease(
            ListingEntity listing,
            ListingReservationEntity reservation,
            OrderEntity order,
            PaymentSessionEntity session,
            List<PaymentSessionStatus> allowedSessionStatuses) {
        return order.getStatus() == OrderStatus.PAYMENT_PENDING
                && allowedSessionStatuses.contains(session.getStatus())
                && reservation.getStatus() == ListingReservationStatus.ACTIVE
                && listing.getStatus() == ListingStatus.RESERVED;
    }

    private static boolean isExactOutcome(
            ListingEntity listing,
            ListingReservationEntity reservation,
            OrderEntity order,
            PaymentSessionEntity session,
            OrderStatus orderStatus,
            PaymentSessionStatus sessionStatus,
            ListingReservationStatus reservationStatus) {
        return order.getStatus() == orderStatus
                && session.getStatus() == sessionStatus
                && reservation.getStatus() == reservationStatus
                && listing.getStatus() == ListingStatus.ACTIVE;
    }

    private static boolean isCompletedSale(ListingEntity listing, OrderEntity order, PaymentSessionEntity session) {
        return session.getStatus() == PaymentSessionStatus.PAID
                && order.getStatus() == OrderStatus.PAID
                && listing.getStatus() == ListingStatus.SOLD;
    }
}
