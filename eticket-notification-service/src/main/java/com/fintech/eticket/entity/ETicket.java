package com.fintech.eticket.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ETicket implements Serializable {
    private String ticketCode;
    private String orderNumber;
    private String eventName;
    private String tierName;
    private Integer quantity;
    private BigDecimal totalAmount;
    private String buyerName;
    private String buyerEmail;
    private String qrVerificationHash;
    private String qrCodeBase64;
    private String qrPayload;
    private String status; // ISSUED, VERIFIED
    private LocalDateTime issuedAt;
    private LocalDateTime verifiedAt;
}
