package com.fintech.ticket.common.constant;

public final class CacheConstant {
    private CacheConstant() {}

    public static final String IDEMPOTENCY_PREFIX = "idempotency:payment:";
    public static final String EVENT_CACHE_PREFIX = "event:details:";
    public static final String EVENT_QUOTA_PREFIX = "event:quota:";

    public static final long IDEMPOTENCY_TTL_MINUTES = 10;
    public static final long EVENT_CACHE_TTL_MINUTES = 5;
}
