package com.ticketpass.api.listing;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ListingReservationRepository extends JpaRepository<ListingReservationEntity, UUID> {

    Optional<ListingReservationEntity> findByListingIdAndStatus(
            UUID listingId,
            ListingReservationStatus status);

    @Query("""
            select new com.ticketpass.api.listing.ListingReservationExpirationCandidate(
                reservation.id,
                listing.id
            )
            from ListingReservationEntity reservation
            join reservation.listing listing
            where reservation.status = :status
                and reservation.expiresAt <= :now
            order by reservation.expiresAt asc, reservation.id asc
            """)
    List<ListingReservationExpirationCandidate> findExpirationCandidates(
            @Param("status") ListingReservationStatus status,
            @Param("now") Instant now,
            Pageable pageable);
}
