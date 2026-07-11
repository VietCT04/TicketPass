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
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

class ListingServiceTest {

    private UserRepository userRepository;
    private EventRepository eventRepository;
    private ListingRepository listingRepository;
    private ListingService listingService;

    @BeforeEach
    void setUp() {
        userRepository = org.mockito.Mockito.mock(UserRepository.class);
        eventRepository = org.mockito.Mockito.mock(EventRepository.class);
        listingRepository = org.mockito.Mockito.mock(ListingRepository.class);
        listingService = new ListingService(userRepository, eventRepository, listingRepository);
    }

    @Test
    void createListingPersistsEventAndListingWithServerDerivedSeller() {
        UUID sellerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UserEntity seller = seller(sellerId);
        when(userRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(eventRepository.save(any(EventEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(listingRepository.save(any(ListingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ListingEntity listing = listingService.createListing(sellerId, request());

        ArgumentCaptor<EventEntity> eventCaptor = ArgumentCaptor.forClass(EventEntity.class);
        verify(eventRepository).save(eventCaptor.capture());
        EventEntity event = eventCaptor.getValue();
        assertThat(event.getName()).isEqualTo("Example Concert");
        assertThat(event.getVenue()).isEqualTo("Example Arena");
        assertThat(event.getCity()).isEqualTo("Singapore");
        assertThat(event.getStartsAt()).isEqualTo(Instant.parse("2026-08-15T11:30:00Z"));
        assertThat(event.getEventPlatform()).isEqualTo("Ticketmaster");

        ArgumentCaptor<ListingEntity> listingCaptor = ArgumentCaptor.forClass(ListingEntity.class);
        verify(listingRepository).save(listingCaptor.capture());
        ListingEntity savedListing = listingCaptor.getValue();
        assertThat(savedListing).isSameAs(listing);
        assertThat(savedListing.getSeller()).isSameAs(seller);
        assertThat(savedListing.getQuantity()).isEqualTo(1);
        assertThat(savedListing.getCurrency()).isEqualTo("SGD");
        assertThat(savedListing.getAskingPriceMinor()).isEqualTo(12500);
        assertThat(savedListing.getTransferMethod()).isEqualTo(TransferMethod.PLATFORM_TRANSFER);
        assertThat(savedListing.isTransferableConfirmed()).isTrue();
        assertThat(savedListing.getStatus()).isEqualTo(ListingStatus.ACTIVE);
        assertThat(savedListing.getPublicNotes()).isEqualTo("Mobile transfer available after purchase.");
    }

    @Test
    void createListingRejectsUnconfirmedTransferability() {
        UUID sellerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        CreateListingRequest request = new CreateListingRequest(
                "Example Concert",
                "Example Arena",
                "Singapore",
                Instant.parse("2026-08-15T11:30:00Z"),
                "Ticketmaster",
                "Section 101, Row B, Seat 12",
                "General Admission",
                "SGD",
                12500,
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
    void createListingRejectsInvalidCurrency() {
        UUID sellerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(userRepository.findById(sellerId)).thenReturn(Optional.of(seller(sellerId)));
        CreateListingRequest request = new CreateListingRequest(
                "Example Concert",
                "Example Arena",
                "Singapore",
                Instant.parse("2026-08-15T11:30:00Z"),
                "Ticketmaster",
                "Section 101, Row B, Seat 12",
                "General Admission",
                "BAD",
                12500,
                TransferMethod.PLATFORM_TRANSFER,
                true,
                null);

        assertThatThrownBy(() -> listingService.createListing(sellerId, request))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        verify(listingRepository, never()).save(any());
    }

    private static CreateListingRequest request() {
        return new CreateListingRequest(
                "  Example Concert  ",
                "  Example Arena  ",
                "  Singapore  ",
                Instant.parse("2026-08-15T11:30:00Z"),
                "  Ticketmaster  ",
                "  Section 101, Row B, Seat 12  ",
                "  General Admission  ",
                "sgd",
                12500,
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
}

