package com.fintech.ticket.service.impl;

import com.fintech.ticket.dto.response.OccupancyReportResponse;
import com.fintech.ticket.dto.response.RunningRevenueReportResponse;
import com.fintech.ticket.dto.response.SalesAnalyticsResponse;
import com.fintech.ticket.entity.Order;
import com.fintech.ticket.entity.OrderStatus;
import com.fintech.ticket.repository.OrderRepository;
import com.fintech.ticket.repository.projection.OccupancySummaryProjection;
import com.fintech.ticket.repository.projection.RunningRevenueProjection;
import com.fintech.ticket.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RunningRevenueReportResponse> getRunningRevenueReport() {
        // ADVANCE NATIVE SQL: Window Function query execution
        List<RunningRevenueProjection> projections = orderRepository.findRunningRevenueReport();

        // JAVA STREAM API: Map native projections to DTO response
        return projections.stream()
                .map(p -> RunningRevenueReportResponse.builder()
                        .orderId(p.getOrderId())
                        .orderNumber(p.getOrderNumber())
                        .eventName(p.getEventName())
                        .tierName(p.getTierName())
                        .quantity(p.getQuantity())
                        .totalAmount(p.getTotalAmount())
                        .createdAt(p.getCreatedAt())
                        .runningTotalRevenue(p.getRunningTotalRevenue())
                        .revenueRank(p.getRevenueRank())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OccupancyReportResponse> getOccupancySummaryReport() {
        // ADVANCE NATIVE SQL: CTE (Common Table Expressions) query execution
        List<OccupancySummaryProjection> projections = orderRepository.findOccupancySummaryReport();

        // JAVA STREAM API: Map native CTE projections to DTO response
        return projections.stream()
                .map(p -> OccupancyReportResponse.builder()
                        .eventId(p.getEventId())
                        .eventName(p.getEventName())
                        .tierId(p.getTierId())
                        .tierName(p.getTierName())
                        .ticketPrice(p.getTicketPrice())
                        .totalQuota(p.getTotalQuota())
                        .availableQuota(p.getAvailableQuota())
                        .ticketsSold(p.getTicketsSold())
                        .grossRevenue(p.getGrossRevenue())
                        .occupancyPercentage(p.getOccupancyPercentage())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SalesAnalyticsResponse getSalesAnalytics() {
        List<Order> allOrders = orderRepository.findAll();

        // =========================================================================
        // JAVA STREAM API ADVANCED ANALYTICS PIPELINES
        // =========================================================================

        // 1. Filter: Filter only completed/PAID orders
        List<Order> paidOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID)
                .toList();

        long totalCompletedOrders = paidOrders.size();

        // 2. Reduce: Sum total tickets sold
        long totalTicketsSold = paidOrders.stream()
                .mapToLong(Order::getQuantity)
                .sum();

        // 3. Reduce: Calculate total gross revenue
        BigDecimal totalGrossRevenue = paidOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. GroupingBy & SummingLong: Count tickets per tier
        Map<String, Long> ticketCountByTier = paidOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getTier().getTierName(),
                        Collectors.summingLong(Order::getQuantity)
                ));

        // 5. GroupingBy & Reducing: Total revenue per tier
        Map<String, BigDecimal> revenueByTier = paidOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getTier().getTierName(),
                        Collectors.reducing(BigDecimal.ZERO, Order::getTotalAmount, BigDecimal::add)
                ));

        // 6. GroupingBy & Counting: Order count by status across all orders
        Map<String, Long> orderCountByStatus = allOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getStatus().name(),
                        Collectors.counting()
                ));

        // 7. Calculate average order value
        double averageOrderValue = totalCompletedOrders > 0
                ? totalGrossRevenue.divide(BigDecimal.valueOf(totalCompletedOrders), 2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        return SalesAnalyticsResponse.builder()
                .totalCompletedOrders(totalCompletedOrders)
                .totalTicketsSold(totalTicketsSold)
                .totalGrossRevenue(totalGrossRevenue)
                .ticketCountByTier(ticketCountByTier)
                .revenueByTier(revenueByTier)
                .orderCountByStatus(orderCountByStatus)
                .averageOrderValue(averageOrderValue)
                .build();
    }
}
