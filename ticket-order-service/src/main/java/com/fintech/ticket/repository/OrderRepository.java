package com.fintech.ticket.repository;

import com.fintech.ticket.entity.Order;
import com.fintech.ticket.entity.OrderStatus;
import com.fintech.ticket.repository.projection.OccupancySummaryProjection;
import com.fintech.ticket.repository.projection.RunningRevenueProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Order> findByStatus(OrderStatus status);

    @Query(value = """
            SELECT 
                o.id AS orderId,
                o.order_number AS orderNumber,
                e.name AS eventName,
                tt.tier_name AS tierName,
                o.quantity AS quantity,
                o.total_amount AS totalAmount,
                o.created_at AS createdAt,
                SUM(o.total_amount) OVER (
                    PARTITION BY o.event_id 
                    ORDER BY o.created_at ASC
                ) AS runningTotalRevenue,
                DENSE_RANK() OVER (
                    PARTITION BY o.event_id 
                    ORDER BY o.total_amount DESC
                ) AS revenueRank
            FROM orders o
            JOIN events e ON o.event_id = e.id
            JOIN ticket_tiers tt ON o.tier_id = tt.id
            WHERE o.status = 'PAID'
            ORDER BY o.event_id, o.created_at ASC
            """, nativeQuery = true)
    List<RunningRevenueProjection> findRunningRevenueReport();

    @Query(value = """
            WITH tier_metrics AS (
                SELECT 
                    tier_id,
                    COUNT(id) AS total_successful_orders,
                    COALESCE(SUM(quantity), 0) AS total_tickets_sold,
                    COALESCE(SUM(total_amount), 0) AS total_gross_revenue
                FROM orders
                WHERE status = 'PAID'
                GROUP BY tier_id
            ),
            event_tier_summary AS (
                SELECT 
                    e.id AS eventId,
                    e.name AS eventName,
                    tt.id AS tierId,
                    tt.tier_name AS tierName,
                    tt.price AS ticketPrice,
                    tt.total_quota AS totalQuota,
                    tt.available_quota AS availableQuota,
                    COALESCE(tm.total_tickets_sold, 0) AS ticketsSold,
                    COALESCE(tm.total_gross_revenue, 0) AS grossRevenue,
                    ROUND((COALESCE(tm.total_tickets_sold, 0)::numeric / NULLIF(tt.total_quota, 0)::numeric) * 100, 2) AS occupancyPercentage
                FROM ticket_tiers tt
                JOIN events e ON tt.event_id = e.id
                LEFT JOIN tier_metrics tm ON tt.id = tm.tier_id
            )
            SELECT * FROM event_tier_summary
            ORDER BY eventName, ticketPrice DESC
            """, nativeQuery = true)
    List<OccupancySummaryProjection> findOccupancySummaryReport();
}
