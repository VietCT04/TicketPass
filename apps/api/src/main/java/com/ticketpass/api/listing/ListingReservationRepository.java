package com.ticketpass.api.listing;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ListingReservationRepository extends JpaRepository<ListingReservationEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select reservation from ListingReservationEntity reservation where reservation.id = :reservationId")
    Optional<ListingReservationEntity> findByIdForPayment(@Param("reservationId") UUID reservationId);

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
                and not exists (
                    select checkoutOrder
                    from OrderEntity checkoutOrder
                    where checkoutOrder.reservation.id = reservation.id
                )
            order by reservation.expiresAt asc, reservation.id asc
            """)
    List<ListingReservationExpirationCandidate> findExpirationCandidates(
            @Param("status") ListingReservationStatus status,
            @Param("now") Instant now,
            Pageable pageable);
}
