package com.fintech.ticket.service;

import com.fintech.ticket.dto.response.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuditService {
    void recordAudit(String action, String entityName, String entityId, String performedBy, Object oldValue, Object newValue, String ipAddress);
    List<AuditLogResponse> getRecentAuditLogs();
    Page<AuditLogResponse> getAuditLogsPaged(Pageable pageable);
}
