package com.fintech.ticket.service;

import com.fintech.ticket.dto.request.ProcessPaymentRequest;
import com.fintech.ticket.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse processPayment(String idempotencyKey, ProcessPaymentRequest request, String username, String ipAddress);
}
