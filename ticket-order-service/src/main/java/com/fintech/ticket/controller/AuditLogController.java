package com.fintech.ticket.controller;

import com.fintech.ticket.common.constant.ApiUrlConstant;
import com.fintech.ticket.common.constant.AppConstant;
import com.fintech.ticket.common.response.ApiResponse;
import com.fintech.ticket.dto.response.AuditLogResponse;
import com.fintech.ticket.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiUrlConstant.AUDIT_BASE)
@RequiredArgsConstructor
@Tag(name = "Audit Trail Management", description = "Endpoints for inspecting system audit trails and compliance activity logs")
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "Get recent audit trail logs", description = "Retrieves the latest 50 audit logs across all system entities")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getRecentAudits() {
        List<AuditLogResponse> logs = auditService.getRecentAuditLogs();
        return ResponseEntity.ok(ApiResponse.ok(AppConstant.MSG_AUDIT_RETRIEVED, logs));
    }

    @GetMapping("/paged")
    @Operation(summary = "Get audit trail logs (Paged)", description = "Retrieves paginated audit logs for historical inspection")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AuditLogResponse> logs = auditService.getAuditLogsPaged(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(AppConstant.MSG_AUDIT_RETRIEVED, logs));
    }
}
