package com.fintech.eticket.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.eticket.common.constant.AppConstant;
import com.fintech.eticket.entity.ETicket;
import com.fintech.eticket.event.OrderPaidEvent;
import com.fintech.eticket.repository.ETicketRepository;
import com.fintech.eticket.service.EmailService;
import com.fintech.eticket.service.ETicketService;
import com.fintech.eticket.service.QRCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ETicketServiceImpl implements ETicketService {

    private final ETicketRepository ticketRepository;
    private final QRCodeService qrCodeService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @Override
    public ETicket issueTicketFromEvent(OrderPaidEvent event) {
        log.info("Processing E-Ticket issuance for orderNumber: {}", event.getOrderNumber());

        String ticketCode = generateTicketCode();
        String qrHash = generateQrHash(ticketCode, event.getOrderNumber(), event.getBuyerEmail());

        // Construct structured QR payload
        String qrPayload = buildQrPayload(ticketCode, event, qrHash);

        // Generate QR code image in Base64 format
        String qrCodeBase64 = qrCodeService.generateQRCodeBase64(qrPayload, 300, 300);

        ETicket ticket = ETicket.builder()
                .ticketCode(ticketCode)
                .orderNumber(event.getOrderNumber())
                .eventName(event.getEventName())
                .tierName(event.getTierName())
                .quantity(event.getQuantity())
                .totalAmount(event.getTotalAmount())
                .buyerName(event.getBuyerFullName())
                .buyerEmail(event.getBuyerEmail())
                .qrVerificationHash(qrHash)
                .qrPayload(qrPayload)
                .qrCodeBase64(qrCodeBase64)
                .status(AppConstant.STATUS_ISSUED)
                .issuedAt(LocalDateTime.now())
                .build();

        ETicket savedTicket = ticketRepository.save(ticket);

        // Dispatch email notification with embedded QR code and ticket details
        emailService.sendETicketEmail(savedTicket);

        log.info("E-Ticket successfully issued and stored: code={}, orderNumber={}, buyer={}",
                savedTicket.getTicketCode(), savedTicket.getOrderNumber(), savedTicket.getBuyerEmail());

        return savedTicket;
    }

    @Override
    public ETicket getTicketByOrderNumber(String orderNumber) {
        return ticketRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("E-Ticket not found for order number: " + orderNumber));
    }

    @Override
    public ETicket getTicketByCode(String ticketCode) {
        return ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new RuntimeException("E-Ticket not found for code: " + ticketCode));
    }

    @Override
    public ETicket verifyTicketAtGate(String ticketCode) {
        ETicket ticket = getTicketByCode(ticketCode);

        if (AppConstant.STATUS_VERIFIED.equals(ticket.getStatus())) {
            throw new RuntimeException("Ticket has already been verified and checked-in at: " + ticket.getVerifiedAt());
        }

        ticket.setStatus(AppConstant.STATUS_VERIFIED);
        ticket.setVerifiedAt(LocalDateTime.now());

        log.info("Ticket verified at concert entrance gate: code={}, buyer={}", ticketCode, ticket.getBuyerName());
        return ticketRepository.save(ticket);
    }

    @Override
    public String renderTicketHtml(String ticketCode) {
        ETicket ticket = getTicketByCode(ticketCode);
        return emailService.renderETicketHtml(ticket);
    }

    private String buildQrPayload(String ticketCode, OrderPaidEvent event, String qrHash) {
        try {
            Map<String, Object> payloadMap = new LinkedHashMap<>();
            payloadMap.put("ticketCode", ticketCode);
            payloadMap.put("orderNumber", event.getOrderNumber());
            payloadMap.put("eventName", event.getEventName());
            payloadMap.put("tierName", event.getTierName());
            payloadMap.put("quantity", event.getQuantity());
            payloadMap.put("buyerName", event.getBuyerFullName());
            payloadMap.put("buyerEmail", event.getBuyerEmail());
            payloadMap.put("hash", qrHash);
            payloadMap.put("verificationUrl", "http://localhost:8082/api/v1/etickets/verify/" + ticketCode);
            return objectMapper.writeValueAsString(payloadMap);
        } catch (Exception e) {
            log.warn("Failed to serialize QR payload to JSON, falling back to raw format", e);
            return "TICKET:" + ticketCode + "|ORDER:" + event.getOrderNumber() + "|HASH:" + qrHash;
        }
    }

    private String generateTicketCode() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return AppConstant.TICKET_PREFIX + dateStr + "-" + randomStr;
    }

    private String generateQrHash(String ticketCode, String orderNumber, String email) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String raw = ticketCode + ":" + orderNumber + ":" + email + ":SALT_TICKET_SECURE_2026";
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error computing QR hash", e);
        }
    }
}
