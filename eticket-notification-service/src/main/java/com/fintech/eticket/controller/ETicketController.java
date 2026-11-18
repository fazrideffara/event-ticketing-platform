package com.fintech.eticket.controller;

import com.fintech.eticket.common.constant.ApiUrlConstant;
import com.fintech.eticket.common.constant.AppConstant;
import com.fintech.eticket.common.response.ApiResponse;
import com.fintech.eticket.entity.ETicket;
import com.fintech.eticket.service.ETicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiUrlConstant.ETICKETS_BASE)
@RequiredArgsConstructor
@Tag(name = "E-Ticket Inquiry & Verification", description = "Endpoints for retrieving issued e-tickets and gate QR verification")
public class ETicketController {

    private final ETicketService eTicketService;

    @GetMapping(ApiUrlConstant.ETICKETS_ORDER)
    @Operation(summary = "Get E-Ticket by order number", description = "Retrieves issued e-ticket details and QR hash by Order reference")
    public ResponseEntity<ApiResponse<ETicket>> getTicketByOrder(@PathVariable String orderNumber) {
        ETicket ticket = eTicketService.getTicketByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.ok(AppConstant.MSG_TICKET_FOUND, ticket));
    }

    @GetMapping(ApiUrlConstant.ETICKETS_CODE)
    @Operation(summary = "Get E-Ticket by ticket code", description = "Retrieves issued e-ticket details and QR Base64 image by Ticket Code")
    public ResponseEntity<ApiResponse<ETicket>> getTicketByCode(@PathVariable String ticketCode) {
        ETicket ticket = eTicketService.getTicketByCode(ticketCode);
        return ResponseEntity.ok(ApiResponse.ok(AppConstant.MSG_TICKET_FOUND, ticket));
    }

    @GetMapping(value = ApiUrlConstant.ETICKETS_RENDER, produces = org.springframework.http.MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "Render E-Ticket HTML Pass", description = "Renders official responsive HTML gate pass with QR code")
    public ResponseEntity<String> renderTicket(@PathVariable String ticketCode) {
        String html = eTicketService.renderTicketHtml(ticketCode);
        return ResponseEntity.ok(html);
    }

    @PostMapping(ApiUrlConstant.ETICKETS_VERIFY)
    @Operation(summary = "Verify and check-in E-Ticket at entrance gate", description = "Validates e-ticket authenticity and marks it as checked-in")
    public ResponseEntity<ApiResponse<ETicket>> verifyTicket(@PathVariable String ticketCode) {
        ETicket ticket = eTicketService.verifyTicketAtGate(ticketCode);
        return ResponseEntity.ok(ApiResponse.ok(AppConstant.MSG_TICKET_VERIFIED, ticket));
    }
}
