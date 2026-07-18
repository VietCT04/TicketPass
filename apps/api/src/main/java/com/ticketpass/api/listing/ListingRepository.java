package com.ticketpass.api.listing;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ListingRepository extends JpaRepository<ListingEntity, UUID> {

    @Query(
            value = """
                    select new com.ticketpass.api.listing.SellerOwnListingRow(
                        listing.id,
                        listing.status,
                        listing.eventPlatform,
                        listing.seatInfo,
                        listing.ticketType,
                        listing.quantity,
                        listing.askingPriceMinor,
                        listing.currency,
                        listing.transferMethod,
                        listing.transferableConfirmed,
                        listing.publicNotes,
                        listing.createdAt,
                        listing.updatedAt,
                        event.id,
                        event.name,
                        event.startsAt,
                        event.venue,
                        event.city
                    )
                    from ListingEntity listing
                    join listing.event event
                    where listing.seller.id = :sellerId
                    order by listing.createdAt desc, listing.id desc
                    """,
            countQuery = """
                    select count(listing.id)
                    from ListingEntity listing
                    join listing.event event
                    where listing.seller.id = :sellerId
                    """)
    Page<SellerOwnListingRow> findSellerListings(
            @Param("sellerId") UUID sellerId,
            Pageable pageable);

    @Query(
            value = """
                    select new com.ticketpass.api.listing.SellerOwnListingRow(
                        listing.id,
                        listing.status,
                        listing.eventPlatform,
                        listing.seatInfo,
                        listing.ticketType,
                        listing.quantity,
                        listing.askingPriceMinor,
                        listing.currency,
                        listing.transferMethod,
                        listing.transferableConfirmed,
                        listing.publicNotes,
                        listing.createdAt,
                        listing.updatedAt,
                        event.id,
                        event.name,
                        event.startsAt,
                        event.venue,
                        event.city
                    )
                    from ListingEntity listing
                    join listing.event event
                    where listing.seller.id = :sellerId
                        and listing.status = :status
                    order by listing.createdAt desc, listing.id desc
                    """,
            countQuery = """
                    select count(listing.id)
                    from ListingEntity listing
                    join listing.event event
                    where listing.seller.id = :sellerId
                        and listing.status = :status
                    """)
    Page<SellerOwnListingRow> findSellerListingsByStatus(
            @Param("sellerId") UUID sellerId,
            @Param("status") ListingStatus status,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select listing
            from ListingEntity listing
            where listing.id = :listingId
            """)
    Optional<ListingEntity> findByIdForReservation(@Param("listingId") UUID listingId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select listing from ListingEntity listing where listing.id = :listingId")
    Optional<ListingEntity> findByIdForPayment(@Param("listingId") UUID listingId);

    @Query(
            value = """
                    select new com.ticketpass.api.listing.EventListingSummaryRow(
                        listing.id,
                        listing.ticketType,
                        listing.seatInfo,
                        listing.eventPlatform,
                        listing.askingPriceMinor,
                        listing.currency,
                        listing.transferMethod
                    )
                    from ListingEntity listing
                    join listing.event event
                    where event.id = :eventId
                        and """ + PublicListingEligibility.JPQL_PREDICATE + """
                    order by listing.askingPriceMinor asc, listing.createdAt asc, listing.id asc
                    """,
            countQuery = """
                    select count(listing.id)
                    from ListingEntity listing
                    join listing.event event
                    where event.id = :eventId
                        and """ + PublicListingEligibility.JPQL_PREDICATE + """
                    """)
    Page<EventListingSummaryRow> findPublicEventListings(
            @Param("eventId") UUID eventId,
            @Param("status") ListingStatus status,
            @Param("currency") String currency,
            @Param("now") Instant now,
            Pageable pageable);
}

