package com.ticketpass.api.listing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketpass.api.auth.AuthenticatedUser;
import com.ticketpass.api.common.ApiException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private static final Set<String> LEGACY_REJECTED_FIELDS = Set.of(
            "event_name",
            "event_venue",
            "event_city",
            "event_starts_at",
            "currency");

    private final ListingService listingService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public ListingController(
            ListingService listingService,
            ObjectMapper objectMapper,
            Validator validator) {
        this.listingService = listingService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping
    public ListingResponse createListing(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestBody JsonNode body) {
        CreateListingRequest request = parseRequest(body);
        return ListingResponse.from(listingService.createListing(currentUser.id(), request));
    }

    private CreateListingRequest parseRequest(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Request body must be a JSON object");
        }
        for (String field : LEGACY_REJECTED_FIELDS) {
            if (body.has(field)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, field + " must not be provided");
            }
        }

        CreateListingRequest request;
        try {
            request = objectMapper.treeToValue(body, CreateListingRequest.class);
        } catch (JsonProcessingException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Request validation failed");
        }

        Set<ConstraintViolation<CreateListingRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            ConstraintViolation<CreateListingRequest> violation = violations.iterator().next();
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    violation.getPropertyPath() + " " + violation.getMessage());
        }
        return request;
    }
}

