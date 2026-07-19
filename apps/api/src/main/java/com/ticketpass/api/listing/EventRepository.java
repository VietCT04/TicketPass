package com.ticketpass.api.listing;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<EventEntity, UUID> {

    @Query("""
            select event
            from EventEntity event
            where event.startsAt > :now
                and (
                    lower(event.name) like concat('%', :query, '%')
                    or lower(event.venue) like concat('%', :query, '%')
                    or lower(event.city) like concat('%', :query, '%')
                )
            order by
                case
                    when lower(event.name) = :query then 1
                    when lower(event.name) like concat(:query, '%') then 2
                    when lower(event.name) like concat('%', :query, '%') then 3
                    else 4
                end,
                event.startsAt asc,
                event.id asc
            """)
    List<EventEntity> searchAutocomplete(
            @Param("query") String query,
            @Param("now") Instant now,
            Pageable pageable);

    @Query(
            value = """
                    select new com.ticketpass.api.listing.EventBrowseRow(
                        event.id,
                        event.name,
                        event.startsAt,
                        event.venue,
                        event.city,
                        min(listing.askingPriceMinor),
                        count(listing.id)
                    )
                    from ListingEntity listing
                    join listing.event event
                    where """ + PublicListingEligibility.JPQL_PREDICATE + """
                        and (
                            :queryPattern is null
                            or lower(event.name) like :queryPattern escape '!'
                            or lower(event.venue) like :queryPattern escape '!'
                            or lower(event.city) like :queryPattern escape '!'
                        )
                        and (:city is null or lower(event.city) = :city)
                        and (:startsFrom is null or event.startsAt >= :startsFrom)
                        and (:startsBefore is null or event.startsAt < :startsBefore)
                    group by event.id, event.name, event.startsAt, event.venue, event.city
                    order by event.startsAt asc, event.id asc
                    """,
            countQuery = """
                    select count(distinct event.id)
                    from ListingEntity listing
                    join listing.event event
                    where """ + PublicListingEligibility.JPQL_PREDICATE + """
                        and (
                            :queryPattern is null
                            or lower(event.name) like :queryPattern escape '!'
                            or lower(event.venue) like :queryPattern escape '!'
                            or lower(event.city) like :queryPattern escape '!'
                        )
                        and (:city is null or lower(event.city) = :city)
                        and (:startsFrom is null or event.startsAt >= :startsFrom)
                        and (:startsBefore is null or event.startsAt < :startsBefore)
                    """)
    Page<EventBrowseRow> browsePublicEvents(
            @Param("status") ListingStatus status,
            @Param("currency") String currency,
            @Param("now") Instant now,
            @Param("queryPattern") String queryPattern,
            @Param("city") String city,
            @Param("startsFrom") Instant startsFrom,
            @Param("startsBefore") Instant startsBefore,
            Pageable pageable);

    @Query("""
            select event
            from EventEntity event
            where event.id = :id
                and event.startsAt > :now
            """)
    Optional<EventEntity> findPublicUpcomingEventById(
            @Param("id") UUID id,
            @Param("now") Instant now);
}

