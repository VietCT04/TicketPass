package com.ticketpass.api.listing;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ListingReservationExpirationScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListingReservationExpirationScheduler.class);
    private static final int BATCH_SIZE = 100;

    private final ListingReservationRepository reservationRepository;
    private final ListingReservationExpirationService expirationService;
    private final Clock clock;

    public ListingReservationExpirationScheduler(
            ListingReservationRepository reservationRepository,
            ListingReservationExpirationService expirationService,
            Clock clock) {
        this.reservationRepository = reservationRepository;
        this.expirationService = expirationService;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${ticketpass.reservations.expiration-interval-ms:60000}")
    public void expireReservations() {
        Instant now = clock.instant();
        List<ListingReservationExpirationCandidate> candidates = reservationRepository.findExpirationCandidates(
                ListingReservationStatus.ACTIVE,
                now,
                PageRequest.of(0, BATCH_SIZE));
        for (ListingReservationExpirationCandidate candidate : candidates) {
            try {
                expirationService.expireReservation(candidate);
            } catch (RuntimeException exception) {
                LOGGER.error(
                        "Unable to expire listing reservation {} for listing {}",
                        candidate.reservationId(),
                        candidate.listingId(),
                        exception);
            }
        }
    }
}
