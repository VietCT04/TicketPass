package com.ticketpass.api.listing;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventDetailController {

    private final EventDetailService eventDetailService;

    public EventDetailController(EventDetailService eventDetailService) {
        this.eventDetailService = eventDetailService;
    }

    @GetMapping("/{eventId}")
    public EventDetailResponse getEventDetail(
            @PathVariable String eventId,
            @RequestParam(name = "page", required = false) String page,
            @RequestParam(name = "page_size", required = false) String pageSize) {
        return eventDetailService.getEventDetail(eventId, page, pageSize);
    }
}
