package com.ticketpass.api.eventrequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketpass.api.auth.AuthenticatedUser;
import com.ticketpass.api.common.ApiException;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventRequestController {
    private static final Set<String> ALLOWED_FIELDS = Set.of("event_name", "starts_at", "venue", "city", "official_url");
    private final EventRequestService eventRequestService;
    private final ObjectMapper objectMapper;

    public EventRequestController(EventRequestService eventRequestService, ObjectMapper objectMapper) {
        this.eventRequestService = eventRequestService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/api/event-requests")
    public ResponseEntity<EventRequestResponse> createEventRequest(@AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestBody JsonNode body) {
        EventRequestCreateResult result = eventRequestService.createOrReturn(currentUser.id(), parseRequest(body));
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .header(HttpHeaders.CACHE_CONTROL, "no-store").body(EventRequestResponse.from(result.request()));
    }

    private CreateEventRequest parseRequest(JsonNode body) {
        if (body == null || !body.isObject()) throw new ApiException(HttpStatus.BAD_REQUEST, "Request body must be a JSON object");
        body.fieldNames().forEachRemaining(field -> {
            if (!ALLOWED_FIELDS.contains(field)) throw new ApiException(HttpStatus.BAD_REQUEST, field + " must not be provided");
        });
        try { return objectMapper.treeToValue(body, CreateEventRequest.class); }
        catch (JsonProcessingException exception) { throw new ApiException(HttpStatus.BAD_REQUEST, "Request validation failed"); }
    }
}
