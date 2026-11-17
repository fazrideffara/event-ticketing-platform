package com.fintech.ticket.service;

import com.fintech.ticket.dto.request.CreateOrderRequest;
import com.fintech.ticket.dto.response.OrderResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request, String username, String ipAddress);
    OrderResponse getOrderById(UUID orderId, String username);
    List<OrderResponse> getUserOrders(String username);
}
