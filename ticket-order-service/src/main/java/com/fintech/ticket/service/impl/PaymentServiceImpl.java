package com.fintech.ticket.service.impl;

import com.fintech.ticket.common.constant.AppConstant;
import com.fintech.ticket.common.constant.CacheConstant;
import com.fintech.ticket.common.constant.KafkaConstant;
import com.fintech.ticket.dto.request.ProcessPaymentRequest;
import com.fintech.ticket.dto.response.PaymentResponse;
import com.fintech.ticket.entity.*;
import com.fintech.ticket.event.OrderPaidEvent;
import com.fintech.ticket.exception.BadRequestException;
import com.fintech.ticket.exception.DuplicateTransactionException;
import com.fintech.ticket.exception.InsufficientQuotaException;
import com.fintech.ticket.exception.ResourceNotFoundException;
import com.fintech.ticket.repository.OrderRepository;
import com.fintech.ticket.repository.PaymentRepository;
import com.fintech.ticket.repository.TicketTierRepository;
import com.fintech.ticket.service.AuditService;
import com.fintech.ticket.service.EventService;
import com.fintech.ticket.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final TicketTierRepository ticketTierRepository;
    private final EventService eventService;
    private final AuditService auditService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public PaymentResponse processPayment(
            String idempotencyKey,
            ProcessPaymentRequest request,
            String username,
            String ipAddress
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BadRequestException("Header " + AppConstant.HEADER_IDEMPOTENCY_KEY + " is mandatory.");
        }

        String redisKey = CacheConstant.IDEMPOTENCY_PREFIX + idempotencyKey;

        // 1. REDIS IDEMPOTENCY KEY CHECK (SETNX with TTL)
        boolean isNewRequest = false;
        try {
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(redisKey, AppConstant.STATUS_PROCESSING, Duration.ofMinutes(CacheConstant.IDEMPOTENCY_TTL_MINUTES));
            isNewRequest = Boolean.TRUE.equals(acquired);
        } catch (Exception e) {
            log.warn("Redis is unavailable for idempotency locking, falling back to DB unique constraint: {}", e.getMessage());
            isNewRequest = !paymentRepository.existsByIdempotencyKey(idempotencyKey);
        }

        if (!isNewRequest) {
            log.warn("Duplicate payment request detected for idempotency key: {}", idempotencyKey);
            // Check if existing payment is already saved in DB
            Optional<Payment> existingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey);
            if (existingPayment.isPresent()) {
                log.info("Returning previously completed payment for idempotency key: {}", idempotencyKey);
                return mapToResponse(existingPayment.get(), "Idempotent response: payment already completed.");
            }
            throw new DuplicateTransactionException("A payment transaction with this idempotency key is currently being processed. Please wait.");
        }

        try {
            // 2. Execute transactional payment with Pessimistic Lock
            Payment payment = executePaymentTransaction(request, idempotencyKey, username, ipAddress);

            // 3. Mark Idempotency key as COMPLETED in Redis
            try {
                redisTemplate.opsForValue().set(redisKey, AppConstant.STATUS_COMPLETED, Duration.ofMinutes(CacheConstant.IDEMPOTENCY_TTL_MINUTES));
            } catch (Exception e) {
                log.warn("Failed to update idempotency status in Redis: {}", e.getMessage());
            }

            // 4. Publish Event to Kafka asynchronously
            publishOrderPaidEvent(payment.getOrder(), payment);

            // 5. Evict event cache so that clients see real-time remaining quota
            eventService.evictEventCache(payment.getOrder().getEvent().getId());

            return mapToResponse(payment, AppConstant.MSG_PAYMENT_SUCCESS);

        } catch (Exception ex) {
            // If failed, clear idempotency key in Redis so the client can safely retry
            try {
                redisTemplate.delete(redisKey);
            } catch (Exception e) {
                log.warn("Failed to delete idempotency key from Redis on error: {}", e.getMessage());
            }
            throw ex;
        }
    }

    @Transactional
    public Payment executePaymentTransaction(
            ProcessPaymentRequest request,
            String idempotencyKey,
            String username,
            String ipAddress
    ) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.getOrderId()));

        if (order.getStatus() == OrderStatus.PAID) {
            throw new BadRequestException("Order " + order.getOrderNumber() + " is already paid.");
        }

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.EXPIRED) {
            throw new BadRequestException("Order " + order.getOrderNumber() + " is no longer eligible for payment (Status: " + order.getStatus() + ").");
        }

        if (order.getExpiresAt().isBefore(LocalDateTime.now())) {
            order.setStatus(OrderStatus.EXPIRED);
            orderRepository.save(order);
            throw new BadRequestException("Order has expired. Please create a new booking.");
        }

        // =========================================================================
        // PESSIMISTIC LOCKING: Kunci baris tier di PostgreSQL (SELECT ... FOR UPDATE)
        // Mencegah overselling saat terjadi high-concurrency ticket war
        // =========================================================================
        TicketTier lockedTier = ticketTierRepository.findByIdForUpdate(order.getTier().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket tier not found: " + order.getTier().getId()));

        if (lockedTier.getAvailableQuota() < order.getQuantity()) {
            throw new InsufficientQuotaException("Quota exceeded during checkout. Remaining quota: " + lockedTier.getAvailableQuota());
        }

        // Potong kuota tiket
        int oldQuota = lockedTier.getAvailableQuota();
        int newQuota = oldQuota - order.getQuantity();
        lockedTier.setAvailableQuota(newQuota);
        ticketTierRepository.save(lockedTier);

        // Update status order menjadi PAID
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        // Buat record pembayaran
        Payment payment = Payment.builder()
                .order(order)
                .idempotencyKey(idempotencyKey)
                .paymentMethod(request.getPaymentMethod())
                .amount(order.getTotalAmount())
                .status(PaymentStatus.SUCCESS)
                .paidAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Record Audit Trail
        auditService.recordAudit(
                "PAYMENT_SUCCESS",
                "Payment",
                savedPayment.getId().toString(),
                username,
                "Old Quota: " + oldQuota,
                "New Quota: " + newQuota + ", Paid: " + order.getTotalAmount(),
                ipAddress
        );

        log.info("Payment successful: paymentId={}, orderNumber={}, amount={}, newQuota={}",
                savedPayment.getId(), order.getOrderNumber(), order.getTotalAmount(), newQuota);

        return savedPayment;
    }

    private void publishOrderPaidEvent(Order order, Payment payment) {
        OrderPaidEvent event = OrderPaidEvent.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .eventId(order.getEvent().getId())
                .eventName(order.getEvent().getName())
                .tierName(order.getTier().getTierName())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .buyerId(order.getUser().getId())
                .buyerUsername(order.getUser().getUsername())
                .buyerEmail(order.getUser().getEmail())
                .buyerFullName(order.getUser().getFullName())
                .paymentMethod(payment.getPaymentMethod())
                .paidAt(payment.getPaidAt())
                .build();

        try {
            kafkaTemplate.send(KafkaConstant.TOPIC_ORDER_PAID, order.getOrderNumber(), event);
            log.info("Published OrderPaidEvent to Kafka topic '{}' for orderNumber: {}", KafkaConstant.TOPIC_ORDER_PAID, order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to publish OrderPaidEvent to Kafka for orderNumber: {}", order.getOrderNumber(), e);
        }
    }

    private PaymentResponse mapToResponse(Payment payment, String message) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrder().getId())
                .orderNumber(payment.getOrder().getOrderNumber())
                .idempotencyKey(payment.getIdempotencyKey())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .message(message)
                .build();
    }
}
