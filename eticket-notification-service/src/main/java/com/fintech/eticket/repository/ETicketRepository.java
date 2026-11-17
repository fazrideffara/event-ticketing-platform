package com.fintech.eticket.repository;

import com.fintech.eticket.entity.ETicket;

import java.util.Optional;

public interface ETicketRepository {
    ETicket save(ETicket ticket);
    Optional<ETicket> findByOrderNumber(String orderNumber);
    Optional<ETicket> findByTicketCode(String ticketCode);
}
