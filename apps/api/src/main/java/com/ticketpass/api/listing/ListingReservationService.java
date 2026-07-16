package com.ticketpass.api.listing;

import com.ticketpass.api.common.ApiException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListingReservationService {

    private static final Duration RESERVATION_DURATION = Duration.ofMinutes(10);
    private static final String UNAVAILABLE_MESSAGE = "Listing is no longer available";

    private final ListingRepository listingRepository;
    private final ListingReservationRepository reservationRepository;
    private final Clock clock;

    public ListingReservationService(
            ListingRepository listingRepository,
            ListingReservationRepository reservationRepository,
            Clock clock) {
        this.listingRepository = listingRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    @Transactional
    public ListingReservationResult createReservation(UUID buyerId, String rawListingId) {
        UUID listingId = parseListingId(rawListingId);
        Instant now = clock.instant();
        ListingEntity listing = listingRepository.findByIdForReservation(listingId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Listing not found"));

        ListingReservationEntity activeReservation = reservationRepository
                .findByListingIdAndStatus(listingId, ListingReservationStatus.ACTIVE)
                .orElse(null);
        if (activeReservation != null) {
            if (!activeReservation.getExpiresAt().isAfter(now)) {
                expireReservation(activeReservation, listing, now);
            } else if (activeReservation.getBuyerUserId().equals(buyerId)) {
                return new ListingReservationResult(activeReservation, false);
            } else {
                throw unavailable();
            }
        }

        if (listing.getStatus() != ListingStatus.ACTIVE
                || !ListingEntity.MVP_CURRENCY.equals(listing.getCurrency())
                || !listing.getEvent().getStartsAt().isAfter(now)
                || listing.getSeller().getId().equals(buyerId)) {
            throw unavailable();
        }

        ListingReservationEntity reservation = new ListingReservationEntity();
        reservation.setListing(listing);
        reservation.setBuyerUserId(buyerId);
        reservation.setStatus(ListingReservationStatus.ACTIVE);
        reservation.setCreatedAt(now);
        reservation.setUpdatedAt(now);
        reservation.setExpiresAt(now.plus(RESERVATION_DURATION));
        listing.setStatus(ListingStatus.RESERVED);

        try {
            return new ListingReservationResult(reservationRepository.saveAndFlush(reservation), true);
        } catch (DataIntegrityViolationException exception) {
            throw unavailable();
        }
    }

    private static UUID parseListingId(String rawListingId) {
        try {
            return UUID.fromString(rawListingId);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "listingId must be a UUID");
        }
    }

    private void expireReservation(
            ListingReservationEntity reservation,
            ListingEntity listing,
            Instant now) {
        reservation.setStatus(ListingReservationStatus.EXPIRED);
        reservation.setUpdatedAt(now);
        if (listing.getStatus() == ListingStatus.RESERVED) {
            listing.setStatus(ListingStatus.ACTIVE);
        }
        reservationRepository.saveAndFlush(reservation);
    }

    private static ApiException unavailable() {
        return new ApiException(HttpStatus.CONFLICT, UNAVAILABLE_MESSAGE);
    }
}
