package com.fintech.ticket.controller;

import com.fintech.ticket.common.constant.ApiUrlConstant;
import com.fintech.ticket.common.constant.AppConstant;
import com.fintech.ticket.common.response.ApiResponse;
import com.fintech.ticket.dto.request.ProcessPaymentRequest;
import com.fintech.ticket.dto.response.PaymentResponse;
import com.fintech.ticket.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiUrlConstant.PAYMENTS_BASE)
@RequiredArgsConstructor
@Tag(name = "Payment & Settlement Management", description = "Endpoints for processing ticket payments with Idempotency and Pessimistic Locking")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(
            summary = "Process ticket order payment",
            description = "Executes payment transaction with Pessimistic Lock on ticket quota, publishes OrderPaidEvent to Kafka, and protects against double-spending using Redis Idempotency Key"
    )
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Parameter(description = "Unique UUID idempotency token to prevent double-charging", required = true)
            @RequestHeader(AppConstant.HEADER_IDEMPOTENCY_KEY) String idempotencyKey,
            @Valid @RequestBody ProcessPaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest servletRequest
    ) {
        String ipAddress = servletRequest.getRemoteAddr();
        PaymentResponse response = paymentService.processPayment(
                idempotencyKey,
                request,
                userDetails.getUsername(),
                ipAddress
        );
        return ResponseEntity.ok(ApiResponse.ok(AppConstant.MSG_PAYMENT_SUCCESS, response));
    }
}
