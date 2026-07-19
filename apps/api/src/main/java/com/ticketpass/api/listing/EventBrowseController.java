package com.ticketpass.api.listing;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventBrowseController {

    private final EventBrowseService eventBrowseService;

    public EventBrowseController(EventBrowseService eventBrowseService) {
        this.eventBrowseService = eventBrowseService;
    }

    @GetMapping
    public EventBrowseResponse browse(
            @RequestParam(name = "page", required = false) String page,
            @RequestParam(name = "page_size", required = false) String pageSize,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "starts_from", required = false) String startsFrom,
            @RequestParam(name = "starts_before", required = false) String startsBefore) {
        return eventBrowseService.browse(page, pageSize, query, city, startsFrom, startsBefore);
    }
}
