package com.ticketpass.api.listing;

import com.ticketpass.api.common.ApiException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

public record SellerOwnListingQueryParser(int page, int pageSize, ListingStatus status) {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String PAGE_ERROR = "page must be an integer greater than or equal to 1";
    private static final String PAGE_SIZE_ERROR = "page_size must be an integer between 1 and 100";
    private static final String STATUS_ERROR = "status must be one of DRAFT, ACTIVE, RESERVED, SOLD, CANCELLED, EXPIRED";

    public static SellerOwnListingQueryParser parse(String rawPage, String rawPageSize, String rawStatus) {
        return new SellerOwnListingQueryParser(
                parsePage(rawPage),
                parsePageSize(rawPageSize),
                parseStatus(rawStatus));
    }

    public PageRequest toPageRequest() {
        return PageRequest.of(page - 1, pageSize);
    }

    private static int parsePage(String rawPage) {
        if (rawPage == null) {
            return DEFAULT_PAGE;
        }

        int page = parseInteger(rawPage, PAGE_ERROR);
        if (page < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, PAGE_ERROR);
        }
        return page;
    }

    private static int parsePageSize(String rawPageSize) {
        if (rawPageSize == null) {
            return DEFAULT_PAGE_SIZE;
        }

        int pageSize = parseInteger(rawPageSize, PAGE_SIZE_ERROR);
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, PAGE_SIZE_ERROR);
        }
        return pageSize;
    }

    private static ListingStatus parseStatus(String rawStatus) {
        if (rawStatus == null) {
            return null;
        }

        try {
            return ListingStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, STATUS_ERROR);
        }
    }

    private static int parseInteger(String rawValue, String message) {
        try {
            return Integer.parseInt(rawValue);
        } catch (NumberFormatException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
