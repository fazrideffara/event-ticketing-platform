package com.fintech.ticket.repository;

import com.fintech.ticket.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    List<Event> findByStatusOrderByEventDateAsc(String status);

    @Query(value = "SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.tiers WHERE e.id = :id")
    Optional<Event> findByIdWithTiers(UUID id);
}
