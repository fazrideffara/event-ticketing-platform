package com.fintech.ticket.service.impl;

import com.fintech.ticket.common.constant.AppConstant;
import com.fintech.ticket.dto.request.CreateOrderRequest;
import com.fintech.ticket.dto.response.OrderResponse;
import com.fintech.ticket.entity.*;
import com.fintech.ticket.exception.BadRequestException;
import com.fintech.ticket.exception.InsufficientQuotaException;
import com.fintech.ticket.exception.ResourceNotFoundException;
import com.fintech.ticket.repository.EventRepository;
import com.fintech.ticket.repository.OrderRepository;
import com.fintech.ticket.repository.TicketTierRepository;
import com.fintech.ticket.repository.UserRepository;
import com.fintech.ticket.service.AuditService;
import com.fintech.ticket.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final EventRepository eventRepository;
    private final TicketTierRepository ticketTierRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String username, String ipAddress) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + request.getEventId()));

        TicketTier tier = ticketTierRepository.findById(request.getTierId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket tier not found with ID: " + request.getTierId()));

        if (!tier.getEvent().getId().equals(event.getId())) {
            throw new BadRequestException("Ticket tier does not belong to the specified event");
        }

        if (tier.getAvailableQuota() < request.getQuantity()) {
            throw new InsufficientQuotaException("Requested quantity exceeds available tier quota (" + tier.getAvailableQuota() + " remaining)");
        }

        BigDecimal totalAmount = tier.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        String orderNumber = generateOrderNumber();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .event(event)
                .tier(tier)
                .quantity(request.getQuantity())
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        Order savedOrder = orderRepository.save(order);

        auditService.recordAudit(
                "ORDER_CREATED",
                "Order",
                savedOrder.getId().toString(),
                user.getUsername(),
                null,
                savedOrder.getOrderNumber() + " (Total: " + totalAmount + ")",
                ipAddress
        );

        log.info("Order created successfully: orderNumber={}, user={}, total={}", orderNumber, username, totalAmount);

        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!order.getUser().getUsername().equals(username) && !order.getUser().getRole().equals(Role.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to view this order");
        }

        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        // JAVA STREAM API: Map orders to OrderResponse DTOs
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return AppConstant.ORDER_PREFIX + timestamp + "-" + randomSuffix;
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .eventId(order.getEvent().getId())
                .eventName(order.getEvent().getName())
                .tierId(order.getTier().getId())
                .tierName(order.getTier().getTierName())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .expiresAt(order.getExpiresAt())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
