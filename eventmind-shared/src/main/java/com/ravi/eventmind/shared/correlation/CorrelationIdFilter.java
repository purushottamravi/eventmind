package com.ravi.eventmind.shared.correlation;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Stamps every inbound REST request with a correlation ID.
 *
 * <p>The ID is taken from the {@code X-Correlation-Id} header when present and
 * generated otherwise, then exposed through the SLF4J {@link MDC} for the duration
 * of the request so every log statement in the thread carries it. Axon dispatch
 * interceptors read the same value from the MDC and attach it to message metadata,
 * which carries the ID across process boundaries (events, forwarded headers).</p>
 */
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader(CorrelationId.HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = CorrelationId.generate();
        }
        MDC.put(CorrelationId.MDC_KEY, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CorrelationId.MDC_KEY);
        }
    }
}
