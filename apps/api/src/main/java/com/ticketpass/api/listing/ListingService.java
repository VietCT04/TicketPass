package com.ticketpass.api.listing;

import com.ticketpass.api.common.ApiException;
import com.ticketpass.api.user.UserEntity;
import com.ticketpass.api.user.UserRepository;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListingService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ListingRepository listingRepository;

    public ListingService(
            UserRepository userRepository,
            EventRepository eventRepository,
            ListingRepository listingRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.listingRepository = listingRepository;
    }

    @Transactional
    public ListingEntity createListing(UUID sellerId, CreateListingRequest request) {
        if (!request.transferableConfirmed()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ticket transferability must be confirmed");
        }

        UserEntity seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required"));

        EventEntity event = new EventEntity();
        event.setName(trim(request.eventName()));
        event.setVenue(trim(request.eventVenue()));
        event.setCity(trim(request.eventCity()));
        event.setStartsAt(request.eventStartsAt());
        event.setEventPlatform(trim(request.eventPlatform()));
        EventEntity savedEvent = eventRepository.save(event);

        ListingEntity listing = new ListingEntity();
        listing.setSeller(seller);
        listing.setEvent(savedEvent);
        listing.setSeatInfo(trim(request.seatInfo()));
        listing.setTicketType(trim(request.ticketType()));
        listing.setQuantity(ListingEntity.MVP_QUANTITY);
        listing.setCurrency(normalizeCurrency(request.currency()));
        listing.setAskingPriceMinor(request.askingPriceMinor());
        listing.setTransferMethod(request.transferMethod());
        listing.setTransferableConfirmed(true);
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setPublicNotes(optionalTrim(request.publicNotes()));
        return listingRepository.save(listing);
    }

    private static String normalizeCurrency(String value) {
        String currency = trim(value).toUpperCase(Locale.ROOT);
        try {
            Currency.getInstance(currency);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "currency must be a valid ISO-4217 code");
        }
        return currency;
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

