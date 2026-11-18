package com.fintech.eticket.repository.impl;

import com.fintech.eticket.entity.ETicket;
import com.fintech.eticket.repository.ETicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ETicketRepositoryImpl implements ETicketRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private final Map<String, ETicket> localByOrder = new ConcurrentHashMap<>();
    private final Map<String, ETicket> localByCode = new ConcurrentHashMap<>();

    private static final String REDIS_ORDER_PREFIX = "eticket:order:";
    private static final String REDIS_CODE_PREFIX = "eticket:code:";

    @Override
    public ETicket save(ETicket ticket) {
        localByOrder.put(ticket.getOrderNumber(), ticket);
        localByCode.put(ticket.getTicketCode(), ticket);

        try {
            redisTemplate.opsForValue().set(REDIS_ORDER_PREFIX + ticket.getOrderNumber(), ticket);
            redisTemplate.opsForValue().set(REDIS_CODE_PREFIX + ticket.getTicketCode(), ticket);
            log.info("Saved ETicket to Redis store: ticketCode={}, orderNumber={}", ticket.getTicketCode(), ticket.getOrderNumber());
        } catch (Exception e) {
            log.warn("Redis unavailable, saved to local in-memory store: {}", e.getMessage());
        }

        return ticket;
    }

    @Override
    public Optional<ETicket> findByOrderNumber(String orderNumber) {
        try {
            Object obj = redisTemplate.opsForValue().get(REDIS_ORDER_PREFIX + orderNumber);
            if (obj instanceof ETicket ticket) {
                return Optional.of(ticket);
            }
        } catch (Exception e) {
            log.warn("Failed to read from Redis, checking local store: {}", e.getMessage());
        }
        return Optional.ofNullable(localByOrder.get(orderNumber));
    }

    @Override
    public Optional<ETicket> findByTicketCode(String ticketCode) {
        try {
            Object obj = redisTemplate.opsForValue().get(REDIS_CODE_PREFIX + ticketCode);
            if (obj instanceof ETicket ticket) {
                return Optional.of(ticket);
            }
        } catch (Exception e) {
            log.warn("Failed to read from Redis, checking local store: {}", e.getMessage());
        }
        return Optional.ofNullable(localByCode.get(ticketCode));
    }
}
