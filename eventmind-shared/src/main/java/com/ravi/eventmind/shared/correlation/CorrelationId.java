package com.ravi.eventmind.shared.correlation;

import org.slf4j.MDC;

/**
 * Shared correlation ID contract for the EventMind multi-module application.
 *
 * <p>Every REST request is stamped with a correlation ID (either echoed from the
 * {@link #HEADER} inbound header or generated when absent). The ID is kept in the
 * SLF4J {@link MDC} for that request thread so all log statements share it, and is
 * propagated through Axon message {@link #METADATA_KEY metadata} so events emitted
 * downstream (query projection, observability ingestion, AI log store) carry the
 * same trace.</p>
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
