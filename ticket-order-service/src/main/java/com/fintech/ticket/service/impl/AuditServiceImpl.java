package com.fintech.ticket.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.ticket.dto.response.AuditLogResponse;
import com.fintech.ticket.entity.AuditLog;
import com.fintech.ticket.repository.AuditLogRepository;
import com.fintech.ticket.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAudit(
            String action,
            String entityName,
            String entityId,
            String performedBy,
            Object oldValue,
            Object newValue,
            String ipAddress
    ) {
        try {
            String oldJson = oldValue != null ? objectMapper.writeValueAsString(oldValue) : null;
            String newJson = newValue != null ? objectMapper.writeValueAsString(newValue) : null;

            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityName(entityName)
                    .entityId(entityId)
                    .performedBy(performedBy)
                    .oldValue(oldJson)
                    .newValue(newJson)
                    .ipAddress(ipAddress != null ? ipAddress : "127.0.0.1")
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit recorded: action={}, entity={}, id={}, user={}", action, entityName, entityId, performedBy);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit log details for entity: {}", entityName, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getRecentAuditLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsPaged(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable)
                .map(this::mapToResponse);
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .action(log.getAction())
                .entityName(log.getEntityName())
                .entityId(log.getEntityId())
                .performedBy(log.getPerformedBy())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .ipAddress(log.getIpAddress())
                .timestamp(log.getTimestamp())
                .build();
    }
}
