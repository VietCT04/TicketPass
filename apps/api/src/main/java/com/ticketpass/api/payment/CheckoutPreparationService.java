package com.ticketpass.api.payment;

import com.ticketpass.api.common.ApiException;
import com.ticketpass.api.listing.ListingEntity;
import com.ticketpass.api.listing.ListingRepository;
import com.ticketpass.api.listing.ListingReservationEntity;
import com.ticketpass.api.listing.ListingReservationRepository;
import com.ticketpass.api.listing.ListingReservationStatus;
import com.ticketpass.api.listing.ListingStatus;
import com.ticketpass.api.order.OrderEntity;
import com.ticketpass.api.order.OrderRepository;
import com.ticketpass.api.order.OrderStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
class CheckoutPreparationService {

    static final String UNAVAILABLE_MESSAGE = "Checkout is no longer available";

    private final ListingRepository listingRepository;
    private final ListingReservationRepository reservationRepository;
    private final OrderRepository orderRepository;
    private final PaymentSessionRepository paymentSessionRepository;
    private final PaymentProvider paymentProvider;
    private final Clock clock;

    CheckoutPreparationService(
            ListingRepository listingRepository,
            ListingReservationRepository reservationRepository,
            OrderRepository orderRepository,
            PaymentSessionRepository paymentSessionRepository,
            PaymentProvider paymentProvider,
            Clock clock) {
        this.listingRepository = listingRepository;
        this.reservationRepository = reservationRepository;
        this.orderRepository = orderRepository;
        this.paymentSessionRepository = paymentSessionRepository;
        this.paymentProvider = paymentProvider;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    CheckoutPreparation prepare(UUID buyerId, String rawReservationId) {
        UUID reservationId = parseReservationId(rawReservationId);
        Instant now = clock.instant();
        ListingReservationEntity initialReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> notFound());
        if (!initialReservation.getBuyerUserId().equals(buyerId)) {
            throw notFound();
        }

        ListingEntity listing = listingRepository.findByIdForReservation(initialReservation.getListing().getId())
                .orElseThrow(CheckoutPreparationService::unavailable);
        ListingReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(CheckoutPreparationService::unavailable);
        validateEligibility(buyerId, listing, reservation, now);

        OrderEntity order = orderRepository.findByReservationIdForCheckout(reservationId).orElse(null);
        boolean orderCreated = false;
        if (order == null) {
            order = createOrder(buyerId, listing, reservation, now);
            order = orderRepository.saveAndFlush(order);
            orderCreated = true;
        }

        if (order.getStatus() == OrderStatus.PAID) {
            return new CheckoutPreparation(order, null, orderCreated, false);
        }
        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw unavailable();
        }

        PaymentSessionEntity session = paymentSessionRepository.findByOrderIdAndStatusInForCheckout(
                        order.getId(), List.of(PaymentSessionStatus.CREATING, PaymentSessionStatus.PENDING))
                .orElse(null);
        if (session != null) {
            return new CheckoutPreparation(order, session, orderCreated, false);
        }

        session = new PaymentSessionEntity();
        session.setOrder(order);
        session.setProvider(paymentProvider.providerName());
        session.setProviderSessionId(paymentProvider.newProviderSessionId());
        session.setStatus(PaymentSessionStatus.CREATING);
        session.setExpiresAt(order.getExpiresAt());
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        session = paymentSessionRepository.saveAndFlush(session);
        return new CheckoutPreparation(order, session, orderCreated, true);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    PaymentSessionEntity persistProviderResult(UUID paymentSessionId, PaymentSessionResult result) {
        PaymentSessionEntity session = paymentSessionRepository.findById(paymentSessionId)
                .orElseThrow(PaymentProviderUnavailableException::new);
        session.setStatus(result.status());
        session.setUpdatedAt(clock.instant());
        return session;
    }

    private static OrderEntity createOrder(
            UUID buyerId,
            ListingEntity listing,
            ListingReservationEntity reservation,
            Instant now) {
        OrderEntity order = new OrderEntity();
        order.setReservation(reservation);
        order.setBuyerUserId(buyerId);
        order.setSellerUserId(listing.getSeller().getId());
        order.setListing(listing);
        order.setAmountMinor(listing.getAskingPriceMinor());
        order.setCurrency(listing.getCurrency());
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setExpiresAt(reservation.getExpiresAt());
        order.setPaidAt(null);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        return order;
    }

    private static void validateEligibility(
            UUID buyerId,
            ListingEntity listing,
            ListingReservationEntity reservation,
            Instant now) {
        if (reservation.getListing().getId().equals(listing.getId())
                && reservation.getStatus() == ListingReservationStatus.ACTIVE
                && reservation.getExpiresAt().isAfter(now)
                && listing.getStatus() == ListingStatus.RESERVED
                && !listing.getSeller().getId().equals(buyerId)
                && listing.getAskingPriceMinor() > 0
                && ListingEntity.MVP_CURRENCY.equals(listing.getCurrency())) {
            return;
        }
        throw unavailable();
    }

    private static UUID parseReservationId(String rawReservationId) {
        try {
            return UUID.fromString(rawReservationId);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Reservation not found");
        }
    }

    private static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "Reservation not found");
    }

    private static ApiException unavailable() {
        return new ApiException(HttpStatus.CONFLICT, UNAVAILABLE_MESSAGE);
    }
}
