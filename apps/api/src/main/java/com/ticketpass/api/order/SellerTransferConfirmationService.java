package com.ticketpass.api.order;

import com.ticketpass.api.common.ApiException;
import com.ticketpass.api.listing.ListingEntity;
import com.ticketpass.api.listing.ListingRepository;
import com.ticketpass.api.listing.ListingReservationEntity;
import com.ticketpass.api.listing.ListingReservationRepository;
import com.ticketpass.api.listing.ListingStatus;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SellerTransferConfirmationService {

    private static final Duration TRANSFER_WINDOW = Duration.ofMinutes(15);
    private static final String NOT_FOUND_MESSAGE = "Order not found";
    private static final String INELIGIBLE_MESSAGE = "Transfer confirmation is not available";
    private static final String INTEGRITY_MESSAGE = "Transfer confirmation could not be completed";

    private final OrderRepository orderRepository;
    private final ListingRepository listingRepository;
    private final ListingReservationRepository reservationRepository;
    private final OrderFulfillmentRepository fulfillmentRepository;
    private final Clock clock;

    public SellerTransferConfirmationService(
            OrderRepository orderRepository,
            ListingRepository listingRepository,
            ListingReservationRepository reservationRepository,
            OrderFulfillmentRepository fulfillmentRepository,
            Clock clock) {
        this.orderRepository = orderRepository;
        this.listingRepository = listingRepository;
        this.reservationRepository = reservationRepository;
        this.fulfillmentRepository = fulfillmentRepository;
        this.clock = clock;
    }

    @Transactional
    public SellerTransferConfirmationResponse confirm(UUID sellerId, String rawOrderId) {
        UUID orderId = parseOrderId(rawOrderId);
        SellerTransferLockTarget target = orderRepository.findSellerTransferLockTarget(orderId, sellerId)
                .orElseThrow(SellerTransferConfirmationService::notFound);

        ListingEntity listing = listingRepository.findByIdForPayment(target.listingId())
                .orElseThrow(SellerTransferConfirmationService::notFound);
        ListingReservationEntity reservation = reservationRepository.findByIdForPayment(target.reservationId())
                .orElseThrow(SellerTransferConfirmationService::notFound);
        OrderEntity order = orderRepository.findByIdForPayment(orderId)
                .orElseThrow(SellerTransferConfirmationService::notFound);
        OrderFulfillmentEntity fulfillment = fulfillmentRepository.findByOrderIdForUpdate(orderId)
                .orElseThrow(SellerTransferConfirmationService::ineligible);

        validateRelationships(sellerId, target, listing, reservation, order, fulfillment);
        Instant now = clock.instant();
        validateFulfillment(order, fulfillment);

        if (fulfillment.getTransferStatus() == TransferStatus.AWAITING_SELLER_TRANSFER) {
            if (!now.isBefore(fulfillment.getTransferDeadlineAt())) {
                throw ineligible();
            }
            fulfillment.setTransferStatus(TransferStatus.SELLER_CONFIRMED_TRANSFER);
            fulfillment.setSellerConfirmedAt(now);
            fulfillment.setUpdatedAt(now);
        } else if (!isIdempotentSuccess(fulfillment)) {
            throw ineligible();
        }

        return response(order, listing, fulfillment);
    }

    private static void validateRelationships(
            UUID sellerId,
            SellerTransferLockTarget target,
            ListingEntity listing,
            ListingReservationEntity reservation,
            OrderEntity order,
            OrderFulfillmentEntity fulfillment) {
        if (!listing.getId().equals(target.listingId())
                || !reservation.getId().equals(target.reservationId())
                || !order.getListing().getId().equals(listing.getId())
                || !order.getReservation().getId().equals(reservation.getId())
                || !reservation.getListing().getId().equals(listing.getId())
                || !order.getSellerUserId().equals(sellerId)
                || !listing.getSeller().getId().equals(sellerId)
                || !order.getBuyerUserId().equals(reservation.getBuyerUserId())
                || !fulfillment.getOrderId().equals(order.getId())) {
            throw integrity();
        }
        if (order.getStatus() != OrderStatus.PAID
                || order.getPaidAt() == null
                || listing.getStatus() != ListingStatus.SOLD) {
            throw ineligible();
        }
    }

    private static void validateFulfillment(OrderEntity order, OrderFulfillmentEntity fulfillment) {
        Instant deadline = order.getPaidAt().plus(TRANSFER_WINDOW);
        if (!deadline.equals(fulfillment.getTransferDeadlineAt())
                || !order.getPaidAt().equals(fulfillment.getCreatedAt())
                || fulfillment.getUpdatedAt().isBefore(fulfillment.getCreatedAt())) {
            throw integrity();
        }
        if (fulfillment.getTransferStatus() == TransferStatus.AWAITING_SELLER_TRANSFER) {
            if (fulfillment.getSettlementStatus() != SettlementStatus.FUNDS_HELD
                    || fulfillment.getSellerConfirmedAt() != null) {
                throw integrity();
            }
            return;
        }
        if (fulfillment.getTransferStatus() == TransferStatus.SELLER_CONFIRMED_TRANSFER
                && (fulfillment.getSettlementStatus() != SettlementStatus.FUNDS_HELD
                || fulfillment.getSellerConfirmedAt() == null
                || !fulfillment.getSellerConfirmedAt().isBefore(fulfillment.getTransferDeadlineAt()))) {
            throw integrity();
        }
        if (fulfillment.getTransferStatus() == TransferStatus.BUYER_CONFIRMED_RECEIPT
                && fulfillment.getBuyerConfirmedAt() == null) {
            throw integrity();
        }
        if (fulfillment.getSettlementStatus() == SettlementStatus.RELEASED_TO_SELLER
                && fulfillment.getSettlementReleasedAt() == null) {
            throw integrity();
        }
    }

    private static boolean isIdempotentSuccess(OrderFulfillmentEntity fulfillment) {
        return fulfillment.getTransferStatus() == TransferStatus.SELLER_CONFIRMED_TRANSFER
                || fulfillment.getTransferStatus() == TransferStatus.BUYER_CONFIRMED_RECEIPT
                || fulfillment.getSettlementStatus() == SettlementStatus.RELEASED_TO_SELLER;
    }

    private static SellerTransferConfirmationResponse response(
            OrderEntity order,
            ListingEntity listing,
            OrderFulfillmentEntity fulfillment) {
        return new SellerTransferConfirmationResponse(
                order.getId().toString(),
                order.getStatus().name(),
                fulfillment.getTransferStatus().name(),
                fulfillment.getSettlementStatus().name(),
                order.getPaidAt(),
                fulfillment.getTransferDeadlineAt(),
                fulfillment.getSellerConfirmedAt(),
                new SellerTransferConfirmationResponse.Event(
                        listing.getEvent().getName(),
                        listing.getEvent().getStartsAt(),
                        listing.getEvent().getVenue(),
                        listing.getEvent().getCity()),
                new SellerTransferConfirmationResponse.Ticket(
                        listing.getTicketType(),
                        listing.getSeatInfo(),
                        listing.getTransferMethod().name()));
    }

    private static UUID parseOrderId(String rawOrderId) {
        try {
            return UUID.fromString(rawOrderId);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "orderId must be a UUID");
        }
    }

    private static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, NOT_FOUND_MESSAGE);
    }

    private static ApiException ineligible() {
        return new ApiException(HttpStatus.CONFLICT, INELIGIBLE_MESSAGE);
    }

    private static ApiException integrity() {
        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, INTEGRITY_MESSAGE);
    }
}
