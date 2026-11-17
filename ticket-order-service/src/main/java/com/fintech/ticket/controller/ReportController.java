package com.fintech.ticket.controller;

import com.fintech.ticket.common.constant.ApiUrlConstant;
import com.fintech.ticket.common.constant.AppConstant;
import com.fintech.ticket.common.response.ApiResponse;
import com.fintech.ticket.dto.response.OccupancyReportResponse;
import com.fintech.ticket.dto.response.RunningRevenueReportResponse;
import com.fintech.ticket.dto.response.SalesAnalyticsResponse;
import com.fintech.ticket.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiUrlConstant.REPORTS_BASE)
@RequiredArgsConstructor
@Tag(name = "Financial & Occupancy Reporting", description = "Endpoints showcasing Advance Native SQL (Window Functions & CTE) and Java Stream API")
public class ReportController {

    private final ReportService reportService;

    @GetMapping(ApiUrlConstant.REPORTS_RUNNING_REVENUE)
    @Operation(
            summary = "Running Total Revenue Report (Window Function)",
            description = "Calculates cumulative revenue per event and ranks orders using PostgreSQL Native Window Functions (SUM() OVER, DENSE_RANK() OVER)"
    )
    public ResponseEntity<ApiResponse<List<RunningRevenueReportResponse>>> getRunningRevenueReport() {
        List<RunningRevenueReportResponse> report = reportService.getRunningRevenueReport();
        return ResponseEntity.ok(ApiResponse.ok(AppConstant.MSG_REPORT_GENERATED, report));
    }

    @GetMapping(ApiUrlConstant.REPORTS_OCCUPANCY)
    @Operation(
            summary = "Event Occupancy Summary (Common Table Expressions - CTE)",
            description = "Generates multi-tier occupancy percentage and gross revenue using Native SQL CTE (WITH tier_metrics AS ...)"
    )
    public ResponseEntity<ApiResponse<List<OccupancyReportResponse>>> getOccupancySummaryReport() {
        List<OccupancyReportResponse> report = reportService.getOccupancySummaryReport();
        return ResponseEntity.ok(ApiResponse.ok(AppConstant.MSG_REPORT_GENERATED, report));
    }

    @GetMapping(ApiUrlConstant.REPORTS_SALES_ANALYTICS)
    @Operation(
            summary = "Sales Analytics (Java Stream API)",
            description = "Performs complex in-memory grouping, filtering, and reduction using Java Stream API (groupingBy, reducing, summingLong)"
    )
    public ResponseEntity<ApiResponse<SalesAnalyticsResponse>> getSalesAnalytics() {
        SalesAnalyticsResponse analytics = reportService.getSalesAnalytics();
        return ResponseEntity.ok(ApiResponse.ok(AppConstant.MSG_REPORT_GENERATED, analytics));
    }
}
