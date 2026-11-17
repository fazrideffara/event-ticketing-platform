package com.fintech.ticket.service.impl;

import com.fintech.ticket.common.constant.CacheConstant;
import com.fintech.ticket.dto.response.EventDetailResponse;
import com.fintech.ticket.dto.response.TicketTierResponse;
import com.fintech.ticket.entity.Event;
import com.fintech.ticket.exception.ResourceNotFoundException;
import com.fintech.ticket.repository.EventRepository;
import com.fintech.ticket.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<EventDetailResponse> getAllPublishedEvents() {
        List<Event> events = eventRepository.findByStatusOrderByEventDateAsc("PUBLISHED");

        // JAVA STREAM API: Transform entity list to DTO list
        return events.stream()
                .map(this::mapToEventDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventDetailResponse getEventById(UUID eventId) {
        String cacheKey = CacheConstant.EVENT_CACHE_PREFIX + eventId;

        // 1. Check Redis Cache first
        try {
            Object cachedData = redisTemplate.opsForValue().get(cacheKey);
            if (cachedData instanceof EventDetailResponse cachedResponse) {
                log.info("Cache HIT for event details: {}", eventId);
                return cachedResponse;
            }
        } catch (Exception e) {
            log.warn("Redis unavailable or error during get: {}", e.getMessage());
        }

        log.info("Cache MISS for event details: {}. Querying database...", eventId);

        // 2. Fetch from database if cache miss
        Event event = eventRepository.findByIdWithTiers(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + eventId));

        EventDetailResponse response = mapToEventDetailResponse(event);

        // 3. Put into Redis cache with TTL
        try {
            redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMinutes(CacheConstant.EVENT_CACHE_TTL_MINUTES));
        } catch (Exception e) {
            log.warn("Failed to write to Redis cache: {}", e.getMessage());
        }

        return response;
    }

    @Override
    public void evictEventCache(UUID eventId) {
        String cacheKey = CacheConstant.EVENT_CACHE_PREFIX + eventId;
        try {
            redisTemplate.delete(cacheKey);
            log.info("Evicted event cache for ID: {}", eventId);
        } catch (Exception e) {
            log.warn("Failed to evict Redis cache: {}", e.getMessage());
        }
    }

    private EventDetailResponse mapToEventDetailResponse(Event event) {
        // JAVA STREAM API: Map tier entities to response DTOs
        List<TicketTierResponse> tierResponses = event.getTiers().stream()
                .map(tier -> TicketTierResponse.builder()
                        .id(tier.getId())
                        .tierName(tier.getTierName())
                        .price(tier.getPrice())
                        .totalQuota(tier.getTotalQuota())
                        .availableQuota(tier.getAvailableQuota())
                        .build())
                .collect(Collectors.toList());

        return EventDetailResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .venue(event.getVenue())
                .eventDate(event.getEventDate())
                .status(event.getStatus())
                .tiers(tierResponses)
                .build();
    }
}
