package com.fintech.ticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupancyReportResponse {
    private UUID eventId;
    private String eventName;
    private UUID tierId;
    private String tierName;
    private BigDecimal ticketPrice;
    private Integer totalQuota;
    private Integer availableQuota;
    private Long ticketsSold;
    private BigDecimal grossRevenue;
    private BigDecimal occupancyPercentage;
}
