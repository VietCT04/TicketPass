package com.ticketpass.api.eventrequest;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRequestRepository extends JpaRepository<EventRequestEntity, UUID> {
    @Query("""
            select request from EventRequestEntity request
            where request.requesterUserId = :requesterUserId
                and request.normalizedEventName = :normalizedEventName
                and request.startsAt = :startsAt
                and request.normalizedVenue = :normalizedVenue
                and request.normalizedCity = :normalizedCity
                and request.status = com.ticketpass.api.eventrequest.EventRequestStatus.PENDING
            """)
    Optional<EventRequestEntity> findPendingDuplicate(
            @Param("requesterUserId") UUID requesterUserId,
            @Param("normalizedEventName") String normalizedEventName,
            @Param("startsAt") Instant startsAt,
            @Param("normalizedVenue") String normalizedVenue,
            @Param("normalizedCity") String normalizedCity);
}
