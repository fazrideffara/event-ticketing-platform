package com.fintech.ticket.controller;

import com.fintech.ticket.common.constant.ApiUrlConstant;
import com.fintech.ticket.common.constant.AppConstant;
import com.fintech.ticket.common.response.ApiResponse;
import com.fintech.ticket.dto.request.CreateOrderRequest;
import com.fintech.ticket.dto.response.OrderResponse;
import com.fintech.ticket.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiUrlConstant.ORDERS_BASE)
@RequiredArgsConstructor
@Tag(name = "Order & Booking Management", description = "Endpoints for creating and retrieving ticket bookings")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create ticket booking order", description = "Creates a new PENDING order for the authenticated user")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest servletRequest
    ) {
        String ipAddress = servletRequest.getRemoteAddr();
        OrderResponse response = orderService.createOrder(request, userDetails.getUsername(), ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(AppConstant.MSG_ORDER_CREATED, response));
    }

    @GetMapping(ApiUrlConstant.ORDERS_ID)
    @Operation(summary = "Get order details by ID", description = "Retrieves order status and breakdown by Order UUID")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        OrderResponse response = orderService.getOrderById(orderId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(AppConstant.MSG_ORDER_CREATED, response));
    }

    @GetMapping(ApiUrlConstant.ORDERS_USER)
    @Operation(summary = "Get current user orders", description = "Lists all ticket orders made by the authenticated user")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getUserOrders(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<OrderResponse> orders = orderService.getUserOrders(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("User orders retrieved successfully", orders));
    }
}
