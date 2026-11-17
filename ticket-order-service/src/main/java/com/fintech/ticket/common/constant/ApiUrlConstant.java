package com.fintech.ticket.common.constant;

public final class ApiUrlConstant {
    private ApiUrlConstant() {}

    public static final String API_V1 = "/api/v1";

    public static final String AUTH_BASE = API_V1 + "/auth";
    public static final String AUTH_REGISTER = "/register";
    public static final String AUTH_LOGIN = "/login";

    public static final String EVENTS_BASE = API_V1 + "/events";
    public static final String EVENTS_ID = "/{eventId}";

    public static final String ORDERS_BASE = API_V1 + "/orders";
    public static final String ORDERS_ID = "/{orderId}";
    public static final String ORDERS_USER = "/my-orders";

    public static final String PAYMENTS_BASE = API_V1 + "/payments";

    public static final String REPORTS_BASE = API_V1 + "/reports";
    public static final String REPORTS_RUNNING_REVENUE = "/running-revenue";
    public static final String REPORTS_OCCUPANCY = "/occupancy-summary";
    public static final String REPORTS_SALES_ANALYTICS = "/sales-analytics";

    public static final String AUDIT_BASE = API_V1 + "/audits";
}
