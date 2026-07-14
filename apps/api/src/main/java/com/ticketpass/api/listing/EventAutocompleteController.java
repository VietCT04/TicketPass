package com.ticketpass.api.listing;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventAutocompleteController {

    private final EventAutocompleteService eventAutocompleteService;

    public EventAutocompleteController(EventAutocompleteService eventAutocompleteService) {
        this.eventAutocompleteService = eventAutocompleteService;
    }

    @GetMapping("/autocomplete")
    public EventAutocompleteResponse autocomplete(@RequestParam(name = "q", required = false) String query) {
        return eventAutocompleteService.autocomplete(query);
    }
}
