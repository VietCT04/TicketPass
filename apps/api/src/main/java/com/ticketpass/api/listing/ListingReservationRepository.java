package com.ticketpass.api.listing;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingReservationRepository extends JpaRepository<ListingReservationEntity, UUID> {

    Optional<ListingReservationEntity> findByListingIdAndStatus(
            UUID listingId,
            ListingReservationStatus status);
}
