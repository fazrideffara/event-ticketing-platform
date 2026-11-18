package com.fintech.eticket.service;

import com.fintech.eticket.entity.ETicket;
import com.fintech.eticket.event.OrderPaidEvent;

public interface ETicketService {
    ETicket issueTicketFromEvent(OrderPaidEvent event);
    ETicket getTicketByOrderNumber(String orderNumber);
    ETicket getTicketByCode(String ticketCode);
    ETicket verifyTicketAtGate(String ticketCode);
    String renderTicketHtml(String ticketCode);
}
