package com.ticketpass.api.listing;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
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
}

