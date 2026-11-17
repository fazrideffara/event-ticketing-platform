package com.fintech.ticket.common.constant;

public final class AppConstant {
    private AppConstant() {}

    public static final String HEADER_IDEMPOTENCY_KEY = "X-Idempotency-Key";

    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_COMPLETED = "COMPLETED";

    public static final String MSG_REGISTRATION_SUCCESS = "User registration completed successfully.";
    public static final String MSG_LOGIN_SUCCESS = "Authentication successful.";
    public static final String MSG_ORDER_CREATED = "Ticket booking created successfully. Please proceed with payment.";
    public static final String MSG_PAYMENT_SUCCESS = "Payment executed successfully. E-Ticket is being generated.";
    public static final String MSG_EVENT_FOUND = "Event details retrieved successfully.";
    public static final String MSG_REPORT_GENERATED = "Report generated successfully.";
    public static final String MSG_AUDIT_RETRIEVED = "Audit trail logs retrieved successfully.";

    public static final String ORDER_PREFIX = "ORD-";
}
