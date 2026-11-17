package com.fintech.eticket.common.constant;

public final class ApiUrlConstant {
    private ApiUrlConstant() {}

    public static final String API_V1 = "/api/v1";
    public static final String ETICKETS_BASE = API_V1 + "/etickets";
    public static final String ETICKETS_ORDER = "/order/{orderNumber}";
    public static final String ETICKETS_VERIFY = "/verify/{ticketCode}";
}
