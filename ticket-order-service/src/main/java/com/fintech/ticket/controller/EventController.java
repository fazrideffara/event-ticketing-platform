package com.fintech.ticket.controller;

import com.fintech.ticket.common.constant.ApiUrlConstant;
import com.fintech.ticket.common.constant.AppConstant;
import com.fintech.ticket.common.response.ApiResponse;
import com.fintech.ticket.dto.response.EventDetailResponse;
import com.fintech.ticket.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiUrlConstant.EVENTS_BASE)
@RequiredArgsConstructor
@Tag(name = "Event Catalog Management", description = "Endpoints for browsing concert events, ticket tiers, and remaining quota")
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "List all published events", description = "Retrieves all currently active and published concert events")
    public ResponseEntity<ApiResponse<List<EventDetailResponse>>> getAllEvents() {
        List<EventDetailResponse> events = eventService.getAllPublishedEvents();
        return ResponseEntity.ok(ApiResponse.ok(AppConstant.MSG_EVENT_FOUND, events));
    }

    @GetMapping(ApiUrlConstant.EVENTS_ID)
    @Operation(summary = "Get event details by ID", description = "Fetches complete event details and available ticket tier quotas with Redis caching")
    public ResponseEntity<ApiResponse<EventDetailResponse>> getEventById(@PathVariable UUID eventId) {
        EventDetailResponse response = eventService.getEventById(eventId);
        return ResponseEntity.ok(ApiResponse.ok(AppConstant.MSG_EVENT_FOUND, response));
    }
}
