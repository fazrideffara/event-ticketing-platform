package com.fintech.eticket.service.impl;

import com.fintech.eticket.common.constant.AppConstant;
import com.fintech.eticket.entity.ETicket;
import com.fintech.eticket.event.OrderPaidEvent;
import com.fintech.eticket.repository.ETicketRepository;
import com.fintech.eticket.service.ETicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ETicketServiceImpl implements ETicketService {

    private final ETicketRepository ticketRepository;

    @Override
    public ETicket issueTicketFromEvent(OrderPaidEvent event) {
        log.info("Processing E-Ticket issuance for orderNumber: {}", event.getOrderNumber());

        String ticketCode = generateTicketCode();
        String qrHash = generateQrHash(ticketCode, event.getOrderNumber(), event.getBuyerEmail());

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
                .status(AppConstant.STATUS_ISSUED)
                .issuedAt(LocalDateTime.now())
                .build();

        ETicket savedTicket = ticketRepository.save(ticket);

        // Simulate sending email notification
        dispatchEmailNotification(savedTicket);

        log.info("E-Ticket successfully issued: code={}, orderNumber={}, buyer={}",
                savedTicket.getTicketCode(), savedTicket.getOrderNumber(), savedTicket.getBuyerEmail());

        return savedTicket;
    }

    @Override
    public ETicket getTicketByOrderNumber(String orderNumber) {
        return ticketRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("E-Ticket not found for order number: " + orderNumber));
    }

    @Override
    public ETicket verifyTicketAtGate(String ticketCode) {
        ETicket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new RuntimeException("Invalid ticket code: " + ticketCode));

        if (AppConstant.STATUS_VERIFIED.equals(ticket.getStatus())) {
            throw new RuntimeException("Ticket has already been verified and checked-in at: " + ticket.getVerifiedAt());
        }

        ticket.setStatus(AppConstant.STATUS_VERIFIED);
        ticket.setVerifiedAt(LocalDateTime.now());

        log.info("Ticket verified at concert entrance gate: code={}, buyer={}", ticketCode, ticket.getBuyerName());
        return ticketRepository.save(ticket);
    }

    private void dispatchEmailNotification(ETicket ticket) {
        log.info("=========================================================================");
        log.info(">>> SIMULATING EMAIL DISPATCH <<<");
        log.info("To: {} ({})", ticket.getBuyerEmail(), ticket.getBuyerName());
        log.info("Subject: [CONFIRMATION] Your E-Ticket for {}", ticket.getEventName());
        log.info("E-Ticket Code: {}", ticket.getTicketCode());
        log.info("Tier: {} | Quantity: {} ticket(s)", ticket.getTierName(), ticket.getQuantity());
        log.info("QR Code Verification Hash: {}", ticket.getQrVerificationHash());
        log.info("=========================================================================");
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
