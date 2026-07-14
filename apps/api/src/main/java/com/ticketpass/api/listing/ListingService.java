package com.ticketpass.api.listing;

import com.ticketpass.api.common.ApiException;
import com.ticketpass.api.user.UserEntity;
import com.ticketpass.api.user.UserRepository;
import java.time.Clock;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListingService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ListingRepository listingRepository;
    private final Clock clock;

    public ListingService(
            UserRepository userRepository,
            EventRepository eventRepository,
            ListingRepository listingRepository,
            Clock clock) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.listingRepository = listingRepository;
        this.clock = clock;
    }

    @Transactional
    public ListingEntity createListing(UUID sellerId, CreateListingRequest request) {
        if (!request.transferableConfirmed()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ticket transferability must be confirmed");
        }

        UserEntity seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required"));

        EventEntity event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found"));
        if (!event.getStartsAt().isAfter(clock.instant())) {
            throw new ApiException(HttpStatus.CONFLICT, "Event has already started");
        }

        ListingEntity listing = new ListingEntity();
        listing.setSeller(seller);
        listing.setEvent(event);
        listing.setEventPlatform(trim(request.eventPlatform()));
        listing.setSeatInfo(trim(request.seatInfo()));
        listing.setTicketType(trim(request.ticketType()));
        listing.setQuantity(ListingEntity.MVP_QUANTITY);
        listing.setCurrency(ListingEntity.MVP_CURRENCY);
        listing.setAskingPriceMinor(request.askingPriceMinor());
        listing.setTransferMethod(request.transferMethod());
        listing.setTransferableConfirmed(true);
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setPublicNotes(optionalTrim(request.publicNotes()));
        return listingRepository.save(listing);
    }

    private static String trim(String value) {
        return value.trim();
    }

    private static String optionalTrim(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

