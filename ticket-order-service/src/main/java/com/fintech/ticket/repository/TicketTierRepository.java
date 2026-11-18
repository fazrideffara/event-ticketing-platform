package com.fintech.ticket.repository;

import com.fintech.ticket.entity.TicketTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketTierRepository extends JpaRepository<TicketTier, UUID> {

    List<TicketTier> findByEventId(UUID eventId);

    @Query(value = "SELECT * FROM ticket_tiers WHERE id = :tierId FOR UPDATE", nativeQuery = true)
    Optional<TicketTier> findByIdForUpdate(@Param("tierId") UUID tierId);
}
