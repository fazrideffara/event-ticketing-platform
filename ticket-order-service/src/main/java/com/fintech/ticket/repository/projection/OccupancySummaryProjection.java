package com.fintech.ticket.repository.projection;

import java.math.BigDecimal;
import java.util.UUID;

public interface OccupancySummaryProjection {
    UUID getEventId();
    String getEventName();
    UUID getTierId();
    String getTierName();
    BigDecimal getTicketPrice();
    Integer getTotalQuota();
    Integer getAvailableQuota();
    Long getTicketsSold();
    BigDecimal getGrossRevenue();
    BigDecimal getOccupancyPercentage();
}
