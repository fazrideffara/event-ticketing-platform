package com.fintech.ticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunningRevenueReportResponse {
    private UUID orderId;
    private String orderNumber;
    private String eventName;
    private String tierName;
    private Integer quantity;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private BigDecimal runningTotalRevenue;
    private Long revenueRank;
}
