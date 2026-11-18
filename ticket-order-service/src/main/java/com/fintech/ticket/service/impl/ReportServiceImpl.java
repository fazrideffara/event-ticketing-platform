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
        List<RunningRevenueProjection> projections = orderRepository.findRunningRevenueReport();

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
        List<OccupancySummaryProjection> projections = orderRepository.findOccupancySummaryReport();

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

        List<Order> paidOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID)
                .toList();

        long totalCompletedOrders = paidOrders.size();

        long totalTicketsSold = paidOrders.stream()
                .mapToLong(Order::getQuantity)
                .sum();

        BigDecimal totalGrossRevenue = paidOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> ticketCountByTier = paidOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getTier().getTierName(),
                        Collectors.summingLong(Order::getQuantity)
                ));

        Map<String, BigDecimal> revenueByTier = paidOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getTier().getTierName(),
                        Collectors.reducing(BigDecimal.ZERO, Order::getTotalAmount, BigDecimal::add)
                ));

        Map<String, Long> orderCountByStatus = allOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getStatus().name(),
                        Collectors.counting()
                ));

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
