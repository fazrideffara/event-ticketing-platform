package com.fintech.eticket.service;

import com.fintech.eticket.entity.ETicket;

public interface EmailService {
    void sendETicketEmail(ETicket ticket);
    String renderETicketHtml(ETicket ticket);
}
