package com.fintech.ticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDetailResponse {
    private UUID id;
    private String name;
    private String description;
    private String venue;
    private LocalDateTime eventDate;
    private String status;
    private List<TicketTierResponse> tiers;
}
