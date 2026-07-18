package com.ticketpass.api.listing;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SellerOwnListingService {

    private final ListingRepository listingRepository;

    public SellerOwnListingService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Transactional(readOnly = true)
    public SellerOwnListingPageResponse getSellerListings(
            UUID sellerId,
            String rawPage,
            String rawPageSize,
            String rawStatus) {
        SellerOwnListingQueryParser query = SellerOwnListingQueryParser.parse(rawPage, rawPageSize, rawStatus);
        Page<SellerOwnListingRow> listings = query.status() == null
                ? listingRepository.findSellerListings(sellerId, query.toPageRequest())
                : listingRepository.findSellerListingsByStatus(sellerId, query.status(), query.toPageRequest());
        return SellerOwnListingPageResponse.from(listings, query);
    }
}
