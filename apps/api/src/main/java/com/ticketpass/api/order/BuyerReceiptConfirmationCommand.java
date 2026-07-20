package com.ticketpass.api.order;

import com.ticketpass.api.audit.AuditAction;
import com.ticketpass.api.audit.AuditService;
import com.ticketpass.api.common.ApiException;
import com.ticketpass.api.listing.ListingEntity;
import com.ticketpass.api.listing.ListingRepository;
import com.ticketpass.api.listing.ListingReservationEntity;
import com.ticketpass.api.listing.ListingReservationRepository;
import com.ticketpass.api.listing.ListingStatus;
import com.ticketpass.api.settlement.SettlementProvider;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuyerReceiptConfirmationCommand {

    private final OrderRepository orderRepository;
    private final ListingRepository listingRepository;
    private final ListingReservationRepository reservationRepository;
    private final OrderFulfillmentRepository fulfillmentRepository;
    private final SettlementReleaseOperationRepository operationRepository;
    private final SettlementProvider settlementProvider;
    private final AuditService auditService;
    private final Clock clock;

    public BuyerReceiptConfirmationCommand(
            OrderRepository orderRepository,
            ListingRepository listingRepository,
            ListingReservationRepository reservationRepository,
            OrderFulfillmentRepository fulfillmentRepository,
            SettlementReleaseOperationRepository operationRepository,
            SettlementProvider settlementProvider,
            AuditService auditService,
            Clock clock) {
        this.orderRepository = orderRepository;
        this.listingRepository = listingRepository;
        this.reservationRepository = reservationRepository;
        this.fulfillmentRepository = fulfillmentRepository;
        this.operationRepository = operationRepository;
        this.settlementProvider = settlementProvider;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional
    public UUID accept(UUID buyerId, UUID orderId) {
        BuyerReceiptLockTarget target = orderRepository.findBuyerReceiptLockTarget(orderId, buyerId)
                .orElseThrow(BuyerReceiptConfirmationCommand::notFound);
        ListingEntity listing = listingRepository.findByIdForPayment(target.listingId())
                .orElseThrow(BuyerReceiptConfirmationCommand::notFound);
        ListingReservationEntity reservation = reservationRepository.findByIdForPayment(target.reservationId())
                .orElseThrow(BuyerReceiptConfirmationCommand::notFound);
        OrderEntity order = orderRepository.findByIdForPayment(orderId)
                .orElseThrow(BuyerReceiptConfirmationCommand::notFound);
        OrderFulfillmentEntity fulfillment = fulfillmentRepository.findByOrderIdForUpdate(orderId)
                .orElseThrow(BuyerReceiptConfirmationCommand::ineligible);

        validateOrder(buyerId, listing, reservation, order);
        confirmOrRecover(buyerId, orderId, fulfillment);
        createOperationIfAbsent(orderId, order);
        return orderId;
    }

    private void validateOrder(
            UUID buyerId,
            ListingEntity listing,
            ListingReservationEntity reservation,
            OrderEntity order) {
        if (!order.getBuyerUserId().equals(buyerId)
                || !order.getListing().getId().equals(listing.getId())
                || !order.getReservation().getId().equals(reservation.getId())
                || !reservation.getListing().getId().equals(listing.getId())
                || !reservation.getBuyerUserId().equals(buyerId)
                || order.getStatus() != OrderStatus.PAID
                || order.getPaidAt() == null
                || listing.getStatus() != ListingStatus.SOLD) {
            throw ineligible();
        }
    }

    private void confirmOrRecover(UUID buyerId, UUID orderId, OrderFulfillmentEntity fulfillment) {
        if (isFirstConfirmationEligible(fulfillment)) {
            Instant now = clock.instant();
            fulfillment.setTransferStatus(TransferStatus.BUYER_CONFIRMED_RECEIPT);
            fulfillment.setBuyerConfirmedAt(now);
            fulfillment.setUpdatedAt(now);
            auditService.recordOrderAction(buyerId, orderId, AuditAction.BUYER_RECEIPT_CONFIRMED, now);
            return;
        }
        if (!isConfirmedReceipt(fulfillment)) {
            throw ineligible();
        }
    }

    private void createOperationIfAbsent(UUID orderId, OrderEntity order) {
        if (operationRepository.findByOrderIdForUpdate(orderId).isPresent()) {
            return;
        }
        Instant now = clock.instant();
        SettlementReleaseOperationEntity operation = new SettlementReleaseOperationEntity();
        operation.setOrder(order);
        operation.setProvider(settlementProvider.providerName());
        operation.setIdempotencyKey("settlement-release:" + orderId);
        operation.setStatus(SettlementReleaseOperationStatus.PENDING);
        operation.setAttemptCount(0);
        operation.setCreatedAt(now);
        operation.setUpdatedAt(now);
        operationRepository.save(operation);
    }

    private static boolean isFirstConfirmationEligible(OrderFulfillmentEntity fulfillment) {
        return fulfillment.getTransferStatus() == TransferStatus.SELLER_CONFIRMED_TRANSFER
                && fulfillment.getSellerConfirmedAt() != null
                && fulfillment.getBuyerConfirmedAt() == null
                && fulfillment.getSettlementStatus() == SettlementStatus.FUNDS_HELD
                && fulfillment.getSettlementReleasedAt() == null;
    }

    private static boolean isConfirmedReceipt(OrderFulfillmentEntity fulfillment) {
        return fulfillment.getTransferStatus() == TransferStatus.BUYER_CONFIRMED_RECEIPT
                && fulfillment.getBuyerConfirmedAt() != null
                && (fulfillment.getSettlementStatus() == SettlementStatus.FUNDS_HELD
                || fulfillment.getSettlementStatus() == SettlementStatus.RELEASED_TO_SELLER);
    }

    private static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "Order not found");
    }

    private static ApiException ineligible() {
        return new ApiException(HttpStatus.CONFLICT, "Receipt confirmation is not available");
    }
}
