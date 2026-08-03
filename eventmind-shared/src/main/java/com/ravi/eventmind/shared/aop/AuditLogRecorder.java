package com.ravi.eventmind.shared.aop;

/**
 * Optional sink for the audit entries produced by {@link AuditAspect}. Modules
 * can plug their own implementation in (the observability module persists them);
 * when none is registered the aspect falls back to a no-op and just logs.
 */
public interface AuditLogRecorder {

    void record(String level, String message, Exception exception, String correlationId);
}
