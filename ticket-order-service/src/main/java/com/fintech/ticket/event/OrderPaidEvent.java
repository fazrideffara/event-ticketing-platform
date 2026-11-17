package com.fintech.ticket.event;

import com.fintech.ticket.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaidEvent implements Serializable {
    private UUID orderId;
    private String orderNumber;
    private UUID eventId;
    private String eventName;
    private String tierName;
    private Integer quantity;
    private BigDecimal totalAmount;
    private UUID buyerId;
    private String buyerUsername;
    private String buyerEmail;
    private String buyerFullName;
    private PaymentMethod paymentMethod;
    private LocalDateTime paidAt;
}
