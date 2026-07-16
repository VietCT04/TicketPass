package com.ticketpass.api.listing;

import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListingReservationExpirationService {

    private final ListingRepository listingRepository;
    private final ListingReservationRepository reservationRepository;
    private final Clock clock;

    public ListingReservationExpirationService(
            ListingRepository listingRepository,
            ListingReservationRepository reservationRepository,
            Clock clock) {
        this.listingRepository = listingRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    @Transactional
    public void expireReservation(ListingReservationExpirationCandidate candidate) {
        Instant now = clock.instant();
        ListingEntity listing = listingRepository.findByIdForReservation(candidate.listingId()).orElse(null);
        if (listing == null) {
            return;
        }

        ListingReservationEntity reservation = reservationRepository
                .findByListingIdAndStatus(listing.getId(), ListingReservationStatus.ACTIVE)
                .orElse(null);
        if (reservation == null
                || !reservation.getId().equals(candidate.reservationId())
                || reservation.getExpiresAt().isAfter(now)) {
            return;
        }

        reservation.setStatus(ListingReservationStatus.EXPIRED);
        reservation.setUpdatedAt(now);
        if (listing.getStatus() == ListingStatus.RESERVED) {
            listing.setStatus(ListingStatus.ACTIVE);
        }
    }
}
