package com.fintech.ticket.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String action; // e.g. ORDER_CREATED, PAYMENT_SUCCESS, QUOTA_DEDUCTED

    @Column(name = "entity_name", nullable = false, length = 50)
    private String entityName;

    @Column(name = "entity_id", nullable = false, length = 50)
    private String entityId;

    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
