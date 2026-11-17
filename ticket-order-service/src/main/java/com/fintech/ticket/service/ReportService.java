package com.fintech.ticket.service;

import com.fintech.ticket.dto.response.OccupancyReportResponse;
import com.fintech.ticket.dto.response.RunningRevenueReportResponse;
import com.fintech.ticket.dto.response.SalesAnalyticsResponse;

import java.util.List;

public interface ReportService {
    List<RunningRevenueReportResponse> getRunningRevenueReport();
    List<OccupancyReportResponse> getOccupancySummaryReport();
    SalesAnalyticsResponse getSalesAnalytics();
}
