package com.ticketpass.api.listing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketpass.api.auth.AuthenticatedUser;
import com.ticketpass.api.user.UserEntity;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ListingControllerTest {

    @Test
    void createListingPassesAuthenticatedUserIdToService() throws Exception {
        ListingService listingService = org.mockito.Mockito.mock(ListingService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        ListingController controller = new ListingController(listingService, objectMapper, validator);
        AuthenticatedUser currentUser = new AuthenticatedUser(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "seller@example.com",
                "Seller",
                Instant.parse("2026-07-10T10:00:00Z"));
        CreateListingRequest request = request();
        JsonNode body = objectMapper.readTree("""
                {
                  "event_id": "22222222-2222-2222-2222-222222222222",
                  "event_platform": "Ticketmaster",
                  "seat_info": "Section 101, Row B, Seat 12",
                  "ticket_type": "General Admission",
                  "asking_price_minor": 1250000,
                  "transfer_method": "PLATFORM_TRANSFER",
                  "is_transferable_confirmed": true,
                  "public_notes": "Mobile transfer available after purchase."
                }
                """);
        ListingEntity listing = listing(currentUser.id());
        when(listingService.createListing(eq(currentUser.id()), eq(request))).thenReturn(listing);

        ListingResponse response = controller.createListing(currentUser, body);

        verify(listingService).createListing(eq(currentUser.id()), eq(request));
        assertThat(response.sellerId()).isEqualTo(currentUser.id().toString());
        assertThat(response.quantity()).isEqualTo(1);
        assertThat(response.status()).isEqualTo(ListingStatus.ACTIVE);
    }

    private static CreateListingRequest request() {
        return new CreateListingRequest(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "Ticketmaster",
                "Section 101, Row B, Seat 12",
                "General Admission",
                1250000,
                TransferMethod.PLATFORM_TRANSFER,
                true,
                "Mobile transfer available after purchase.");
    }

    private static ListingEntity listing(UUID sellerId) {
        UserEntity seller = new UserEntity();
        ReflectionTestUtils.setField(seller, "id", sellerId);
        seller.setEmail("seller@example.com");
        seller.setDisplayName("Seller");

        EventEntity event = new EventEntity();
        ReflectionTestUtils.setField(event, "id", UUID.fromString("22222222-2222-2222-2222-222222222222"));
        event.setName("Example Concert");
        event.setVenue("Example Arena");
        event.setCity("Singapore");
        event.setStartsAt(Instant.parse("2026-08-15T11:30:00Z"));

        ListingEntity listing = new ListingEntity();
        ReflectionTestUtils.setField(listing, "id", UUID.fromString("33333333-3333-3333-3333-333333333333"));
        ReflectionTestUtils.setField(listing, "createdAt", Instant.parse("2026-07-11T10:00:00Z"));
        ReflectionTestUtils.setField(listing, "updatedAt", Instant.parse("2026-07-11T10:00:00Z"));
        listing.setSeller(seller);
        listing.setEvent(event);
        listing.setEventPlatform("Ticketmaster");
        listing.setSeatInfo("Section 101, Row B, Seat 12");
        listing.setTicketType("General Admission");
        listing.setQuantity(1);
        listing.setCurrency("VND");
        listing.setAskingPriceMinor(1250000);
        listing.setTransferMethod(TransferMethod.PLATFORM_TRANSFER);
        listing.setTransferableConfirmed(true);
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setPublicNotes("Mobile transfer available after purchase.");
        return listing;
    }
}

