package com.ticketpass.api.order;
import com.ticketpass.api.audit.*;
import com.ticketpass.api.common.ApiException;
import com.ticketpass.api.listing.*;
import com.ticketpass.api.settlement.SettlementProvider;
import java.time.*;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuyerReceiptConfirmationCommand {
    private final OrderRepository orders; private final ListingRepository listings; private final ListingReservationRepository reservations;
    private final OrderFulfillmentRepository fulfillments; private final SettlementReleaseOperationRepository operations;
    private final SettlementProvider provider; private final AuditService audit; private final Clock clock;
    public BuyerReceiptConfirmationCommand(OrderRepository orders, ListingRepository listings, ListingReservationRepository reservations, OrderFulfillmentRepository fulfillments, SettlementReleaseOperationRepository operations, SettlementProvider provider, AuditService audit, Clock clock) { this.orders=orders; this.listings=listings; this.reservations=reservations; this.fulfillments=fulfillments; this.operations=operations; this.provider=provider; this.audit=audit; this.clock=clock; }
    @Transactional
    public UUID accept(UUID buyerId, UUID orderId) {
        BuyerReceiptLockTarget target=orders.findBuyerReceiptLockTarget(orderId,buyerId).orElseThrow(this::notFound);
        ListingEntity listing=listings.findByIdForPayment(target.listingId()).orElseThrow(this::notFound);
        ListingReservationEntity reservation=reservations.findByIdForPayment(target.reservationId()).orElseThrow(this::notFound);
        OrderEntity order=orders.findByIdForPayment(orderId).orElseThrow(this::notFound);
        OrderFulfillmentEntity fulfillment=fulfillments.findByOrderIdForUpdate(orderId).orElseThrow(this::ineligible);
        if (!order.getBuyerUserId().equals(buyerId) || !order.getListing().getId().equals(listing.getId()) || !order.getReservation().getId().equals(reservation.getId()) || !reservation.getListing().getId().equals(listing.getId()) || !reservation.getBuyerUserId().equals(buyerId) || order.getStatus()!=OrderStatus.PAID || order.getPaidAt()==null || listing.getStatus()!=ListingStatus.SOLD) throw ineligible();
        if (fulfillment.getTransferStatus()==TransferStatus.SELLER_CONFIRMED_TRANSFER && fulfillment.getSellerConfirmedAt()!=null && fulfillment.getBuyerConfirmedAt()==null && fulfillment.getSettlementStatus()==SettlementStatus.FUNDS_HELD && fulfillment.getSettlementReleasedAt()==null) {
            Instant now=clock.instant(); fulfillment.setTransferStatus(TransferStatus.BUYER_CONFIRMED_RECEIPT); fulfillment.setBuyerConfirmedAt(now); fulfillment.setUpdatedAt(now); audit.recordOrderAction(buyerId,orderId,AuditAction.BUYER_RECEIPT_CONFIRMED,now);
        } else if (fulfillment.getTransferStatus()!=TransferStatus.BUYER_CONFIRMED_RECEIPT || fulfillment.getBuyerConfirmedAt()==null || (fulfillment.getSettlementStatus()!=SettlementStatus.FUNDS_HELD && fulfillment.getSettlementStatus()!=SettlementStatus.RELEASED_TO_SELLER)) throw ineligible();
        SettlementReleaseOperationEntity operation=operations.findByOrderIdForUpdate(orderId).orElse(null);
        if (operation==null) { Instant now=clock.instant(); operation=new SettlementReleaseOperationEntity(); operation.setOrder(order); operation.setProvider(provider.providerName()); operation.setIdempotencyKey("settlement-release:"+orderId); operation.setStatus(SettlementReleaseOperationStatus.PENDING); operation.setAttemptCount(0); operation.setCreatedAt(now); operation.setUpdatedAt(now); operations.save(operation); }
        return orderId;
    }
    private ApiException notFound(){return new ApiException(HttpStatus.NOT_FOUND,"Order not found");} private ApiException ineligible(){return new ApiException(HttpStatus.CONFLICT,"Receipt confirmation is not available");}
}
