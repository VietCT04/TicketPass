package com.ticketpass.api.listing;

final class PublicListingEligibility {

    static final String JPQL_PREDICATE = """
            listing.status = :status
                and listing.currency = :currency
                and event.startsAt > :now
            """;

    private PublicListingEligibility() {
    }
}
