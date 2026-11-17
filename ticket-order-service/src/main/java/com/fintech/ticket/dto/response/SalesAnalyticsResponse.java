package com.fintech.ticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesAnalyticsResponse {
    private long totalCompletedOrders;
    private long totalTicketsSold;
    private BigDecimal totalGrossRevenue;
    private Map<String, Long> ticketCountByTier;
    private Map<String, BigDecimal> revenueByTier;
    private Map<String, Long> orderCountByStatus;
    private double averageOrderValue;
}
