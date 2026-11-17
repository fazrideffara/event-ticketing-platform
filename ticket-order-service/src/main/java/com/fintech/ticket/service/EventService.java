package com.fintech.ticket.service;

import com.fintech.ticket.dto.response.EventDetailResponse;

import java.util.List;
import java.util.UUID;

public interface EventService {
    List<EventDetailResponse> getAllPublishedEvents();
    EventDetailResponse getEventById(UUID eventId);
    void evictEventCache(UUID eventId);
}
