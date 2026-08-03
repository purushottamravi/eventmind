package com.ravi.eventmind.observability.aop;

import com.ravi.eventmind.observability.logging.ApplicationLogService;
import com.ravi.eventmind.shared.aop.AuditLogRecorder;
import org.springframework.stereotype.Component;

/**
 * The observability-side audit sink: every audit entry the shared {@code AuditAspect}
 * emits lands in the {@code APPLICATION_LOG} table via {@link ApplicationLogService}.
 */
@Component
public class ApplicationLogRecorder implements AuditLogRecorder {

    private final ApplicationLogService applicationLogService;

    public ApplicationLogRecorder(ApplicationLogService applicationLogService) {
        this.applicationLogService = applicationLogService;
    }

    @Override
    public void record(String level, String message, Exception exception, String correlationId) {
        applicationLogService.save(level, message, exception, correlationId);
    }
}
