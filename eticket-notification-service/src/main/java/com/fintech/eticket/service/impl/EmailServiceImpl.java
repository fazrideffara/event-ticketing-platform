package com.fintech.eticket.service.impl;

import com.fintech.eticket.entity.ETicket;
import com.fintech.eticket.service.EmailService;
import com.fintech.eticket.service.QRCodeService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final QRCodeService qrCodeService;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${app.mail.from:no-reply@ticketplatform.com}")
    private String mailFrom;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy HH:mm");

    @Override
    public void sendETicketEmail(ETicket ticket) {
        log.info("Preparing E-Ticket email dispatch for recipient: {} (Ticket: {})",
                ticket.getBuyerEmail(), ticket.getTicketCode());

        String htmlContent = renderETicketHtml(ticket);

        if (!mailEnabled) {
            log.info("Email delivery is disabled via app.mail.enabled=false. Skipping SMTP transport.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom);
            helper.setTo(ticket.getBuyerEmail());
            helper.setSubject("[E-TICKET CONFIRMATION] " + ticket.getEventName() + " - " + ticket.getTicketCode());
            helper.setText(htmlContent, true);

            byte[] qrBytes = qrCodeService.generateQRCodeImage(ticket.getQrPayload(), 300, 300);
            helper.addAttachment("eticket-" + ticket.getTicketCode() + "-qr.png",
                    new ByteArrayResource(qrBytes));

            mailSender.send(message);
            log.info("E-Ticket email successfully dispatched via SMTP to {}", ticket.getBuyerEmail());
        } catch (Exception e) {
            log.warn("SMTP dispatch failed for {}: {}. (E-Ticket remains active in Redis and accessible via API)",
                    ticket.getBuyerEmail(), e.getMessage());
        }
    }

    @Override
    public String renderETicketHtml(ETicket ticket) {
        String formattedPrice = "IDR 0";
        if (ticket.getTotalAmount() != null) {
            DecimalFormat df = new DecimalFormat("#,###");
            formattedPrice = "IDR " + df.format(ticket.getTotalAmount());
        }

        String issuedDate = ticket.getIssuedAt() != null
                ? ticket.getIssuedAt().format(DATE_FORMATTER)
                : "-";

        String qrImageSrc = ticket.getQrCodeBase64() != null
                ? ticket.getQrCodeBase64()
                : "";

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>E-Ticket Gate Pass</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                        background-color: #f1f5f9;
                        margin: 0;
                        padding: 24px;
                        color: #1e293b;
                    }
                    .ticket-card {
                        max-width: 600px;
                        margin: 0 auto;
                        background: #ffffff;
                        border-radius: 16px;
                        overflow: hidden;
                        box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
                        border: 1px solid #e2e8f0;
                    }
                    .ticket-header {
                        background: linear-gradient(135deg, #0f172a 0%%, #1e3a8a 100%%);
                        color: #ffffff;
                        padding: 32px 28px;
                        text-align: center;
                    }
                    .ticket-header h1 {
                        margin: 0 0 8px 0;
                        font-size: 22px;
                        letter-spacing: 1px;
                        text-transform: uppercase;
                        font-weight: 800;
                    }
                    .badge-status {
                        display: inline-block;
                        background-color: #10b981;
                        color: #ffffff;
                        font-size: 11px;
                        font-weight: 700;
                        text-transform: uppercase;
                        letter-spacing: 1.5px;
                        padding: 6px 16px;
                        border-radius: 9999px;
                        margin-top: 6px;
                    }
                    .ticket-body {
                        padding: 28px;
                    }
                    .ticket-code-box {
                        background-color: #f8fafc;
                        border: 2px dashed #cbd5e1;
                        border-radius: 12px;
                        text-align: center;
                        padding: 16px;
                        margin-bottom: 24px;
                    }
                    .ticket-code-label {
                        font-size: 11px;
                        text-transform: uppercase;
                        letter-spacing: 1.2px;
                        color: #64748b;
                        font-weight: 600;
                        margin-bottom: 4px;
                    }
                    .ticket-code-value {
                        font-family: 'Courier New', Courier, monospace;
                        font-size: 22px;
                        font-weight: 800;
                        color: #1e3a8a;
                        letter-spacing: 2px;
                    }
                    .details-grid {
                        width: 100%%;
                        border-collapse: collapse;
                        margin-bottom: 24px;
                    }
                    .details-grid td {
                        padding: 10px 0;
                        border-bottom: 1px solid #f1f5f9;
                        vertical-align: top;
                    }
                    .detail-label {
                        color: #64748b;
                        font-size: 13px;
                        font-weight: 600;
                        width: 38%%;
                    }
                    .detail-value {
                        color: #0f172a;
                        font-size: 14px;
                        font-weight: 700;
                        text-align: right;
                    }
                    .qr-section {
                        text-align: center;
                        background-color: #f8fafc;
                        border-radius: 12px;
                        padding: 24px 16px;
                        margin-bottom: 24px;
                        border: 1px solid #e2e8f0;
                    }
                    .qr-image {
                        width: 220px;
                        height: 220px;
                        background-color: #ffffff;
                        padding: 10px;
                        border-radius: 8px;
                        box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
                        margin: 0 auto 12px auto;
                        display: block;
                    }
                    .qr-instruction {
                        font-size: 12px;
                        color: #475569;
                        margin: 0;
                        line-height: 1.5;
                    }
                    .security-notice {
                        background-color: #eff6ff;
                        border-left: 4px solid #3b82f6;
                        padding: 12px 16px;
                        border-radius: 0 8px 8px 0;
                        font-size: 12px;
                        color: #1e40af;
                        line-height: 1.6;
                        margin-bottom: 20px;
                    }
                    .ticket-footer {
                        text-align: center;
                        font-size: 11px;
                        color: #94a3b8;
                        border-top: 1px solid #f1f5f9;
                        padding-top: 16px;
                    }
                </style>
            </head>
            <body>
                <div class="ticket-card">
                    <div class="ticket-header">
                        <h1>%s</h1>
                        <div><span class="badge-status">VALID GATE PASS</span></div>
                    </div>
                    <div class="ticket-body">
                        <div class="ticket-code-box">
                            <div class="ticket-code-label">OFFICIAL PASS CODE</div>
                            <div class="ticket-code-value">%s</div>
                        </div>

                        <table class="details-grid">
                            <tr>
                                <td class="detail-label">Event Name</td>
                                <td class="detail-value">%s</td>
                            </tr>
                            <tr>
                                <td class="detail-label">Ticket Category</td>
                                <td class="detail-value">%s</td>
                            </tr>
                            <tr>
                                <td class="detail-label">Quantity</td>
                                <td class="detail-value">%d Ticket(s)</td>
                            </tr>
                            <tr>
                                <td class="detail-label">Total Amount Paid</td>
                                <td class="detail-value" style="color: #059669;">%s</td>
                            </tr>
                            <tr>
                                <td class="detail-label">Order Number</td>
                                <td class="detail-value">%s</td>
                            </tr>
                            <tr>
                                <td class="detail-label">Attendee / Buyer</td>
                                <td class="detail-value">%s (%s)</td>
                            </tr>
                            <tr>
                                <td class="detail-label">Issued At</td>
                                <td class="detail-value">%s</td>
                            </tr>
                        </table>

                        <div class="qr-section">
                            <img src="%s" alt="Ticket QR Code" class="qr-image"/>
                            <p class="qr-instruction">
                                <strong>SCAN AT TURNSTILE GATE</strong><br/>
                                Present this digital pass or printed QR code at the event gate entrance for validation.
                            </p>
                        </div>

                        <div class="security-notice">
                            <strong>Security & Check-in Advisory:</strong><br/>
                            This e-ticket has a unique cryptographic verification hash. Each QR code can only be checked in once. Strictly non-transferable and non-refundable.
                        </div>

                        <div class="ticket-footer">
                            Automated issuance by Event Ticketing Platform &bull; Need assistance? contact support@ticketplatform.com
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                escapeHtml(ticket.getEventName()),
                ticket.getTicketCode(),
                escapeHtml(ticket.getEventName()),
                escapeHtml(ticket.getTierName()),
                ticket.getQuantity(),
                formattedPrice,
                ticket.getOrderNumber(),
                escapeHtml(ticket.getBuyerName()),
                escapeHtml(ticket.getBuyerEmail()),
                issuedDate,
                qrImageSrc
        );
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
