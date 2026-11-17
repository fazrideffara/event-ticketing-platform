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
public class TicketTierResponse {
    private UUID id;
    private String tierName;
    private BigDecimal price;
    private Integer totalQuota;
    private Integer availableQuota;
}
