package com.ticketpass.api.listing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ticketpass.api.common.ApiException;
import com.ticketpass.api.user.UserEntity;
import com.ticketpass.api.user.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

class ListingServiceTest {

    private UserRepository userRepository;
    private EventRepository eventRepository;
    private ListingRepository listingRepository;
    private ListingService listingService;
    private Clock clock;

    @BeforeEach
    void setUp() {
        userRepository = org.mockito.Mockito.mock(UserRepository.class);
        eventRepository = org.mockito.Mockito.mock(EventRepository.class);
        listingRepository = org.mockito.Mockito.mock(ListingRepository.class);
        clock = Clock.fixed(Instant.parse("2026-07-11T10:00:00Z"), ZoneOffset.UTC);
        listingService = new ListingService(userRepository, eventRepository, listingRepository, clock);
    }

    @Test
    void createListingPersistsListingWithExistingEventAndServerDerivedSeller() {
        UUID sellerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID eventId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UserEntity seller = seller(sellerId);
        EventEntity event = event(eventId);
        when(userRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(listingRepository.save(any(ListingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ListingEntity listing = listingService.createListing(sellerId, request());

        verify(eventRepository, never()).save(any(EventEntity.class));
        assertThat(listing.getSeller()).isSameAs(seller);
        assertThat(listing.getEvent()).isSameAs(event);
        assertThat(listing.getEventPlatform()).isEqualTo("Ticketmaster");
        assertThat(listing.getQuantity()).isEqualTo(1);
        assertThat(listing.getCurrency()).isEqualTo("VND");
        assertThat(listing.getAskingPriceMinor()).isEqualTo(1250000);
        assertThat(listing.getTransferMethod()).isEqualTo(TransferMethod.PLATFORM_TRANSFER);
        assertThat(listing.isTransferableConfirmed()).isTrue();
        assertThat(listing.getStatus()).isEqualTo(ListingStatus.ACTIVE);
        assertThat(listing.getPublicNotes()).isEqualTo("Mobile transfer available after purchase.");
    }

    @Test
    void createListingRejectsUnconfirmedTransferability() {
        UUID sellerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        CreateListingRequest request = new CreateListingRequest(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "Ticketmaster",
                "Section 101, Row B, Seat 12",
                "General Admission",
                1250000,
                TransferMethod.PLATFORM_TRANSFER,
                false,
                null);

        assertThatThrownBy(() -> listingService.createListing(sellerId, request))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        verify(listingRepository, never()).save(any());
    }

    @Test
    void createListingRejectsPastEvent() {
        UUID sellerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID eventId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(userRepository.findById(sellerId)).thenReturn(Optional.of(seller(sellerId)));
        EventEntity event = event(eventId);
        event.setStartsAt(Instant.parse("2026-07-11T10:00:00Z"));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> listingService.createListing(sellerId, request()))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.CONFLICT);
        verify(listingRepository, never()).save(any());
    }

    private static CreateListingRequest request() {
        return new CreateListingRequest(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "  Ticketmaster  ",
                "  Section 101, Row B, Seat 12  ",
                "  General Admission  ",
                1250000,
                TransferMethod.PLATFORM_TRANSFER,
                true,
                "  Mobile transfer available after purchase.  ");
    }

    private static UserEntity seller(UUID sellerId) {
        UserEntity seller = new UserEntity();
        ReflectionTestUtils.setField(seller, "id", sellerId);
        seller.setEmail("seller@example.com");
        seller.setDisplayName("Seller");
        return seller;
    }

    private static EventEntity event(UUID eventId) {
        EventEntity event = new EventEntity();
        ReflectionTestUtils.setField(event, "id", eventId);
        event.setName("Example Concert");
        event.setVenue("Example Arena");
        event.setCity("Singapore");
        event.setStartsAt(Instant.parse("2026-08-15T11:30:00Z"));
        return event;
    }
}

