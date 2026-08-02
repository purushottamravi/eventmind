package com.ravi.eventmind.shared.correlation;

import org.slf4j.MDC;

/**
 * Shared correlation ID contract for the multi-module EventMind app. Every REST
 * request gets one, either echoed from the inbound header or generated fresh, and
 * it lives in the SLF4J MDC so all log lines on the thread share it. It also rides
 * along in Axon message metadata so downstream events keep the same trace.
 */
public final class CorrelationId {

    /** HTTP header used to accept / forward the correlation ID between services. */
    public static final String HEADER = "X-Correlation-Id";

    /** Key used in the SLF4J {@link MDC} so log patterns can render it (e.g. %X{correlationId}). */
    public static final String MDC_KEY = "correlationId";

    /** Key used in Axon message metadata so commands, queries and events propagate it. */
    public static final String METADATA_KEY = "correlationId";

    private CorrelationId() {
    }

    /** @return the correlation ID of the current thread, or {@code null} if none is set. */
    public static String get() {
        return MDC.get(MDC_KEY);
    }

    public static String generate() {
        return java.util.UUID.randomUUID().toString();
    }
}
