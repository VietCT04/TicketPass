package com.ticketpass.api.eventrequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "event_requests")
public class EventRequestEntity {
    @Id private UUID id;
    @Column(name = "requester_user_id", nullable = false, updatable = false) private UUID requesterUserId;
    @Column(name = "event_name", nullable = false, length = 255) private String eventName;
    @Column(name = "normalized_event_name", nullable = false, length = 255) private String normalizedEventName;
    @Column(name = "starts_at", nullable = false) private Instant startsAt;
    @Column(nullable = false, length = 255) private String venue;
    @Column(name = "normalized_venue", nullable = false, length = 255) private String normalizedVenue;
    @Column(nullable = false, length = 120) private String city;
    @Column(name = "normalized_city", nullable = false, length = 120) private String normalizedCity;
    @Column(name = "official_url", length = 2048) private String officialUrl;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private EventRequestStatus status;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public void setRequesterUserId(UUID requesterUserId) { this.requesterUserId = requesterUserId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public void setNormalizedEventName(String normalizedEventName) { this.normalizedEventName = normalizedEventName; }
    public Instant getStartsAt() { return startsAt; }
    public void setStartsAt(Instant startsAt) { this.startsAt = startsAt; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public void setNormalizedVenue(String normalizedVenue) { this.normalizedVenue = normalizedVenue; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public void setNormalizedCity(String normalizedCity) { this.normalizedCity = normalizedCity; }
    public String getOfficialUrl() { return officialUrl; }
    public void setOfficialUrl(String officialUrl) { this.officialUrl = officialUrl; }
    public EventRequestStatus getStatus() { return status; }
    public void setStatus(EventRequestStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
