package com.fintech.ticket.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface RunningRevenueProjection {
    UUID getOrderId();
    String getOrderNumber();
    String getEventName();
    String getTierName();
    Integer getQuantity();
    BigDecimal getTotalAmount();
    LocalDateTime getCreatedAt();
    BigDecimal getRunningTotalRevenue();
    Long getRevenueRank();
}
